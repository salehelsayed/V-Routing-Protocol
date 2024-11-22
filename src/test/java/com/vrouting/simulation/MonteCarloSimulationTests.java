package com.vrouting.simulation;

import com.vrouting.network.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class MonteCarloSimulationTests {
    private SimulationEngine engine;
    private static final int NODE_COUNT = 50;
    private static final double FAILURE_PROB = 0.1;
    private static final double RECOVERY_PROB = 0.3;
    private static final double MSG_GEN_PROB = 0.2;
    private static final int SIM_STEPS = 1000;
    
    @BeforeEach
    void setUp() {
        engine = new SimulationEngine(
            NODE_COUNT,
            FAILURE_PROB,
            RECOVERY_PROB,
            MSG_GEN_PROB,
            SIM_STEPS
        );
    }
    
    @Test
    void testNetworkFormation() {
        engine.initializeNetwork();
        SimulationMetrics metrics = engine.getCurrentMetrics();
        
        assertTrue(metrics.getNodeCount() == NODE_COUNT);
        assertTrue(metrics.getClusterCount() > 0);
    }
    
    @Test
    void testNodeFailureScenario() {
        engine.initializeNetwork();
        engine.simulateNodeFailures();
        
        SimulationMetrics metrics = engine.getCurrentMetrics();
        assertTrue(metrics.getFailedNodeCount() > 0);
        assertTrue(metrics.getActiveNodeCount() < NODE_COUNT);
    }
    
    @Test
    void testMessageDeliveryReliability() {
        engine.initializeNetwork();
        engine.runSimulationSteps(100);
        
        SimulationMetrics metrics = engine.getCurrentMetrics();
        assertTrue(metrics.getMessageDeliveryRate() > 0.8); // Expected 80% success rate
        assertTrue(metrics.getAverageLatency() < 500); // Expected latency under 500ms
    }
    
    @Test
    void testClusterStability() {
        engine.initializeNetwork();
        
        // Record initial cluster count
        int initialClusterCount = engine.getCurrentMetrics().getClusterCount();
        
        // Run simulation
        engine.runSimulationSteps(500);
        
        // Check cluster stability
        SimulationMetrics metrics = engine.getCurrentMetrics();
        assertTrue(Math.abs(metrics.getClusterCount() - initialClusterCount) < 5);
    }
    
    @Test
    void testSecureTunnelReliability() {
        engine.initializeNetwork();
        engine.runSimulationSteps(200);
        
        SimulationMetrics metrics = engine.getCurrentMetrics();
        assertTrue(metrics.getTunnelEstablishmentRate() > 0.9); // Expected 90% success rate
        assertTrue(metrics.getSecureMessageDeliveryRate() > 0.95); // Expected 95% success rate
    }
    
    @Test
    void testNetworkRecovery() {
        engine.initializeNetwork();
        
        // Record initial active node count
        int initialActiveCount = engine.getCurrentMetrics().getActiveNodeCount();
        
        // Simulate massive failure
        engine.simulateMassiveFailure(0.8); // 80% of nodes fail
        
        // Record metrics after failure
        SimulationMetrics postFailureMetrics = engine.getCurrentMetrics();
        assertTrue(postFailureMetrics.getActiveNodeCount() < initialActiveCount);
        
        // Run recovery simulation
        for (int i = 0; i < 300; i++) {
            engine.simulateNodeRecoveries();
        }
        
        // Check recovery metrics
        SimulationMetrics recoveryMetrics = engine.getCurrentMetrics();
        assertTrue(recoveryMetrics.getActiveNodeCount() > postFailureMetrics.getActiveNodeCount());
        assertTrue(recoveryMetrics.getClusterCount() >= postFailureMetrics.getClusterCount());
    }
    
    @Test
    void testRoutingEfficiency() {
        engine.initializeNetwork();
        engine.runSimulationSteps(100);
        
        SimulationMetrics metrics = engine.getCurrentMetrics();
        assertTrue(metrics.getAverageHopCount() < 4); // Expected average less than 4 hops
        assertTrue(metrics.getRoutingTableAccuracy() > 0.9); // Expected 90% routing accuracy
    }
}
