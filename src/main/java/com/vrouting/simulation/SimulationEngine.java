package com.vrouting.simulation;

import com.vrouting.network.Node;
import com.vrouting.network.Cluster;

import java.io.Serializable;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Random;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private int messagesSent = 0;
    private int messagesDelivered = 0;
    private double totalLatency = 0;
    private static final Logger logger = LoggerFactory.getLogger(SimulationEngine.class);

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
        // Initialize the simulation but don't run any steps yet
        // Steps will be executed one at a time through simulateStep()
        logger.info("Simulation initialized with {} nodes and {} total steps", nodes.size(), simulationSteps);
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
        List<Node> activeNodes = getActiveNodes();
        if (activeNodes.size() > 1) {
            Node targetNode;
            do {
                targetNode = activeNodes.get(random.nextInt(activeNodes.size()));
            } while (targetNode.equals(node));

            messagesSent++;
            
            // Simulate message delivery with routing
            if (canDeliverMessage(node, targetNode)) {
                messagesDelivered++;
                totalLatency += calculateMessageLatency(node, targetNode);
            }
        }
    }

    private boolean canDeliverMessage(Node source, Node target) {
        // Simple simulation of message delivery success
        // In a real implementation, this would check routing tables and network conditions
        return source.isActive() && target.isActive() && random.nextDouble() > 0.1; // 90% success rate
    }

    private double calculateMessageLatency(Node source, Node target) {
        // Simple latency calculation based on number of hops
        // In a real implementation, this would use actual network topology
        return 1.0 + random.nextDouble() * 2.0; // Random latency between 1-3 ms
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

    private double calculateMessageDeliveryRate() {
        return messagesSent > 0 ? (double) messagesDelivered / messagesSent : 0.0;
    }

    private double calculateAverageLatency() {
        return messagesDelivered > 0 ? totalLatency / messagesDelivered : 0.0;
    }

    private int getTotalMessagesSent() {
        return messagesSent;
    }

    private int getTotalMessagesDelivered() {
        return messagesDelivered;
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
