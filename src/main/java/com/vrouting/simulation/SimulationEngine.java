package com.vrouting.simulation;

import com.vrouting.network.Node;
import com.vrouting.network.Cluster;

import java.io.Serializable;
import java.util.*;

public class SimulationEngine implements Serializable {
    private static final long serialVersionUID = 1L;

    private List<Node> nodes;
    private List<Cluster> clusters;
    private Map<Node, Cluster> nodeClusterMap;
    private Random random;
    private double failureProbability;
    private double recoveryProbability;
    private double messageGenerationProbability;
    private int simulationSteps;
    private List<SimulationMetrics> metrics;
    private int currentStep;
    private boolean simulationComplete;

    // Constructor
    public SimulationEngine(
            int nodeCount,
            double failureProbability,
            double recoveryProbability,
            double messageGenerationProbability,
            int simulationSteps
    ) {
        this.nodes = new ArrayList<>();
        this.clusters = new ArrayList<>();
        this.nodeClusterMap = new HashMap<>();
        this.random = new Random();
        this.failureProbability = failureProbability;
        this.recoveryProbability = recoveryProbability;
        this.messageGenerationProbability = messageGenerationProbability;
        this.simulationSteps = simulationSteps;
        this.metrics = new ArrayList<>();
        this.currentStep = 0;
        this.simulationComplete = false;

        initializeNetwork(nodeCount);
    }

    // Initialize network with nodes and clusters
    public void initializeNetwork(int nodeCount) {
        for (int i = 0; i < nodeCount; i++) {
            Node node = new Node("Node-" + i);
            nodes.add(node);
            // Assign to a cluster (for simplicity, all nodes in one cluster)
            assignToCluster(node);
        }
    }

    private void assignToCluster(Node node) {
        Cluster cluster;
        if (clusters.isEmpty()) {
            cluster = new Cluster("Cluster-1");
            clusters.add(cluster);
        } else {
            cluster = clusters.get(0); // Assign all nodes to the first cluster
        }
        cluster.addNode(node);
        nodeClusterMap.put(node, cluster);
    }

    // Start the simulation
    public void startSimulation() {
        while (currentStep < simulationSteps && !simulationComplete) {
            simulateStep();
            currentStep++;
        }
    }

    // Simulate a single step
    public void simulateStep() {
        // Simulate node failures and recoveries
        for (Node node : nodes) {
            if (node.isActive()) {
                if (random.nextDouble() < failureProbability) {
                    node.setActive(false);
                }
            } else {
                if (random.nextDouble() < recoveryProbability) {
                    node.setActive(true);
                }
            }
        }

        // Simulate message generation
        for (Node node : nodes) {
            if (node.isActive() && random.nextDouble() < messageGenerationProbability) {
                generateMessageFromNode(node);
            }
        }

        // Collect metrics
        collectMetrics();

        // Increment the current step
        currentStep++;

        // Check if simulation is complete
        if (currentStep >= simulationSteps) {
            simulationComplete = true;
        }
    }

    private void generateMessageFromNode(Node node) {
        // For simplicity, message is sent to a random active node
        List<Node> activeNodes = getActiveNodes();
        if (activeNodes.size() > 1) {
            Node targetNode;
            do {
                targetNode = activeNodes.get(random.nextInt(activeNodes.size()));
            } while (targetNode.equals(node));

            // Simulate message delivery (for simplicity, assume successful delivery)
            // In a real scenario, routing logic would be applied here
        }
    }

    private List<Node> getActiveNodes() {
        List<Node> activeNodes = new ArrayList<>();
        for (Node node : nodes) {
            if (node.isActive()) {
                activeNodes.add(node);
            }
        }
        return activeNodes;
    }

    // Collect simulation metrics
    private void collectMetrics() {
        int activeNodeCount = getActiveNodes().size();
        int failedNodeCount = nodes.size() - activeNodeCount;
        double messageDeliveryRate = calculateMessageDeliveryRate();
        double averageLatency = calculateAverageLatency();
        int totalMessagesSent = getTotalMessagesSent();
        int totalMessagesDelivered = getTotalMessagesDelivered();

        SimulationMetrics metric = new SimulationMetrics(
            currentStep,
            activeNodeCount,
            failedNodeCount,
            messageDeliveryRate,
            averageLatency,
            totalMessagesSent,
            totalMessagesDelivered
        );

        metrics.add(metric);
    }

    // Placeholder methods for metrics calculations
    private double calculateMessageDeliveryRate() {
        // Placeholder implementation
        return 0.0; // For MVP, return default value
    }

    private double calculateAverageLatency() {
        // Placeholder implementation
        return 0.0; // For MVP, return default value
    }

    private int getTotalMessagesSent() {
        // Placeholder implementation
        return 0; // For MVP, return default value
    }

    private int getTotalMessagesDelivered() {
        // Placeholder implementation
        return 0; // For MVP, return default value
    }

    // Getter methods
    public List<SimulationMetrics> getMetrics() {
        return metrics;
    }
    public List<Node> getNodes() {
        return nodes;
    }

    public int getCurrentStep() {
        return currentStep;
    }

    public boolean isSimulationComplete() {
        return simulationComplete;
    }

    public int getSimulationSteps() {
        return simulationSteps;
    }
}
