package com.vrouting.vrouting.service;

import com.vrouting.network.socket.config.NetworkConfig;
import com.vrouting.network.socket.core.Node;
import com.vrouting.network.socket.core.NodeState;
import com.vrouting.network.socket.core.SimulationNode;
import com.vrouting.simulation.SimulationEngine;
import com.vrouting.simulation.SimulationMetrics;
import com.vrouting.vrouting.model.SimulationRequest;
import com.vrouting.vrouting.model.SimulationResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class SimulationService {
    private static final Logger logger = LoggerFactory.getLogger(SimulationService.class);
    private final Map<String, List<Node>> activeSimulations = new HashMap<>();
    private final Map<String, SimulationEngine> simulationEngines = new HashMap<>();
    private String latestSimulationId;

    public String getLatestSimulationId() {
        return latestSimulationId;
    }

    public SimulationResponse startSimulation(SimulationRequest request) {
        logger.info("Starting new simulation with {} nodes", request.getNodeCount());
        
        SimulationResponse response = new SimulationResponse();
        
        // Validate request
        if (request.getNodeCount() <= 0) {
            response.setSuccess(false);
            response.setMessage("Node count must be greater than 0");
            return response;
        }
        
        if (request.getDuration() <= 0) {
            response.setSuccess(false);
            response.setMessage("Duration must be greater than 0");
            return response;
        }
        
        String simulationId = UUID.randomUUID().toString();
        
        try {
            // Create simulation engine with configuration
            SimulationEngine engine = new SimulationEngine(
                request.getNodeCount(),
                0.1,  // Default failure probability
                0.3,  // Default recovery probability
                0.2,  // Default message generation probability
                request.getDuration() * 10  // Convert duration to steps
            );
            
            // Create and initialize nodes
            List<Node> nodes = createNodes(request.getNodeCount());
            activeSimulations.put(simulationId, nodes);
            
            // Start the nodes
            nodes.forEach(Node::start);
            
            // Start the simulation engine
            engine.startSimulation();
            
            simulationEngines.put(simulationId, engine);
            latestSimulationId = simulationId;
            
            // Prepare response
            response.setSimulationId(simulationId);
            response.setNodes(nodes.stream().map(Node::getNodeId).toList());
            response.setConnections(buildConnectionsMap(nodes));
            response.setSuccess(true);
            response.setMessage("Simulation started successfully");
            
            // Schedule simulation end if duration is specified
            if (request.getDuration() > 0) {
                scheduleSimulationEnd(simulationId, request.getDuration());
            }
            
        } catch (Exception e) {
            logger.error("Error starting simulation", e);
            response.setSuccess(false);
            response.setMessage("Failed to start simulation: " + e.getMessage());
        }
        
        return response;
    }

    public void stopSimulation(String simulationId) {
        List<Node> nodes = activeSimulations.remove(simulationId);
        SimulationEngine engine = simulationEngines.remove(simulationId);
        if (simulationId.equals(latestSimulationId)) {
            latestSimulationId = null;
        }
        if (nodes != null) {
            nodes.forEach(Node::stop);
            logger.info("Stopped simulation {}", simulationId);
        }
    }

    public String stepSimulation(String simulationId) {
        SimulationEngine engine = simulationEngines.get(simulationId);
        if (engine == null) {
            return "Simulation not found";
        }
        engine.simulateStep();
        return "Step completed";
    }

    public List<SimulationMetrics> getMetrics(String simulationId) {
        SimulationEngine engine = simulationEngines.get(simulationId);
        if (engine == null) {
            return Collections.emptyList();
        }
        return engine.getMetrics();
    }

    public Map<String, Object> getStatus(String simulationId) {
        SimulationEngine engine = simulationEngines.get(simulationId);
        List<Node> nodes = activeSimulations.get(simulationId);
        
        if (engine == null || nodes == null) {
            return Map.of(
                "initialized", false,
                "simulationComplete", false,
                "stepsCompleted", 0,
                "totalSteps", 0,
                "activeSimulations", simulationEngines.size(),
                "activeNodes", 0,
                "totalNodes", 0
            );
        }

        // Count nodes that are in REGULAR, CLUSTER_HEAD, or CLUSTER_MEMBER state
        long activeNodeCount = nodes.stream()
            .map(Node::getState)
            .filter(state -> state == NodeState.REGULAR || 
                           state == NodeState.CLUSTER_HEAD || 
                           state == NodeState.CLUSTER_MEMBER)
            .count();
        
        return Map.of(
            "initialized", true,
            "simulationComplete", engine.isSimulationComplete(),
            "stepsCompleted", engine.getCurrentStep(),
            "totalSteps", engine.getSimulationSteps(),
            "activeSimulations", simulationEngines.size(),
            "activeNodes", activeNodeCount,
            "totalNodes", nodes.size()
        );
    }

    private List<Node> createNodes(int count) {
        List<Node> nodes = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            String nodeId = "node-" + i;
            NetworkConfig config = new NetworkConfig.Builder()
                .port(8080)
                .maxConnections(100)
                .bufferSize(8192)
                .connectionTimeout(30000)
                .heartbeatInterval(5000)
                .maxRetries(3)
                .clusterHeadEligible(true)
                .clusterHeadThreshold(0.7)
                .maxNetworkDepth(10)
                .maxClusterSize(50)
                .build();
            nodes.add(new SimulationNode(nodeId, config));
        }
        return nodes;
    }

    private Map<String, List<String>> buildConnectionsMap(List<Node> nodes) {
        Map<String, List<String>> connections = new HashMap<>();
        nodes.forEach(node -> {
            List<String> peers = node.getPeerDirectory().getAllPeers().stream().toList();
            connections.put(node.getNodeId(), peers);
        });
        return connections;
    }

    private void scheduleSimulationEnd(String simulationId, int duration) {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                stopSimulation(simulationId);
            }
        }, duration * 1000L);
    }
}
