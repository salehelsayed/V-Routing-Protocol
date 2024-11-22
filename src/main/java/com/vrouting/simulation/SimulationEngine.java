package com.vrouting.simulation;

import com.vrouting.network.socket.message.Message;
import com.vrouting.network.socket.message.MessageType;
import com.vrouting.network.Node;
import com.vrouting.network.socket.cluster.Cluster;
import com.vrouting.network.socket.cluster.ClusterManager;
import com.vrouting.simulation.SimulationMetrics;
import com.vrouting.simulation.SimulationStatus;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimulationEngine {
    private static final Logger logger = LoggerFactory.getLogger(SimulationEngine.class);
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
    private SimulationStatus status;
    private int currentStep;
    private final int totalSteps;

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
        // Create initial node for ClusterManager
        Node initialNode = new Node("Node-Initial");
        this.clusterManager = new ClusterManager(initialNode);
        initialNode.setClusterManager(this.clusterManager);
        this.nodes.add(initialNode); // Add initial node to the network
        this.status = new SimulationStatus();
        this.currentStep = 0;
        this.totalSteps = simulationSteps;
    }

    public void initializeNetwork() {
        // Create nodes
        for (int i = 0; i < simulationSteps; i++) {
            Node node = new Node("Node-" + i);
            node.setClusterManager(clusterManager);
            nodes.add(node);
        }

        // Establish connections between nodes
        establishNodeConnections();

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

    public void establishNodeConnections() {
        // Create a mesh network where each node is connected to approximately 20% of other nodes
        double connectionProbability = 0.2;
        
        for (int i = 0; i < nodes.size(); i++) {
            Node currentNode = nodes.get(i);
            // Try to connect with other nodes
            for (int j = i + 1; j < nodes.size(); j++) {
                Node otherNode = nodes.get(j);
                // Randomly decide whether to create a connection
                if (random.nextDouble() < connectionProbability) {
                    currentNode.addNeighbor(otherNode);
                }
            }
        }
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
                if (source != destination && destination.isActive()) {
                    Message message = new Message(source.getId(), destination.getId(), MessageType.DATA);
                    Set<Node> visited = new HashSet<>();
                    propagateMessage(message, visited, 10); // Assuming maxHops is 10
                }
            }
        }
    }

    public SimulationMetrics getCurrentMetrics() {
        return new SimulationMetrics(nodes, clusters, nodeClusterMap);
    }

    public void step() {
        if (!status.isRunning()) {
            return;
        }

        currentStep++;
        int completionPercentage = (int) ((double) currentStep / totalSteps * 100);
        status.setCompletionPercentage(Math.min(completionPercentage, 100));
        status.setCurrentState("Processing Step " + currentStep + " of " + totalSteps);
        status.setMessage("Simulating network behavior...");

        logger.info("Step {}/{} ({}%): Starting simulation step", currentStep, totalSteps, completionPercentage);
        logger.debug("Active nodes: {}, Total nodes: {}", 
            nodes.stream().filter(Node::isActive).count(), nodes.size());

        // Existing step logic
        simulateNodeFailures();
        simulateNodeRecoveries();
        updateClusters();
        simulateMessagePropagation();
        updateMetrics();

        logger.info("Step {}/{}: Completed simulation step", currentStep, totalSteps);
        SimulationMetrics currentMetrics = metrics.get(metrics.size() - 1);
        logger.debug("Network state - Active nodes: {}/{}, Clusters: {}, Message delivery rate: {}", 
            currentMetrics.getActiveNodeCount(),
            currentMetrics.getNodeCount(),
            currentMetrics.getClusterCount(),
            String.format("%.2f%%", currentMetrics.getMessageDeliveryRate() * 100));

        if (currentStep >= totalSteps) {
            status.setRunning(false);
            status.setCurrentState("COMPLETED");
            status.setMessage("Simulation completed successfully");
            status.setCompletionPercentage(100);
            logger.info("Simulation completed successfully after {} steps", totalSteps);
        }
    }

    public void start() {
        status.setRunning(true);
        status.setCurrentState("INITIALIZING");
        status.setMessage("Setting up network...");
        status.setCompletionPercentage(0);
        currentStep = 0;
        
        initializeNetwork();
        
        status.setCurrentState("RUNNING");
        status.setMessage("Simulation in progress");
    }

    public void stop() {
        status.setRunning(false);
        status.setCurrentState("STOPPED");
        status.setMessage("Simulation stopped by user");
    }

    public SimulationStatus getStatus() {
        return status;
    }

    public List<Node> getNodes() {
        return new ArrayList<>(nodes);
    }

    private void updateClusters() {
        logger.info("Starting cluster update - Current clusters: {}", clusters.size());
        clusters.clear();
        nodeClusterMap.clear();

        // Group nodes into clusters based on connectivity
        Set<Node> unassignedNodes = new HashSet<>(nodes);
        int initialUnassigned = unassignedNodes.size();
        
        while (!unassignedNodes.isEmpty()) {
            Node node = unassignedNodes.iterator().next();
            if (!node.isActive()) {
                unassignedNodes.remove(node);
                continue;
            }

            Set<Node> clusterNodes = new HashSet<>();
            Queue<Node> queue = new LinkedList<>();
            queue.add(node);

            while (!queue.isEmpty()) {
                Node current = queue.poll();
                if (clusterNodes.add(current)) {
                    unassignedNodes.remove(current);
                    for (Node neighbor : current.getNeighbors()) {
                        if (neighbor.isActive() && !clusterNodes.contains(neighbor)) {
                            queue.add(neighbor);
                        }
                    }
                }
            }

            if (!clusterNodes.isEmpty()) {
                Cluster newCluster = new Cluster("Cluster-" + clusters.size());
                clusterNodes.forEach(newCluster::addNode);
                clusters.add(newCluster);
                
                // Select cluster head
                List<Node> activeNodes = clusterNodes.stream()
                    .filter(Node::isActive)
                    .toList();
                if (!activeNodes.isEmpty()) {
                    Node head = activeNodes.get(random.nextInt(activeNodes.size()));
                    newCluster.promoteToClusterHead(head);
                    logger.debug("Cluster {} formed with {} nodes, head: {}", 
                        newCluster.getId(), clusterNodes.size(), head.getId());
                }

                // Update node-cluster mapping
                for (Node n : clusterNodes) {
                    nodeClusterMap.put(n, newCluster);
                }
            }
        }
        
        // Update routing tables after cluster changes
        clusterManager.updateRoutingTables();
        logger.info("Clusters updated: {} clusters formed from {} initial unassigned nodes", 
            clusters.size(), initialUnassigned);
        logger.debug("Cluster sizes: {}", 
            clusters.stream()
                .map((Cluster c) -> c.getId() + ":" + c.getNodes().size())
                .reduce((a, b) -> a + ", " + b)
                .orElse("none"));
    }

    private void simulateMessagePropagation() {
        for (Node source : nodes) {
            if (!source.isActive()) continue;

            for (Node target : nodes) {
                if (source == target || !target.isActive()) continue;

                double messageRate = random.nextDouble();
                if (messageRate < messageGenerationProbability) {
                    Message message = new Message(source.getId(), target.getId(), MessageType.DATA);
                    Set<Node> visited = new HashSet<>();
                    propagateMessage(message, visited, 10);
                }
            }
        }
    }

    private boolean propagateMessage(Message message, Set<Node> visited, int remainingHops) {
        Node sourceNode = nodes.stream()
                .filter(n -> n.getId().equals(message.getSource()))
                .findFirst()
                .orElse(null);
        Node destinationNode = nodes.stream()
                .filter(n -> n.getId().equals(message.getDestinationNodeId()))
                .findFirst()
                .orElse(null);
                
        logger.debug("Message propagation: [{}->{}] {} hops remaining, visited {} nodes", 
            sourceNode != null ? sourceNode.getId() : "unknown",
            destinationNode != null ? destinationNode.getId() : "unknown",
            remainingHops,
            visited.size());

        if (remainingHops <= 0) {
            logger.warn("Message dropped: [{}->{}] Exceeded maximum hop count", 
                message.getSource(), message.getDestinationNodeId());
            return false;
        }

        Node currentNode = nodes.stream()
                .filter(n -> n.getId().equals(message.getRouteHistory().isEmpty() ? message.getSource() : message.getRouteHistory().get(message.getRouteHistory().size() - 1)))
                .findFirst()
                .orElse(null);

        if (currentNode == null || !currentNode.isActive()) return false;
        if (!visited.add(currentNode)) return false;

        if (currentNode.getId().equals(message.getDestinationNodeId())) {
            logger.debug("Message reached destination: {}", message.getDestinationNodeId());
            return true;
        }

        for (Node neighbor : currentNode.getNeighbors()) {
            if (neighbor.isActive() && !visited.contains(neighbor)) {
                message.addHop(neighbor.getId());
                if (propagateMessage(message, visited, remainingHops - 1)) {
                    return true;
                }
                message.removeLastHop();
            }
        }

        return false;
    }

    private void updateMetrics() {
        SimulationMetrics currentMetrics = getCurrentMetrics();
        metrics.add(currentMetrics);
    }
}
