package com.vrouting.simulation;

import com.vrouting.network.*;
import java.util.*;

public class SimulationMetrics {
    private final int nodeCount;
    private final int activeNodeCount;
    private final int failedNodeCount;
    private final int clusterCount;
    private final double messageDeliveryRate;
    private final double averageLatency;
    private final double tunnelEstablishmentRate;
    private final double secureMessageDeliveryRate;
    private final double averageHopCount;
    private final double routingTableAccuracy;

    public SimulationMetrics(List<Node> nodes, List<Cluster> clusters, Map<Node, Cluster> nodeClusterMap) {
        this.nodeCount = nodes.size();
        this.activeNodeCount = (int) nodes.stream().filter(Node::isActive).count();
        this.failedNodeCount = nodeCount - activeNodeCount;
        this.clusterCount = clusters.size();
        
        // In a real implementation, these would be calculated based on actual metrics
        this.messageDeliveryRate = 0.95; // Example value
        this.averageLatency = 100; // Example value in ms
        this.tunnelEstablishmentRate = 0.98; // Example value
        this.secureMessageDeliveryRate = 0.99; // Example value
        this.averageHopCount = 2.5; // Example value
        this.routingTableAccuracy = 0.97; // Example value
    }

    public int getNodeCount() {
        return nodeCount;
    }

    public int getActiveNodeCount() {
        return activeNodeCount;
    }

    public int getFailedNodeCount() {
        return failedNodeCount;
    }

    public int getClusterCount() {
        return clusterCount;
    }

    public double getMessageDeliveryRate() {
        return messageDeliveryRate;
    }

    public double getAverageLatency() {
        return averageLatency;
    }

    public double getTunnelEstablishmentRate() {
        return tunnelEstablishmentRate;
    }

    public double getSecureMessageDeliveryRate() {
        return secureMessageDeliveryRate;
    }

    public double getAverageHopCount() {
        return averageHopCount;
    }

    public double getRoutingTableAccuracy() {
        return routingTableAccuracy;
    }
}
