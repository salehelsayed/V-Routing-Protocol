package com.vrouting.network;

import java.util.*;

public class ClusterManager {
    private final Map<String, Cluster> clusters;
    private final Map<String, Node> nodes;
    private final Map<String, Cluster> nodeClusterMap;

    public ClusterManager() {
        this.clusters = new HashMap<>();
        this.nodes = new HashMap<>();
        this.nodeClusterMap = new HashMap<>();
    }

    public void addCluster(Cluster cluster) {
        clusters.put(cluster.getId(), cluster);
    }

    public void addNode(Node node, Cluster cluster) {
        nodes.put(node.getId(), node);
        cluster.addNode(node);
        nodeClusterMap.put(node.getId(), cluster);
    }

    public void mergeClusters(Cluster cluster1, Cluster cluster2) {
        // Create a new merged cluster
        Cluster mergedCluster = new Cluster("Merged-" + cluster1.getName() + "-" + cluster2.getName());
        
        // Move all nodes to the new cluster
        for (Node node : cluster1.getNodes()) {
            mergedCluster.addNode(node);
            nodeClusterMap.put(node.getId(), mergedCluster);
        }
        for (Node node : cluster2.getNodes()) {
            mergedCluster.addNode(node);
            nodeClusterMap.put(node.getId(), mergedCluster);
        }

        // Remove old clusters
        clusters.remove(cluster1.getId());
        clusters.remove(cluster2.getId());
        
        // Add new cluster
        clusters.put(mergedCluster.getId(), mergedCluster);
    }

    public Cluster getClusterContaining(Node node) {
        return nodeClusterMap.get(node.getId());
    }

    public void updateRoutingTables() {
        // Initialize routing tables for all nodes
        for (Node node : nodes.values()) {
            node.getRoutingTable().clear();
            
            // Add direct routes to nodes in the same cluster
            Cluster nodeCluster = getClusterContaining(node);
            if (nodeCluster != null) {
                for (Node neighbor : nodeCluster.getNodes()) {
                    if (neighbor != node) {
                        node.updateRoutingTable(new RoutingEntry(neighbor.getId(), neighbor.getId(), 1));
                    }
                }
            }
            
            // Add routes to other clusters through cluster heads
            for (Cluster cluster : clusters.values()) {
                if (cluster != nodeCluster) {
                    Node clusterHead = cluster.getClusterHead();
                    if (clusterHead != null) {
                        // Route to cluster head
                        node.updateRoutingTable(new RoutingEntry(clusterHead.getId(), 
                            nodeCluster.getClusterHead().getId(), 2));
                        
                        // Routes to nodes in other cluster
                        for (Node remoteNode : cluster.getNodes()) {
                            if (remoteNode != clusterHead) {
                                node.updateRoutingTable(new RoutingEntry(remoteNode.getId(), 
                                    clusterHead.getId(), 3));
                            }
                        }
                    }
                }
            }
        }
    }

    public void propagateMessage(Message message, Node source) {
        // Find source cluster
        Cluster sourceCluster = getClusterContaining(source);
        if (sourceCluster == null) {
            return;
        }

        // If source is not cluster head, send to cluster head first
        if (!source.isClusterHead()) {
            Node clusterHead = sourceCluster.getClusterHead();
            if (clusterHead != null) {
                // Forward to cluster head
                // In real implementation, this would use actual network communication
            }
        }

        // Propagate to other clusters through secure tunnels
        for (Cluster targetCluster : clusters.values()) {
            if (targetCluster != sourceCluster) {
                sourceCluster.sendMessageToCluster(targetCluster, message);
            }
        }
    }
}
