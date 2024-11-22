package com.vrouting.simulation;

import com.vrouting.network.*;
import java.util.*;

public class SimulationEngine {
    private final List<Node> nodes;
    private final List<Cluster> clusters;
    private final Map<Node, Cluster> nodeClusterMap;
    private final Random random;
    private final double failureProbability;
    private final double recoveryProbability;
    private final double messageGenerationProbability;
    private final int simulationSteps;
    private final List<SimulationMetrics> metrics;
    private final ClusterManager clusterManager;

    public SimulationEngine(
            int nodeCount,
            double failureProbability,
            double recoveryProbability,
            double messageGenerationProbability,
            int simulationSteps) {
        this.nodes = new ArrayList<>();
        this.clusters = new ArrayList<>();
        this.nodeClusterMap = new HashMap<>();
        this.random = new Random();
        this.failureProbability = failureProbability;
        this.recoveryProbability = recoveryProbability;
        this.messageGenerationProbability = messageGenerationProbability;
        this.simulationSteps = nodeCount; // Use nodeCount for network size
        this.metrics = new ArrayList<>();
        this.clusterManager = new ClusterManager();
    }

    public void initializeNetwork() {
        // Create nodes
        for (int i = 0; i < simulationSteps; i++) {
            Node node = new Node("Node-" + i);
            node.setClusterManager(clusterManager);
            nodes.add(node);
        }

        // Create initial clusters
        int clusterCount = Math.max(1, nodes.size() / 5); // Average 5 nodes per cluster
        for (int i = 0; i < clusterCount; i++) {
            Cluster cluster = new Cluster("Cluster-" + i);
            clusters.add(cluster);
            clusterManager.addCluster(cluster);
        }

        // Assign nodes to clusters
        for (Node node : nodes) {
            Cluster cluster = clusters.get(random.nextInt(clusters.size()));
            cluster.addNode(node);
            nodeClusterMap.put(node, cluster);
            clusterManager.addNode(node, cluster);
        }

        // Select cluster heads
        for (Cluster cluster : clusters) {
            List<Node> clusterNodes = new ArrayList<>(cluster.getNodes());
            if (!clusterNodes.isEmpty()) {
                Node head = clusterNodes.get(random.nextInt(clusterNodes.size()));
                cluster.promoteToClusterHead(head);
            }
        }

        // Initialize routing tables
        clusterManager.updateRoutingTables();
    }

    public void simulateNodeFailures() {
        for (Node node : nodes) {
            if (node.isActive() && random.nextDouble() < failureProbability) {
                node.markAsFailed();
            }
        }
    }

    public void simulateMassiveFailure(double failureRate) {
        int failureCount = (int) (nodes.size() * failureRate);
        List<Node> activeNodes = new ArrayList<>(nodes);
        Collections.shuffle(activeNodes);
        
        for (int i = 0; i < failureCount && i < activeNodes.size(); i++) {
            activeNodes.get(i).markAsFailed();
        }
    }

    public void runSimulationSteps(int steps) {
        for (int i = 0; i < steps; i++) {
            // Simulate node failures and recoveries
            simulateNodeFailures();
            simulateNodeRecoveries();
            
            // Simulate message generation and routing
            simulateMessageGeneration();
            
            // Update routing tables
            clusterManager.updateRoutingTables();
            
            // Collect metrics
            metrics.add(getCurrentMetrics());
        }
    }

    public void simulateNodeRecoveries() {
        for (Node node : nodes) {
            if (!node.isActive() && random.nextDouble() < recoveryProbability) {
                node.recover();
            }
        }
    }

    private void simulateMessageGeneration() {
        for (Node source : nodes) {
            if (source.isActive() && random.nextDouble() < messageGenerationProbability) {
                // Select random destination
                Node destination = nodes.get(random.nextInt(nodes.size()));
                if (source != destination) {
                    Message message = new Message(MessageType.DATA, "Test message");
                    clusterManager.propagateMessage(message, source);
                }
            }
        }
    }

    public SimulationMetrics getCurrentMetrics() {
        return new SimulationMetrics(nodes, clusters, nodeClusterMap);
    }
}
