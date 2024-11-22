package com.vrouting.network;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ClusterIntegrationTests {
    private Cluster cluster1;
    private Cluster cluster2;
    private Node node1, node2, node3;
    private ClusterManager clusterManager;
    
    @BeforeEach
    void setUp() {
        clusterManager = new ClusterManager();
        
        // Create test nodes
        node1 = new Node("Node1");
        node2 = new Node("Node2");
        node3 = new Node("Node3");
        
        // Create test clusters
        cluster1 = new Cluster("Cluster1");
        cluster2 = new Cluster("Cluster2");
    }
    
    @Test
    void testClusterFormation() {
        cluster1.addNode(node1);
        cluster1.addNode(node2);
        
        assertEquals(2, cluster1.getNodeCount());
        assertTrue(cluster1.containsNode(node1));
        assertTrue(cluster1.containsNode(node2));
    }
    
    @Test
    void testClusterHeadPromotion() {
        cluster1.addNode(node1);
        cluster1.addNode(node2);
        
        // Promote node1 to cluster head
        cluster1.promoteToClusterHead(node1);
        
        assertEquals(node1, cluster1.getClusterHead());
        assertTrue(node1.isClusterHead());
    }
    
    @Test
    void testInterClusterCommunication() {
        cluster1.addNode(node1);
        cluster2.addNode(node2);
        
        // Set up cluster heads
        cluster1.promoteToClusterHead(node1);
        cluster2.promoteToClusterHead(node2);
        
        // Test secure tunnel establishment
        SecurityTunnel tunnel = cluster1.establishTunnelWith(cluster2);
        assertTrue(tunnel.isEstablished());
        
        // Test message routing between clusters
        Message message = new Message(MessageType.ROUTING, "TestContent");
        assertTrue(cluster1.sendMessageToCluster(cluster2, message));
    }
    
    @Test
    void testClusterMerging() {
        cluster1.addNode(node1);
        cluster2.addNode(node2);
        
        clusterManager.mergeClusters(cluster1, cluster2);
        
        // Verify merged cluster properties
        Cluster mergedCluster = clusterManager.getClusterContaining(node1);
        assertEquals(mergedCluster, clusterManager.getClusterContaining(node2));
        assertEquals(2, mergedCluster.getNodeCount());
    }
    
    @Test
    void testDynamicRoutingUpdates() {
        // Set up test environment
        cluster1.addNode(node1);
        cluster1.addNode(node2);
        cluster2.addNode(node3);
        
        // Add clusters to manager
        clusterManager.addCluster(cluster1);
        clusterManager.addCluster(cluster2);
        
        // Add nodes to manager
        clusterManager.addNode(node1, cluster1);
        clusterManager.addNode(node2, cluster1);
        clusterManager.addNode(node3, cluster2);
        
        // Set cluster heads
        cluster1.promoteToClusterHead(node1);
        cluster2.promoteToClusterHead(node3);
        
        // Test route updates when topology changes
        clusterManager.updateRoutingTables();
        
        RoutingTable node1Table = node1.getRoutingTable();
        assertTrue(node1Table.hasRouteTo(node2.getId()));
        assertTrue(node1Table.hasRouteTo(node3.getId()));
    }
}
