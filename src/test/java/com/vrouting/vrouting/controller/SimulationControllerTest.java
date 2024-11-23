package com.vrouting.vrouting.controller;

import com.vrouting.simulation.SimulationMetrics;
import com.vrouting.vrouting.model.SimulationRequest;
import com.vrouting.vrouting.model.SimulationResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class SimulationControllerTest {
    private static final Logger logger = LoggerFactory.getLogger(SimulationControllerTest.class);

    @Autowired
    private SimulationController simulationController;

    @Test
    public void testSimulationLifecycle() throws InterruptedException {
        // 1. Start Simulation
        SimulationRequest request = new SimulationRequest();
        request.setNodeCount(5);
        request.setDuration(10);
        request.setEnableClustering(true);

        logger.info("Starting simulation with request: nodeCount={}, duration={}, clustering={}",
            request.getNodeCount(), request.getDuration(), request.isEnableClustering());

        ResponseEntity<SimulationResponse> startResponse = simulationController.startSimulation(request);
        logger.info("Start simulation response: {}", startResponse);

        // Verify start response
        assertTrue(startResponse.getStatusCode().is2xxSuccessful(), 
            "Start simulation should return 200, got: " + startResponse.getStatusCode());
        
        SimulationResponse responseBody = startResponse.getBody();
        assertNotNull(responseBody, "Response body should not be null");
        
        if (!responseBody.isSuccess()) {
            logger.error("Simulation failed to start: {}", responseBody.getMessage());
        }
        assertTrue(responseBody.isSuccess(), "Simulation should start successfully: " + responseBody.getMessage());
        
        String simulationId = responseBody.getSimulationId();
        assertNotNull(simulationId, "Simulation ID should not be null");
        logger.info("Started simulation with ID: {}", simulationId);

        // Wait a bit for simulation to initialize
        Thread.sleep(1000);

        // 2. Check Initial Status
        ResponseEntity<Map<String, Object>> statusResponse = simulationController.getStatus(simulationId);
        logger.info("Initial status response: {}", statusResponse);

        assertTrue(statusResponse.getStatusCode().is2xxSuccessful(), 
            "Get status should return 200, got: " + statusResponse.getStatusCode());
        
        Map<String, Object> status = statusResponse.getBody();
        assertNotNull(status, "Status body should not be null");
        
        // Log all status fields
        status.forEach((key, value) -> logger.info("Initial status {}: {}", key, value));

        assertTrue((Boolean) status.get("initialized"), 
            "Simulation should be initialized, status: " + status);
        assertEquals(0, status.get("stepsCompleted"), 
            "Initial steps should be 0, got: " + status.get("stepsCompleted"));

        // 3. Step Through Simulation
        for (int stepNum = 1; stepNum <= 5; stepNum++) {
            final int currentStep = stepNum;
            logger.info("Executing step {}", currentStep);
            
            ResponseEntity<String> stepResponse = simulationController.stepSimulation(simulationId);
            logger.info("Step {} response: {}", currentStep, stepResponse);
            
            assertTrue(stepResponse.getStatusCode().is2xxSuccessful(), 
                "Step " + currentStep + " should return 200, got: " + stepResponse.getStatusCode());
            assertEquals("Step completed", stepResponse.getBody(), 
                "Step " + currentStep + " response should indicate completion, got: " + stepResponse.getBody());
            
            // Check status after step
            statusResponse = simulationController.getStatus(simulationId);
            status = statusResponse.getBody();
            assertNotNull(status, "Status after step " + currentStep + " should not be null");
            
            int stepsCompleted = (Integer) status.get("stepsCompleted");
            logger.info("After step {}, completed steps: {}", currentStep, stepsCompleted);
            
            assertTrue(stepsCompleted >= currentStep, 
                "Steps should be increasing. Expected >= " + currentStep + ", got: " + stepsCompleted);
            
            // Log all status fields after step
            final Map<String, Object> stepStatus = status;
            stepStatus.forEach((key, value) -> logger.info("Status after step {} - {}: {}", currentStep, key, value));
            
            Thread.sleep(1000); // Longer delay between steps
        }

        // 4. Get Metrics
        ResponseEntity<List<SimulationMetrics>> metricsResponse = simulationController.getMetrics(simulationId);
        logger.info("Metrics response: {}", metricsResponse);

        assertTrue(metricsResponse.getStatusCode().is2xxSuccessful(), 
            "Get metrics should return 200, got: " + metricsResponse.getStatusCode());
        
        List<SimulationMetrics> metrics = metricsResponse.getBody();
        assertNotNull(metrics, "Metrics should not be null");
        logger.info("Received {} metrics", metrics.size());
        metrics.forEach(metric -> logger.info("Metric: {}", metric));

        // 5. Stop Simulation
        ResponseEntity<Void> stopResponse = simulationController.stopSimulation(simulationId);
        logger.info("Stop simulation response: {}", stopResponse);

        assertTrue(stopResponse.getStatusCode().is2xxSuccessful(), 
            "Stop simulation should return 200, got: " + stopResponse.getStatusCode());

        // Wait for cleanup
        Thread.sleep(1000);

        // 6. Verify Simulation Stopped
        statusResponse = simulationController.getStatus(simulationId);
        status = statusResponse.getBody();
        assertNotNull(status, "Final status should not be null");
        
        // Log final status
        logger.info("Final status response: {}", statusResponse);
        final Map<String, Object> finalStatus = status;
        finalStatus.forEach((key, value) -> logger.info("Final status {}: {}", key, value));

        assertFalse((Boolean) status.get("initialized"), 
            "Simulation should not be initialized after stopping, status: " + status);
    }
}
