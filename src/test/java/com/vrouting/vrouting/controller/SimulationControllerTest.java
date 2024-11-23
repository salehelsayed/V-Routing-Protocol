package com.vrouting.vrouting.controller;

import com.vrouting.simulation.SimulationEngine;
import com.vrouting.simulation.SimulationMetrics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class SimulationControllerTest {

    private SimulationController simulationController;

    @BeforeEach
    public void setUp() {
        SimulationEngine simulationEngine = new SimulationEngine();
        simulationController = new SimulationController(simulationEngine);
    }

    @Test
    public void testSimulationLifecycle() {
        // Start Simulation
        ResponseEntity<String> startResponse = simulationController.startSimulation(10);
        assertEquals(200, startResponse.getStatusCodeValue());
        String simulationId = startResponse.getBody();
        assertNotNull(simulationId);

        // Get Status
        ResponseEntity<Map<String, Object>> statusResponse = simulationController.getStatus(simulationId);
        assertEquals(200, statusResponse.getStatusCodeValue());
        Map<String, Object> status = statusResponse.getBody();
        assertNotNull(status);

        // Step Simulation
        ResponseEntity<String> stepResponse = simulationController.stepSimulation(simulationId);
        assertEquals(200, stepResponse.getStatusCodeValue());

        // Get Metrics
        ResponseEntity<List<SimulationMetrics>> metricsResponse = simulationController.getMetrics(simulationId);
        assertEquals(200, metricsResponse.getStatusCodeValue());
        List<SimulationMetrics> metrics = metricsResponse.getBody();
        assertNotNull(metrics);

        // Stop Simulation
        ResponseEntity<Void> stopResponse = simulationController.stopSimulation(simulationId);
        assertEquals(200, stopResponse.getStatusCodeValue());

        // Verify Simulation is Stopped
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            simulationController.getStatus(simulationId);
        });
        assertEquals("Invalid simulation ID", exception.getMessage());
    }
}
