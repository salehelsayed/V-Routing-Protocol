package com.vrouting.network.socket;

import com.vrouting.network.Node;
import com.vrouting.network.socket.config.NetworkConfig;
import com.vrouting.network.socket.message.Message;
import com.vrouting.network.socket.message.MessageType;
import com.vrouting.network.socket.cluster.Cluster;
import com.vrouting.network.socket.core.Phase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class VRoutingTest {
    private Node node1;
    private Node node2;
    private NetworkConfig config;

    @BeforeEach
    void setUp() {
        config = new NetworkConfig();
        config.setHeartbeatInterval(1000);
        config.setCleanupInterval(5000);
        
        node1 = new Node("Node1");
        node2 = new Node("Node2");
    }

    @Test
    void testNodeInitialization() {
        assertNotNull(node1.getId());
        assertNotNull(node2.getId());
        assertTrue(node1.isActive());
        assertTrue(node2.isActive());
    }

    @Test
    void testNodeCommunication() {
        node1.addNeighbor(node2);
        assertTrue(node1.getNeighbors().contains(node2));
        assertTrue(node2.getNeighbors().contains(node1));
    }

    @Test
    void testClusterFormation() {
        Cluster cluster = new Cluster(node1);
        cluster.addMember(node2);
        
        assertEquals(node1, cluster.getClusterHead());
        assertTrue(cluster.getMembers().contains(node2));
        assertEquals(2, cluster.getSize());
    }

    @Test
    void testMessageHandling() {
        Message message = new Message();
        message.setType(MessageType.HEARTBEAT);
        message.setSourceId(node2.getId());
        
        node1.processMessage(message);
        assertTrue(node1.getPeerDirectory().isPeerActive(node2.getId()));
    }

    @Test
    void testNodeFailure() {
        node1.addNeighbor(node2);
        assertTrue(node2.getNeighbors().contains(node1));
        
        node1.markAsFailed();
        assertFalse(node1.isActive());
        
        node1.recover();
        assertTrue(node1.isActive());
    }
}
