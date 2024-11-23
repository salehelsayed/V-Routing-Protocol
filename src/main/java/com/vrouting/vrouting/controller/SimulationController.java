package com.vrouting.vrouting.controller;

import com.vrouting.simulation.SimulationEngine;
import com.vrouting.simulation.SimulationMetrics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private SimulationEngine engine;

    @PostMapping("/start")
    public ResponseEntity<String> startSimulation(@RequestBody SimulationConfig config) {
        logger.info("Starting simulation with config: {}", config);
        engine = new SimulationEngine(
            config.getNodeCount(),
            config.getFailureProbability(),
            config.getRecoveryProbability(),
            config.getMessageGenerationProbability(),
            config.getSimulationSteps()
        );
        logger.info("Simulation initialized");
        return ResponseEntity.ok("Simulation initialized");
    }

    @PostMapping("/step")
    public ResponseEntity<String> stepSimulation() {
        if (engine == null) {
            logger.warn("Attempted to step simulation but it is not initialized");
            return ResponseEntity.badRequest().body("Simulation not initialized");
        }
        logger.info("Performing simulation step");
        engine.simulateStep();
        logger.info("Simulation step completed");
        return ResponseEntity.ok("Step completed");
    }

    @GetMapping("/metrics")
    public ResponseEntity<List<SimulationMetrics>> getMetrics() {
        if (engine == null) {
            logger.warn("Attempted to get metrics but simulation is not initialized");
            return ResponseEntity.badRequest().body(null);
        }
        logger.info("Fetching simulation metrics");
        return ResponseEntity.ok(engine.getMetrics());
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getStatus() {
        if (engine == null) {
            logger.info("Simulation not initialized, returning default status");
            return ResponseEntity.ok(Map.of(
                "initialized", false,
                "simulationComplete", false,
                "stepsCompleted", 0
            ));
        }

        Map<String, Object> status = new HashMap<>();
        status.put("initialized", true);
        status.put("simulationComplete", engine.isSimulationComplete());
        status.put("stepsCompleted", engine.getCurrentStep());
        status.put("totalSteps", engine.getSimulationSteps());
        logger.info("Fetching simulation status: {}", status);
        return ResponseEntity.ok(status);
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
