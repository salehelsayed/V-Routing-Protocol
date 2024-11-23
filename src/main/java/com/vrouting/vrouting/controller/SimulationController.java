package com.vrouting.vrouting.controller;

import com.vrouting.simulation.SimulationEngine;
import com.vrouting.simulation  .SimulationMetrics;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/simulation")
public class SimulationController {

    private SimulationEngine engine;

    @PostMapping("/start")
    public ResponseEntity<String> startSimulation(@RequestBody SimulationConfig config) {
        engine = new SimulationEngine(
            config.getNodeCount(),
            config.getFailureProbability(),
            config.getRecoveryProbability(),
            config.getMessageGenerationProbability(),
            config.getSimulationSteps()
        );
        return ResponseEntity.ok("Simulation initialized");
    }

    @PostMapping("/step")
    public ResponseEntity<String> stepSimulation() {
        if (engine == null) {
            return ResponseEntity.badRequest().body("Simulation not initialized");
        }
        engine.simulateStep();
        return ResponseEntity.ok("Step completed");
    }

    @GetMapping("/metrics")
    public ResponseEntity<List<SimulationMetrics>> getMetrics() {
        if (engine == null) {
            return ResponseEntity.badRequest().body(null);
        }
        return ResponseEntity.ok(engine.getMetrics());
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getStatus() {
        if (engine == null) {
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
