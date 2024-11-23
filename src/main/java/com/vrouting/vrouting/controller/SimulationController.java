package com.vrouting.vrouting.controller;

import com.vrouting.simulation.SimulationEngine;
import com.vrouting.simulation.SimulationMetrics;
import com.vrouting.vrouting.model.SimulationRequest;
import com.vrouting.vrouting.model.SimulationResponse;
import com.vrouting.vrouting.service.SimulationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;

@RestController
@RequestMapping("/api/simulation")
public class SimulationController {
    private static final Logger logger = LoggerFactory.getLogger(SimulationController.class);
    private final SimulationService simulationService;

    @Autowired
    public SimulationController(SimulationService simulationService) {
        this.simulationService = simulationService;
        logger.info("SimulationController initialized");
    }

    @PostMapping("/start")
    public ResponseEntity<SimulationResponse> startSimulation(@RequestBody SimulationRequest request) {
        logger.info("Received simulation request: {}", request);
        
        // Validate request
        if (request.getNodeCount() <= 0) {
            SimulationResponse response = new SimulationResponse();
            response.setSuccess(false);
            response.setMessage("Node count must be greater than 0");
            return ResponseEntity.badRequest().body(response);
        }
        
        if (request.getDuration() <= 0) {
            SimulationResponse response = new SimulationResponse();
            response.setSuccess(false);
            response.setMessage("Duration must be greater than 0");
            return ResponseEntity.badRequest().body(response);
        }
        
        SimulationResponse response = simulationService.startSimulation(request);
        if (!response.isSuccess()) {
            return ResponseEntity.badRequest().body(response);
        }
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{simulationId}/stop")
    public ResponseEntity<Void> stopSimulation(@PathVariable String simulationId) {
        logger.info("Stopping simulation: {}", simulationId);
        simulationService.stopSimulation(simulationId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{simulationId}/step")
    public ResponseEntity<String> stepSimulation(@PathVariable String simulationId) {
        return ResponseEntity.ok(simulationService.stepSimulation(simulationId));
    }

    @GetMapping("/{simulationId}/metrics")
    public ResponseEntity<List<SimulationMetrics>> getMetrics(@PathVariable String simulationId) {
        return ResponseEntity.ok(simulationService.getMetrics(simulationId));
    }

    @GetMapping("/{simulationId}/status")
    public ResponseEntity<Map<String, Object>> getStatus(@PathVariable String simulationId) {
        return ResponseEntity.ok(simulationService.getStatus(simulationId));
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getCurrentStatus() {
        String latestSimulationId = simulationService.getLatestSimulationId();
        if (latestSimulationId == null) {
            return ResponseEntity.ok(Map.of(
                "initialized", false,
                "simulationComplete", false,
                "stepsCompleted", 0,
                "totalSteps", 0,
                "error", "No active simulation"
            ));
        }
        return ResponseEntity.ok(simulationService.getStatus(latestSimulationId));
    }

    @GetMapping("/metrics")
    public ResponseEntity<List<SimulationMetrics>> getCurrentMetrics() {
        String latestSimulationId = simulationService.getLatestSimulationId();
        if (latestSimulationId == null) {
            return ResponseEntity.ok(Collections.emptyList());
        }
        return ResponseEntity.ok(simulationService.getMetrics(latestSimulationId));
    }
}

// Class to hold simulation configuration parameters
class SimulationConfig {
    private int nodeCount;
    private double failureProbability;
    private double recoveryProbability;
    private double messageGenerationProbability;
    private int simulationSteps;

    // Getters and setters

    public int getNodeCount() {
        return nodeCount;
    }

    public void setNodeCount(int nodeCount) {
        this.nodeCount = nodeCount;
    }

    public double getFailureProbability() {
        return failureProbability;
    }

    public void setFailureProbability(double failureProbability) {
        this.failureProbability = failureProbability;
    }

    public double getRecoveryProbability() {
        return recoveryProbability;
    }

    public void setRecoveryProbability(double recoveryProbability) {
        this.recoveryProbability = recoveryProbability;
    }

    public double getMessageGenerationProbability() {
        return messageGenerationProbability;
    }

    public void setMessageGenerationProbability(double messageGenerationProbability) {
        this.messageGenerationProbability = messageGenerationProbability;
    }

    public int getSimulationSteps() {
        return simulationSteps;
    }

    public void setSimulationSteps(int simulationSteps) {
        this.simulationSteps = simulationSteps;
    }
}
