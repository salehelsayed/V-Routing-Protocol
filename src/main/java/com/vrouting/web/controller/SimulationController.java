package com.vrouting.web.controller;

import com.vrouting.simulation.SimulationEngine;
import com.vrouting.simulation.SimulationMetrics;
import com.vrouting.web.dto.NetworkState;
import com.vrouting.web.dto.SimulationRequest;
import com.vrouting.network.Node;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.PreDestroy;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/simulation")
public class SimulationController {
    
    private final SimpMessagingTemplate messagingTemplate;
    private SimulationEngine currentSimulation;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public SimulationController(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @PostMapping("/start")
    public void startSimulation(@RequestBody SimulationRequest request) {
        if (currentSimulation != null) {
            stopSimulation();
        }

        currentSimulation = new SimulationEngine(
            request.getNodeCount(),
            request.getFailureProbability(),
            request.getRecoveryProbability(),
            0.3,  // Default message rate
            100   // Run for 100 steps
        );

        // Initialize and start the simulation
        currentSimulation.initializeNetwork();
        currentSimulation.establishNodeConnections();  
        currentSimulation.start();

        // Schedule periodic updates with error handling and rate limiting
        scheduler.scheduleAtFixedRate(() -> {
            try {
                if (currentSimulation != null) {
                    currentSimulation.step();
                    
                    // Send network state and simulation status with rate limiting
                    NetworkState state = getCurrentNetworkState();
                    if (state != null) {
                        // Only send if there are actual changes
                        if (state.hasChanges()) {
                            messagingTemplate.convertAndSend("/topic/network-state", state);
                        }
                        
                        // Send status update less frequently
                        if (currentSimulation.getStatus().hasChanges()) {
                            messagingTemplate.convertAndSend("/topic/simulation-status", 
                                currentSimulation.getStatus());
                        }
                    }
                }
            } catch (Exception e) {
                // Log the error but keep the simulation running
                System.err.println("Error in simulation step: " + e.getMessage());
            }
        }, 0, 2, TimeUnit.SECONDS); // Increased interval to 2 seconds
    }

    @PostMapping("/stop")
    public void stopSimulation() {
        if (currentSimulation != null) {
            currentSimulation.stop();
            currentSimulation = null;
        }
    }

    @GetMapping("/state")
    public NetworkState getCurrentNetworkState() {
        if (currentSimulation == null) {
            return null;
        }

        try {
            List<com.vrouting.network.socket.core.Node> coreNodes = currentSimulation.getNodes().stream()
                .map(node -> new com.vrouting.network.socket.core.Node(node.getId(), node.getProperties()))
                .collect(Collectors.toList());

            return NetworkState.builder()
                .nodes(coreNodes.stream().map(node -> new Node(node.getId(), node.getProperties())).collect(Collectors.toList()))
                .metrics(currentSimulation.getCurrentMetrics())
                .build();
        } catch (Exception e) {
            System.err.println("Error getting network state: " + e.getMessage());
            return null;
        }
    }

    @PreDestroy
    public void cleanup() {
        // Ensure we clean up the scheduler when the application shuts down
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
