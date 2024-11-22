package com.vrouting.network;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class NodeUnitTests {
    private Node testNode;
    
    @Mock
    private ClusterManager clusterManager;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        testNode = new Node("TestNode1");
        testNode.setClusterManager(clusterManager);
    }
    
    @Test
    void testHeartbeatPhaseTransitions() {
        // Test initial phase
        assertEquals(HeartbeatPhase.DISCOVERY, testNode.getCurrentHeartbeatPhase());
        
        // Simulate time passing for phase transitions
        testNode.updateHeartbeatPhase();
        assertEquals(HeartbeatPhase.STABILIZATION, testNode.getCurrentHeartbeatPhase());
        
        testNode.updateHeartbeatPhase();
        assertEquals(HeartbeatPhase.MAINTENANCE, testNode.getCurrentHeartbeatPhase());
    }
    
    @Test
    void testMessagePropagation() {
        Message testMessage = new Message(MessageType.HEARTBEAT, "TestContent");
        testNode.sendMessage(testMessage);
        
        // Verify message was processed
        verify(clusterManager).propagateMessage(eq(testMessage), eq(testNode));
    }
    
    @Test
    void testNodeFailureRecovery() {
        // Test node failure
        testNode.markAsFailed();
        assertFalse(testNode.isActive());
        
        // Test node recovery
        testNode.recover();
        assertTrue(testNode.isActive());
    }
    
    @Test
    void testRoutingTableManagement() {
        RoutingEntry entry = new RoutingEntry("DestNode", "NextHop", 2);
        testNode.updateRoutingTable(entry);
        
        assertEquals(entry, testNode.getRoutingTable().getEntry("DestNode"));
    }
}
