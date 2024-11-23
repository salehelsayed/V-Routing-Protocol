package com.vrouting.vrouting.controller;

import com.vrouting.simulation.SimulationEngine;
import com.vrouting.simulation.SimulationMetrics;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/simulation")
public class SimulationController {

    private SimulationEngine simulationEngine;

    public SimulationController(SimulationEngine simulationEngine) {
        this.simulationEngine = simulationEngine;
    }

    @PostMapping("/start")
    public ResponseEntity<String> startSimulation(@RequestParam int nodeCount) {
        String simulationId = simulationEngine.startSimulation(nodeCount);
        return ResponseEntity.ok(simulationId);
    }

    @GetMapping("/status/{simulationId}")
    public ResponseEntity<Map<String, Object>> getStatus(@PathVariable String simulationId) {
        Map<String, Object> status = simulationEngine.getSimulationStatus(simulationId);
        return ResponseEntity.ok(status);
    }

    @PostMapping("/step/{simulationId}")
    public ResponseEntity<String> stepSimulation(@PathVariable String simulationId) {
        simulationEngine.stepSimulation(simulationId);
        return ResponseEntity.ok("Simulation stepped for simulation ID: " + simulationId);
    }

    @GetMapping("/metrics/{simulationId}")
    public ResponseEntity<List<SimulationMetrics>> getMetrics(@PathVariable String simulationId) {
        List<SimulationMetrics> metrics = simulationEngine.getSimulationMetrics(simulationId);
        return ResponseEntity.ok(metrics);
    }

    @PostMapping("/stop/{simulationId}")
    public ResponseEntity<Void> stopSimulation(@PathVariable String simulationId) {
        simulationEngine.stopSimulation(simulationId);
        return ResponseEntity.ok().build();
    }
}
