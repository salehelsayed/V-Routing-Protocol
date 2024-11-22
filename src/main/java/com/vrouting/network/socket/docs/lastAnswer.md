Thank you for the comprehensive clarification! Based on your detailed feedback, we'll refine and enhance the architecture to better align with your project's requirements. This includes:

Implementing an interface for Eigenvector Centrality Calculation with an implementation using JGraphT.
Refining Cluster Head Selection Mechanism to be automatic based on centrality scores and external configuration flags, eliminating the need for explicit elections.
Defining Comprehensive Message Types and outlining how nodes react to each type to maintain network health.
Implementing a Three-Phase Heartbeat Mechanism with negative exponential decay intervals to facilitate network discovery and stability.
Establishing Dynamic Routing Table Management with configurable route selection criteria based on multiple KPIs.
Detailing Secure Tunnel Management with dual security aspects, leveraging SSH keys for secure communications.
We'll structure the enhanced architecture as follows:

1. Introduction
Your project aims to develop a Java-based ad-hoc networking package leveraging Wi-Fi Direct, emphasizing multi-hop capabilities, dynamic cluster management based on node centrality, and robust secure communication. This architecture ensures nodes autonomously manage their roles, maintain network health through intelligent heartbeat mechanisms, and handle secure tunnels effectively.

2. Enhanced Package Structure
To accommodate the additional requirements, here's an updated and more granular package structure:

com.vrouting.network
├── adhoc
│   ├── Node.java
│   ├── RegularNode.java
│   ├── ClusterHeadNode.java
│   ├── Cluster.java
│   ├── ClusterManager.java
│   ├── HeartbeatManager.java
│   ├── RoutingManager.java
│   ├── MultiHopManager.java
│   ├── Message.java
│   ├── MessageType.java
│   └── CentralityCalculator.java
├── communication
│   ├── Connection.java
│   ├── ConnectionManager.java
│   ├── SecureConnection.java
│   └── SocketHandler.java
├── security
│   ├── EncryptionManager.java
│   ├── KeyManager.java
│   └── SecureTunnel.java
├── metrics
│   ├── ConnectionMetrics.java
│   └── NetworkMetrics.java
├── utils
│   ├── Logger.java
│   ├── Configuration.java
│   └── Utils.java
├── centrality
│   ├── CentralityCalculatorInterface.java
│   └── JGraphTCentralityCalculator.java
└── testing
    ├── TestHarness.java
    ├── NetworkSimulator.java
    └── TestCases.java
3. Detailed Class Descriptions
3.1. Centrality Calculation
3.1.1. com.vrouting.network.centrality.CentralityCalculatorInterface
Purpose:

Defines the contract for calculating eigenvector centrality.
Implementation:

package com.vrouting.network.centrality;

import com.vrouting.network.adhoc.AdjacencyMatrix;

public interface CentralityCalculatorInterface {
    double calculateEigenvectorCentrality(AdjacencyMatrix adjMatrix, String nodeId);
    String findHighestCentralityNode(AdjacencyMatrix adjMatrix, List<String> nodeIds, List<Boolean> eligibilityFlags);
}
3.1.2. com.vrouting.network.centrality.JGraphTCentralityCalculator
Purpose:

Implements CentralityCalculatorInterface using the JGraphT library for eigenvector centrality calculation.
Implementation:

package com.vrouting.network.centrality;

import com.vrouting.network.adhoc.AdjacencyMatrix;
import org.jgrapht.Graph;
import org.jgrapht.alg.scoring.EigenvectorCentrality;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;

import java.util.List;
import java.util.Map;

public class JGraphTCentralityCalculator implements CentralityCalculatorInterface {

    @Override
    public double calculateEigenvectorCentrality(AdjacencyMatrix adjMatrix, String nodeId) {
        Graph<String, DefaultEdge> graph = buildGraph(adjMatrix);
        EigenvectorCentrality<String, DefaultEdge> centrality = new EigenvectorCentrality<>(graph);
        centrality.initialize();
        return centrality.getVertexScore(nodeId);
    }

    @Override
    public String findHighestCentralityNode(AdjacencyMatrix adjMatrix, List<String> nodeIds, List<Boolean> eligibilityFlags) {
        Graph<String, DefaultEdge> graph = buildGraph(adjMatrix);
        EigenvectorCentrality<String, DefaultEdge> centrality = new EigenvectorCentrality<>(graph);
        centrality.initialize();

        double maxCentrality = Double.MIN_VALUE;
        String highestNode = null;

        for (int i = 0; i < nodeIds.size(); i++) {
            String nodeId = nodeIds.get(i);
            if (eligibilityFlags.get(i)) {
                double score = centrality.getVertexScore(nodeId);
                if (score > maxCentrality) {
                    maxCentrality = score;
                    highestNode = nodeId;
                }
            }
        }

        return highestNode;
    }

    private Graph<String, DefaultEdge> buildGraph(AdjacencyMatrix adjMatrix) {
        Graph<String, DefaultEdge> graph = new SimpleGraph<>(DefaultEdge.class);
        Map<String, Integer> nodeIndexMap = adjMatrix.getNodeIndexMap();
        Map<Integer, String> indexNodeMap = adjMatrix.getIndexNodeMap();

        // Add vertices
        for (String nodeId : nodeIndexMap.keySet()) {
            graph.addVertex(nodeId);
        }

        // Add edges
        int[][] matrix = adjMatrix.getMatrix();
        for (int i = 0; i < matrix.length; i++) {
            for (int j = i + 1; j < matrix.length; j++) {
                if (matrix[i][j] == 1) {
                    String nodeA = indexNodeMap.get(i);
                    String nodeB = indexNodeMap.get(j);
                    graph.addEdge(nodeA, nodeB);
                }
            }
        }

        return graph;
    }
}
3.2. Node Hierarchy and Roles
3.2.1. com.vrouting.network.adhoc.Node (Superclass)
Responsibilities:

Represents a generic node in the ad-hoc network.
Manages node identification, state, and common functionalities shared by all node types.
Maintains a peer directory with routing history and connection metrics.
Decides whether to become a cluster head based on centrality and configuration.
Key Fields:

public abstract class Node {
    protected String nodeId;
    protected String address;
    protected NodeState state;
    protected List<Connection> connections;
    protected Cluster currentCluster;
    protected ConnectionManager connectionManager;
    protected HeartbeatManager heartbeatManager;
    protected ClusterManager clusterManager;
    protected RoutingManager routingManager;
    protected EncryptionManager encryptionManager;
    protected NetworkMetrics networkMetrics;
    protected PeerDirectory peerDirectory; // Tracks peers, last seen, routing history
    protected boolean isAllowedClusterHead; // Flag from configuration
    protected double centralityScore; // Eigenvector centrality score
    protected CentralityCalculatorInterface centralityCalculator; // Interface for centrality
    protected Configuration config; // Configuration settings

    // Constructor
    public Node(String nodeId, String address, boolean isAllowedClusterHead, Configuration config, CentralityCalculatorInterface centralityCalculator) {
        this.nodeId = nodeId;
        this.address = address;
        this.state = NodeState.INITIALIZING;
        this.connections = new ArrayList<>();
        this.connectionManager = new ConnectionManager(this);
        this.heartbeatManager = new HeartbeatManager(this);
        this.clusterManager = new ClusterManager(this);
        this.routingManager = new RoutingManager(this);
        this.encryptionManager = new EncryptionManager(new KeyManager());
        this.networkMetrics = new NetworkMetrics();
        this.peerDirectory = new PeerDirectory();
        this.isAllowedClusterHead = isAllowedClusterHead;
        this.centralityScore = 0.0;
        this.centralityCalculator = centralityCalculator;
        this.config = config;
    }

    // Abstract methods for node-specific behavior
    public abstract void initialize();
    public abstract void shutdown();

    // Common methods
    public String getNodeId() { return nodeId; }
    public String getAddress() { return address; }
    public NodeState getState() { return state; }
    public void setState(NodeState state) { this.state = state; }

    public void addConnection(Connection connection) {
        connections.add(connection);
        connectionManager.addConnection(connection);
    }

    public void removeConnection(Connection connection) {
        connections.remove(connection);
        connectionManager.removeConnection(connection);
    }

    public List<Connection> getConnections() {
        return connections;
    }

    public Cluster getCurrentCluster() { return currentCluster; }

    public void joinCluster(Cluster cluster) {
        this.currentCluster = cluster;
        cluster.addMember(this);
        Logger.logInfo("Node " + nodeId + " joined cluster " + cluster.getClusterHead().getNodeId());
    }

    public void leaveCluster() {
        if (currentCluster != null) {
            currentCluster.removeMember(this);
            Logger.logInfo("Node " + nodeId + " left cluster " + currentCluster.getClusterHead().getNodeId());
            this.currentCluster = null;
        }
    }

    public void sendHeartbeat() {
        heartbeatManager.sendHeartbeat();
    }

    public void receiveHeartbeat(HeartbeatMessage message) {
        heartbeatManager.receiveHeartbeat(message);
    }

    public void sendMessage(Message message) {
        routingManager.routeMessage(message);
    }

    public void receiveMessage(Message message) {
        // Decrypt the message payload
        byte[] decryptedPayload = encryptionManager.decrypt(message.getPayload());
        message.setPayload(decryptedPayload);
        // Update routing history
        message.addToRoutingHistory(this.nodeId);
        // Increment hop count
        message.incrementHopCount();
        // Handle the decrypted message
        routingManager.handleIncomingMessage(message);
    }

    // Method to evaluate and decide to become a cluster head
    public void evaluateAndDecideClusterHead() {
        // Only proceed if allowed to be a cluster head
        if (!isAllowedClusterHead) {
            return;
        }

        // Calculate centrality score using the centrality calculator
        AdjacencyMatrix adjMatrix = peerDirectory.getAdjacencyMatrix();
        centralityScore = centralityCalculator.calculateEigenvectorCentrality(adjMatrix, nodeId);
        Logger.logInfo("Node " + nodeId + " calculated centrality score: " + centralityScore);

        // Check if centrality exceeds threshold from configuration
        double threshold = config.getCentralityThreshold();
        if (centralityScore >= threshold && currentCluster == null) {
            // Promote to cluster head
            promoteToClusterHead();
        }
    }

    private void promoteToClusterHead() {
        // Transition to ClusterHeadNode behavior
        // This could involve casting or creating a new ClusterHeadNode instance
        // For simplicity, assuming Node can handle ClusterHead responsibilities
        Logger.logInfo("Node " + nodeId + " promoting itself to Cluster Head.");
        // Broadcast cluster head announcement
        Message announcement = new Message(this.nodeId, "ALL", "I am now a Cluster Head".getBytes(), MessageType.CLUSTER_HEAD_ANNOUNCEMENT);
        sendMessage(announcement);
    }
}
3.2.2. com.vrouting.network.adhoc.RegularNode (Subclass of Node)
Responsibilities:

Represents a standard node without cluster head responsibilities.
Handles regular message routing and participation in cluster activities.
Key Fields:

public class RegularNode extends Node {
    public RegularNode(String nodeId, String address, boolean isAllowedClusterHead, Configuration config, CentralityCalculatorInterface centralityCalculator) {
        super(nodeId, address, isAllowedClusterHead, config, centralityCalculator);
    }

    @Override
    public void initialize() {
        heartbeatManager.startHeartbeat();
        clusterManager.discoverClusters();
        routingManager.initializeRouting();
        Logger.logInfo("RegularNode " + nodeId + " initialized.");
    }

    @Override
    public void shutdown() {
        heartbeatManager.stopHeartbeat();
        connectionManager.closeAllConnections();
        Logger.logInfo("RegularNode " + nodeId + " shut down.");
    }
}
3.2.3. com.vrouting.network.adhoc.ClusterHeadNode (Subclass of Node)
Responsibilities:

Represents a cluster head with additional responsibilities like managing intra-cluster routing and secure tunnels.
Maintains a routing table for all nodes within the cluster.
Manages secure tunnel handshakes and communications with other cluster heads.
Key Fields:

public class ClusterHeadNode extends Node {
    private List<String> peerClusterHeads;
    private List<SecureTunnel> secureTunnels;
    private RoutingTable intraClusterRoutingTable;

    public ClusterHeadNode(String nodeId, String address, boolean isAllowedClusterHead, Configuration config, CentralityCalculatorInterface centralityCalculator) {
        super(nodeId, address, isAllowedClusterHead, config, centralityCalculator);
        this.peerClusterHeads = new ArrayList<>();
        this.secureTunnels = new ArrayList<>();
        this.intraClusterRoutingTable = new RoutingTable();
    }

    @Override
    public void initialize() {
        heartbeatManager.startHeartbeat();
        clusterManager.discoverClusters();
        routingManager.initializeRouting();
        Logger.logInfo("ClusterHeadNode " + nodeId + " initialized as Cluster Head.");
    }

    @Override
    public void shutdown() {
        heartbeatManager.stopHeartbeat();
        connectionManager.closeAllConnections();
        closeAllSecureTunnels();
        Logger.logInfo("ClusterHeadNode " + nodeId + " shut down.");
    }

    public void broadcastClusterHeadAnnouncement() {
        Message announcement = new Message(this.nodeId, "ALL", "I am now a Cluster Head".getBytes(), MessageType.CLUSTER_HEAD_ANNOUNCEMENT);
        sendMessage(announcement);
    }

    public void manageIntraClusterRouting(Message message) {
        // Implement intra-cluster routing logic using the intraClusterRoutingTable
        // For example, update routing table and forward messages within the cluster
    }

    public void establishSecureTunnel(ClusterHeadNode peerHead) {
        SecureTunnel tunnel = new SecureTunnel(this, peerHead, encryptionManager);
        secureTunnels.add(tunnel);
        tunnel.establish();
        Logger.logInfo("Secure tunnel established between " + this.nodeId + " and " + peerHead.nodeId);
    }

    public void closeAllSecureTunnels() {
        for (SecureTunnel tunnel : secureTunnels) {
            tunnel.close();
        }
        secureTunnels.clear();
    }

    public void maintainRoutingTable() {
        // Maintain routing table for available paths within the cluster
        // This could involve periodic updates, responding to routing changes, etc.
    }
}
3.3. Cluster Management
3.3.1. com.vrouting.network.adhoc.Cluster
Responsibilities:

Represents a cluster of nodes.
Manages cluster membership and intra-cluster communications.
Handles cluster head announcements and routing updates.
Key Fields:

public class Cluster {
    private ClusterHeadNode clusterHead;
    private List<Node> members;
    private List<Connection> intraClusterConnections;

    public Cluster(ClusterHeadNode clusterHead) {
        this.clusterHead = clusterHead;
        this.members = new ArrayList<>();
        this.intraClusterConnections = new ArrayList<>();
        addMember(clusterHead); // Cluster head is a member
    }

    public void addMember(Node node) {
        members.add(node);
        node.joinCluster(this);
        Logger.logInfo("Node " + node.getNodeId() + " added to cluster headed by " + clusterHead.getNodeId());
    }

    public void removeMember(Node node) {
        members.remove(node);
        node.leaveCluster();
        Logger.logInfo("Node " + node.getNodeId() + " removed from cluster headed by " + clusterHead.getNodeId());
    }

    public List<Node> getMembers() {
        return members;
    }

    public ClusterHeadNode getClusterHead() {
        return clusterHead;
    }

    public void setClusterHead(ClusterHeadNode newHead) {
        this.clusterHead = newHead;
        broadcastNewClusterHead();
        Logger.logInfo("Cluster head updated to " + newHead.getNodeId());
    }

    private void broadcastNewClusterHead() {
        clusterHead.broadcastClusterHeadAnnouncement();
    }

    public void routeMessage(Message message) {
        clusterHead.manageIntraClusterRouting(message);
    }
}
3.3.2. com.vrouting.network.adhoc.ClusterManager
Responsibilities:

Manages the formation, maintenance, and dynamic changes of clusters.
Handles cluster head announcements and integrates new cluster heads.
Maintains a registry of clusters and maps nodes to their respective clusters.
Key Fields:

public class ClusterManager {
    private Node node;
    private List<Cluster> clusters;
    private Map<String, Cluster> nodeToClusterMap;

    public ClusterManager(Node node) {
        this.node = node;
        this.clusters = new ArrayList<>();
        this.nodeToClusterMap = new HashMap<>();
    }

    public void discoverClusters() {
        // Implement node discovery logic
        // Send discovery messages and await responses
        // Placeholder for discovery implementation
    }

    public void integrateClusterHead(String clusterHeadId) {
        // Find the ClusterHeadNode from PeerDirectory
        PeerInfo info = node.peerDirectory.getPeer(clusterHeadId);
        if (info != null && info.isClusterHead()) {
            ClusterHeadNode clusterHead = (ClusterHeadNode) getNodeById(clusterHeadId);
            if (clusterHead != null) {
                Cluster cluster = new Cluster(clusterHead);
                clusters.add(cluster);
                nodeToClusterMap.put(clusterHeadId, cluster);
                Logger.logInfo("Integrated cluster headed by " + clusterHeadId);
            }
        }
    }

    public void handleClusterHeadAnnouncement(String clusterHeadId) {
        // Handle incoming cluster head announcements
        integrateClusterHead(clusterHeadId);
    }

    public void handleNodeJoin(Node newNode) {
        // Determine which cluster the node should join based on proximity or centrality
        Cluster targetCluster = findTargetCluster(newNode);
        if (targetCluster != null) {
            targetCluster.addMember(newNode);
            nodeToClusterMap.put(newNode.getNodeId(), targetCluster);
        } else {
            // Form a new cluster if no suitable cluster exists and node is allowed to be a cluster head
            if (newNode.isAllowedClusterHead) {
                Cluster cluster = new Cluster((ClusterHeadNode) newNode);
                clusters.add(cluster);
                nodeToClusterMap.put(newNode.getNodeId(), cluster);
                Logger.logInfo("Formed new cluster with head " + newNode.getNodeId());
            } else {
                Logger.logInfo("Node " + newNode.getNodeId() + " is a RegularNode and not forming a new cluster.");
            }
        }
    }

    public void handleNodeLeave(Node leavingNode) {
        Cluster cluster = nodeToClusterMap.get(leavingNode.getNodeId());
        if (cluster != null) {
            cluster.removeMember(leavingNode);
            nodeToClusterMap.remove(leavingNode.getNodeId());
            if (cluster.getClusterHead().equals(leavingNode)) {
                // Handle cluster head departure
                Logger.logInfo("Cluster head " + leavingNode.getNodeId() + " has left. No automatic election triggered.");
                // Since no election is needed, other cluster heads may update routing tables or establish new tunnels
            }
        }
    }

    public void updateRoutingTables() {
        for (Cluster cluster : clusters) {
            cluster.getClusterHead().maintainRoutingTable();
        }
    }

    private Cluster findTargetCluster(Node node) {
        // Implement logic to find the best cluster for the node to join
        // For example, the cluster with the highest centrality or most recent connections
        return null; // Placeholder
    }

    private Node getNodeById(String nodeId) {
        // Implement logic to retrieve a Node instance by its ID
        // This could involve querying a node registry or peer directory
        return null; // Placeholder
    }
}
3.4. Heartbeat Mechanism
3.4.1. com.vrouting.network.adhoc.HeartbeatManager
Responsibilities:

Manages the heartbeat mechanism to monitor node status and network connectivity.
Implements a three-phase heartbeat strategy with negative exponential decay intervals.
Facilitates network discovery and stability assessments.
Implementation:

public class HeartbeatManager {
    private Node node;
    private ScheduledExecutorService scheduler;
    private List<Long> heartbeatIntervals; // Negative exponential intervals
    private int currentPhase;
    private boolean running;

    public HeartbeatManager(Node node) {
        this.node = node;
        this.scheduler = Executors.newSingleThreadScheduledExecutor();
        this.heartbeatIntervals = Arrays.asList(1000L, 4000L, 18000L); // Example intervals in ms
        this.currentPhase = 0;
        this.running = false;
    }

    public void startHeartbeat() {
        if (!running) {
            running = true;
            scheduleHeartbeat();
            Logger.logInfo("Heartbeat started for node " + node.getNodeId());
        }
    }

    public void stopHeartbeat() {
        if (running) {
            scheduler.shutdownNow();
            running = false;
            Logger.logInfo("Heartbeat stopped for node " + node.getNodeId());
        }
    }

    private void scheduleHeartbeat() {
        if (currentPhase < heartbeatIntervals.size()) {
            long interval = heartbeatIntervals.get(currentPhase);
            scheduler.schedule(this::sendHeartbeat, interval, TimeUnit.MILLISECONDS);
        }
    }

    public void sendHeartbeat() {
        if (!running) return;

        HeartbeatMessage heartbeat = new HeartbeatMessage(node.getNodeId(), System.currentTimeMillis(), node.getState());
        Message heartbeatMsg = new Message(node.getNodeId(), "ALL", heartbeat.serialize(), MessageType.HEARTBEAT);
        node.sendMessage(heartbeatMsg);
        Logger.logDebug("Heartbeat sent by node " + node.getNodeId());

        // Schedule next heartbeat
        if (currentPhase < heartbeatIntervals.size() - 1) {
            currentPhase++;
        }
        scheduleHeartbeat();
    }

    public void receiveHeartbeat(HeartbeatMessage message) {
        // Update PeerDirectory with the heartbeat information
        node.peerDirectory.updatePeerStatus(message.getSenderNodeId(), message.getTimestamp(), message.getNodeState());
        Logger.logDebug("Heartbeat received from node " + message.getSenderNodeId());

        // Respond if the sender is a cluster head
        if (node.peerDirectory.getPeer(message.getSenderNodeId()).isClusterHead()) {
            // Optionally update routing tables or establish secure tunnels
            // This depends on your specific implementation
        }

        // Stability assessment can be implemented here to decide if heartbeats can continue decaying
    }
}
3.5. Routing Management
3.5.1. com.vrouting.network.adhoc.RoutingManager
Responsibilities:

Manages routing within and between clusters using Dynamic Source Routing (DSR) and hybrid protocols.
Maintains and updates the routing table based on network changes and node interactions.
Implements dynamic route selection based on multiple KPIs.
Implementation:

public class RoutingManager {
    private Node node;
    private RoutingTable routingTable;
    private ClusterManager clusterManager;

    public RoutingManager(Node node) {
        this.node = node;
        this.routingTable = new RoutingTable();
        this.clusterManager = node.clusterManager;
    }

    public void initializeRouting() {
        // Initialize routing protocols, e.g., DSR
        Logger.logInfo("RoutingManager initialized for node " + node.getNodeId());
    }

    public void discoverRoute(Message message) {
        // Implement DSR route discovery logic
        Logger.logInfo("Initiating route discovery for message to " + message.getDestinationNodeId());
        // Create and send ROUTE_REQUEST message
        Message routeRequest = new Message(node.getNodeId(), message.getDestinationNodeId(), "Route Request".getBytes(), MessageType.ROUTE_REQUEST);
        sendRouteRequest(routeRequest);
    }

    private void sendRouteRequest(Message routeRequest) {
        // Broadcast ROUTE_REQUEST within the cluster
        node.sendMessage(routeRequest);
    }

    public void maintainRoutes() {
        // Periodically check and update routes
        // Placeholder for route maintenance
    }

    public Route selectOptimalRoute(String destinationNodeId) {
        // Select the best route based on routing table and metrics
        // Implement dynamic selection based on multiple KPIs
        List<Route> possibleRoutes = routingTable.getRoutes(destinationNodeId);
        if (possibleRoutes.isEmpty()) {
            return null;
        }

        // Example: Weighted average of KPIs (latency, packet loss, hop count)
        double bestScore = Double.MAX_VALUE;
        Route optimalRoute = null;
        for (Route route : possibleRoutes) {
            double score = calculateRouteScore(route);
            if (score < bestScore) {
                bestScore = score;
                optimalRoute = route;
            }
        }
        return optimalRoute;
    }

    private double calculateRouteScore(Route route) {
        // Fetch KPIs from connections involved in the route
        double totalLatency = 0.0;
        double totalPacketLoss = 0.0;
        int hopCount = route.getHops().size();

        for (String hopNodeId : route.getHops()) {
            PeerInfo peer = node.peerDirectory.getPeer(hopNodeId);
            if (peer != null) {
                ConnectionMetrics metrics = node.connectionManager.findConnection(peer).getMetrics();
                totalLatency += metrics.getLatency();
                totalPacketLoss += metrics.getPacketLossRate();
            }
        }

        // Fetch weights from configuration
        double latencyWeight = node.config.getLatencyWeight();
        double packetLossWeight = node.config.getPacketLossWeight();
        double hopCountWeight = node.config.getHopCountWeight();

        // Calculate weighted score
        double score = (totalLatency * latencyWeight) +
                       (totalPacketLoss * packetLossWeight) +
                       (hopCount * hopCountWeight);
        return score;
    }

    public void routeMessage(Message message) {
        if (message.getType() == MessageType.ROUTE_REQUEST || message.getType() == MessageType.ROUTE_REPLY) {
            // Handle control messages differently
            handleControlMessage(message);
            return;
        }

        Route route = selectOptimalRoute(message.getDestinationNodeId());
        if (route != null) {
            // Forward the message through the next hop
            String nextHopId = route.getNextHop();
            PeerInfo nextHopPeer = node.peerDirectory.getPeer(nextHopId);
            if (nextHopPeer != null) {
                node.connectionManager.sendDataToNode(nextHopPeer, message.toBytes());
                Logger.logInfo("Message routed from " + node.getNodeId() + " to " + nextHopId);
            } else {
                Logger.logError("Next hop " + nextHopId + " not found for message routing.");
            }
        } else {
            // Initiate route discovery if no route is found
            discoverRoute(message);
        }
    }

    public void handleIncomingMessage(Message message) {
        switch (message.getType()) {
            case HEARTBEAT:
                // Already handled by HeartbeatManager
                break;
            case CLUSTER_HEAD_ANNOUNCEMENT:
                handleClusterHeadAnnouncement(message);
                break;
            case ROUTE_REQUEST:
                handleRouteRequest(message);
                break;
            case ROUTE_REPLY:
                handleRouteReply(message);
                break;
            case UNICAST:
            case BROADCAST:
                deliverOrForwardMessage(message);
                break;
            case SECURE_TUNNEL_ESTABLISH:
                handleSecureTunnelEstablish(message);
                break;
            case SECURE_TUNNEL_CLOSE:
                handleSecureTunnelClose(message);
                break;
            case DATA:
                processDataMessage(message);
                break;
            default:
                Logger.logWarning("Unknown message type received: " + message.getType());
        }
    }

    private void handleControlMessage(Message message) {
        // Handle control messages like ROUTE_REQUEST and ROUTE_REPLY
        // Placeholder for control message handling
    }

    private void handleClusterHeadAnnouncement(Message message) {
        String announcingNodeId = message.getSourceNodeId();
        node.peerDirectory.addClusterHead(announcingNodeId);
        clusterManager.handleClusterHeadAnnouncement(announcingNodeId);
        Logger.logInfo("ClusterHead announcement received from " + announcingNodeId);
        // Optionally establish secure tunnels with the new cluster head
        // Placeholder for secure tunnel establishment
    }

    private void handleRouteRequest(Message message) {
        // Implement logic to respond to route requests
        Logger.logInfo("Route request received from " + message.getSourceNodeId());
        // Placeholder for responding to route requests
    }

    private void handleRouteReply(Message message) {
        // Implement logic to update routing table based on route reply
        Logger.logInfo("Route reply received from " + message.getSourceNodeId());
        // Placeholder for handling route replies
    }

    private void deliverOrForwardMessage(Message message) {
        if (message.getDestinationNodeId().equals(node.getNodeId())) {
            // Message is for this node
            processMessage(message);
        } else {
            // Forward the message
            routeMessage(message);
        }
    }

    private void handleSecureTunnelEstablish(Message message) {
        // Handle secure tunnel establishment requests
        Logger.logInfo("Secure tunnel establishment request received from " + message.getSourceNodeId());
        // Placeholder for secure tunnel establishment handling
    }

    private void handleSecureTunnelClose(Message message) {
        // Handle secure tunnel closure requests
        Logger.logInfo("Secure tunnel closure request received from " + message.getSourceNodeId());
        // Placeholder for secure tunnel closure handling
    }

    private void processDataMessage(Message message) {
        // Handle the actual payload
        Logger.logInfo("Data message received for node " + node.getNodeId());
        // Implement payload processing logic
    }

    private void processMessage(Message message) {
        // Handle the actual payload
        Logger.logInfo("Message received for node " + node.getNodeId());
        // Implement payload processing logic
    }

    public void updateRoutingTable(Route route) {
        routingTable.addRoute(route.getDestination(), route);
        Logger.logInfo("Routing table updated with route to " + route.getDestination());
    }
}
3.5.2. com.vrouting.network.adhoc.RoutingTable
Purpose:

Stores available routes to destination nodes within the network.
Implementation:

public class RoutingTable {
    private Map<String, List<Route>> routes;

    public RoutingTable() {
        this.routes = new HashMap<>();
    }

    public void addRoute(String destination, Route route) {
        routes.computeIfAbsent(destination, k -> new ArrayList<>()).add(route);
    }

    public List<Route> getRoutes(String destination) {
        return routes.getOrDefault(destination, new ArrayList<>());
    }

    public Route getBestRoute(String destination) {
        List<Route> possibleRoutes = getRoutes(destination);
        if (possibleRoutes.isEmpty()) return null;

        // Example: Select the route with the lowest score
        double bestScore = Double.MAX_VALUE;
        Route optimalRoute = null;
        for (Route route : possibleRoutes) {
            double score = route.getScore();
            if (score < bestScore) {
                bestScore = score;
                optimalRoute = route;
            }
        }
        return optimalRoute;
    }
}
3.5.3. com.vrouting.network.adhoc.Route
Purpose:

Represents a route to a destination node, including the list of hops and a score based on KPIs.
Implementation:

public class Route {
    private String destination;
    private List<String> hops;
    private double score; // Calculated based on KPIs

    public Route(String destination, List<String> hops) {
        this.destination = destination;
        this.hops = hops;
        this.score = 0.0;
    }

    public String getDestination() { return destination; }
    public List<String> getHops() { return hops; }
    public double getScore() { return score; }
    public void setScore(double score) { this.score = score; }

    public String getNextHop() {
        return hops.isEmpty() ? null : hops.get(0);
    }
}
3.6. Secure Communication
3.6.1. com.vrouting.network.security.EncryptionManager
Responsibilities:

Manages encryption and decryption of messages to ensure secure communication.
Handles secure tunnel management for inter-cluster communications.
Implementation:

public class EncryptionManager {
    private KeyManager keyManager;
    private Cipher cipher;

    public EncryptionManager(KeyManager keyManager) {
        this.keyManager = keyManager;
        try {
            this.cipher = Cipher.getInstance("AES-256");
            // Initialize cipher as needed
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            Logger.logError("Cipher initialization failed: " + e.getMessage());
        }
    }

    public byte[] encrypt(byte[] plainData, String keyId) {
        try {
            SecretKey key = keyManager.getPreSharedKey(keyId);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            return cipher.doFinal(plainData);
        } catch (InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
            Logger.logError("Encryption failed: " + e.getMessage());
            return null;
        }
    }

    public byte[] decrypt(byte[] encryptedData, String keyId) {
        try {
            SecretKey key = keyManager.getPreSharedKey(keyId);
            cipher.init(Cipher.DECRYPT_MODE, key);
            return cipher.doFinal(encryptedData);
        } catch (InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
            Logger.logError("Decryption failed: " + e.getMessage());
            return null;
        }
    }

    public void establishSecureTunnel(ClusterHeadNode peerHead) {
        // Use SSH keys from KeyManager to establish secure tunnel
        // This could involve key exchange protocols and setting up encrypted channels
        // Placeholder for secure tunnel establishment
    }

    public void closeSecureTunnel(ClusterHeadNode peerHead) {
        // Close the secure tunnel with the specified peer cluster head
        // Placeholder for secure tunnel closure
    }
}
3.6.2. com.vrouting.network.security.KeyManager
Responsibilities:

Handles key generation, distribution, and management for secure communications.
Implementation:

public class KeyManager {
    private Map<String, SecretKey> preSharedKeys;
    private Map<String, KeyPair> keyPairs;

    public KeyManager() {
        this.preSharedKeys = new HashMap<>();
        this.keyPairs = new HashMap<>();
    }

    // Pre-shared Key Management
    public SecretKey getPreSharedKey(String nodeId) {
        return preSharedKeys.get(nodeId);
    }

    public void setPreSharedKey(String nodeId, SecretKey key) {
        preSharedKeys.put(nodeId, key);
    }

    // Key Pair Management
    public KeyPair getKeyPair(String nodeId) {
        return keyPairs.get(nodeId);
    }

    public void generateKeyPair(String nodeId) {
        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            keyGen.initialize(2048);
            KeyPair keyPair = keyGen.generateKeyPair();
            keyPairs.put(nodeId, keyPair);
            Logger.logInfo("Key pair generated for node " + nodeId);
        } catch (NoSuchAlgorithmException e) {
            Logger.logError("Key pair generation failed for node " + nodeId + ": " + e.getMessage());
        }
    }

    public void rotateKeys(String nodeId) {
        // Implement key rotation logic
        // Generate new keys and update preSharedKeys and keyPairs as needed
        generateKeyPair(nodeId);
        // Placeholder for key rotation steps
        Logger.logInfo("Keys rotated for node " + nodeId);
    }
}
3.6.3. com.vrouting.network.security.SecureTunnel
Responsibilities:

Manages the establishment and closure of secure tunnels between cluster heads.
Implementation:

public class SecureTunnel {
    private ClusterHeadNode localClusterHead;
    private ClusterHeadNode remoteClusterHead;
    private EncryptionManager encryptionManager;
    private boolean established;

    public SecureTunnel(ClusterHeadNode local, ClusterHeadNode remote, EncryptionManager encryptionManager) {
        this.localClusterHead = local;
        this.remoteClusterHead = remote;
        this.encryptionManager = encryptionManager;
        this.established = false;
    }

    public void establish() {
        // Implement secure tunnel establishment using SSH keys
        // Placeholder for actual implementation
        encryptionManager.establishSecureTunnel(remoteClusterHead);
        established = true;
        Logger.logInfo("Secure tunnel established between " + localClusterHead.getNodeId() + " and " + remoteClusterHead.getNodeId());
    }

    public void close() {
        // Implement secure tunnel closure
        encryptionManager.closeSecureTunnel(remoteClusterHead);
        established = false;
        Logger.logInfo("Secure tunnel closed between " + localClusterHead.getNodeId() + " and " + remoteClusterHead.getNodeId());
    }

    public boolean isEstablished() {
        return established;
    }
}
3.7. Message Handling and History Tracking
3.7.1. com.vrouting.network.adhoc.Message
Responsibilities:

Represents a network message, including data and metadata.
Tracks the history of the message's routing path.
Supports serialization and deserialization for transmission.
Implementation:

public class Message {
    private String sourceNodeId;
    private String destinationNodeId;
    private byte[] payload;
    private int hopCount;
    private MessageType type;
    private List<String> routingHistory; // Tracks node IDs traversed
    private long timestamp;

    public Message(String sourceNodeId, String destinationNodeId, byte[] payload, MessageType type) {
        this.sourceNodeId = sourceNodeId;
        this.destinationNodeId = destinationNodeId;
        this.payload = payload;
        this.type = type;
        this.hopCount = 0;
        this.routingHistory = new ArrayList<>();
        this.routingHistory.add(sourceNodeId);
        this.timestamp = System.currentTimeMillis();
    }

    // Getters and Setters
    public String getSourceNodeId() { return sourceNodeId; }
    public String getDestinationNodeId() { return destinationNodeId; }
    public byte[] getPayload() { return payload; }
    public void setPayload(byte[] payload) { this.payload = payload; }
    public int getHopCount() { return hopCount; }
    public void setHopCount(int hopCount) { this.hopCount = hopCount; }
    public MessageType getType() { return type; }
    public List<String> getRoutingHistory() { return routingHistory; }
    public long getTimestamp() { return timestamp; }

    // Message Handling
    public void incrementHopCount() { this.hopCount += 1; }

    public void addToRoutingHistory(String nodeId) { this.routingHistory.add(nodeId); }

    // Serialization
    public byte[] toBytes() {
        // Implement serialization logic using JSON for readability and testing
        JSONObject json = new JSONObject();
        json.put("sourceNodeId", sourceNodeId);
        json.put("destinationNodeId", destinationNodeId);
        json.put("payload", Base64.getEncoder().encodeToString(payload));
        json.put("hopCount", hopCount);
        json.put("type", type.toString());
        json.put("routingHistory", routingHistory);
        json.put("timestamp", timestamp);
        return json.toString().getBytes(StandardCharsets.UTF_8);
    }

    public static Message fromBytes(byte[] data) {
        // Implement deserialization logic using JSON
        String jsonString = new String(data, StandardCharsets.UTF_8);
        JSONObject json = new JSONObject(jsonString);
        String source = json.getString("sourceNodeId");
        String destination = json.getString("destinationNodeId");
        byte[] payload = Base64.getDecoder().decode(json.getString("payload"));
        MessageType type = MessageType.valueOf(json.getString("type"));
        int hopCount = json.getInt("hopCount");
        List<String> routingHistory = new ArrayList<>();
        JSONArray historyArray = json.getJSONArray("routingHistory");
        for (int i = 0; i < historyArray.length(); i++) {
            routingHistory.add(historyArray.getString(i));
        }
        long timestamp = json.getLong("timestamp");
        Message message = new Message(source, destination, payload, type);
        message.setHopCount(hopCount);
        message.routingHistory = routingHistory;
        message.timestamp = timestamp;
        return message;
    }
}
3.7.2. com.vrouting.network.adhoc.MessageType
Purpose:

Enumerates all possible message types in the network.
Implementation:

public enum MessageType {
    HEARTBEAT,                  // Heartbeat signals to monitor node status
    BROADCAST,                  // General broadcast messages to all nodes
    UNICAST,                    // Direct messages between specific nodes
    ROUTE_REQUEST,              // Requests for route discovery
    ROUTE_REPLY,                // Replies to route requests
    ACKNOWLEDGMENT,             // Acknowledgments for received messages
    CLUSTER_HEAD_ANNOUNCEMENT,  // Announcements from cluster heads
    SECURE_TUNNEL_ESTABLISH,    // Messages to establish secure tunnels
    SECURE_TUNNEL_CLOSE,        // Messages to close secure tunnels
    DATA,                       // Data payload messages
    // Add other types as needed
}
3.8. Peer Directory and Adjacency Matrix
3.8.1. com.vrouting.network.adhoc.PeerDirectory
Purpose:

Maintains a directory of peers, including their last seen time, connection metrics, and routing history.
Facilitates the creation of an adjacency matrix for centrality calculations.
Implementation:

public class PeerDirectory {
    private Map<String, PeerInfo> peers;

    public PeerDirectory() {
        this.peers = new HashMap<>();
    }

    public void updatePeerStatus(String nodeId, long lastSeen, NodeState state) {
        PeerInfo info = peers.getOrDefault(nodeId, new PeerInfo(nodeId));
        info.setLastSeen(lastSeen);
        info.setState(state);
        peers.put(nodeId, info);
    }

    public void addClusterHead(String nodeId) {
        PeerInfo info = peers.getOrDefault(nodeId, new PeerInfo(nodeId));
        info.setClusterHead(true);
        peers.put(nodeId, info);
    }

    public PeerInfo getPeer(String nodeId) {
        return peers.get(nodeId);
    }

    public Collection<PeerInfo> getAllPeers() {
        return peers.values();
    }

    public AdjacencyMatrix getAdjacencyMatrix() {
        // Convert peer information into an adjacency matrix for centrality calculation
        AdjacencyMatrix adjMatrix = new AdjacencyMatrix(peers.size());
        List<String> nodeIds = new ArrayList<>(peers.keySet());

        for (int i = 0; i < nodeIds.size(); i++) {
            for (int j = i + 1; j < nodeIds.size(); j++) {
                String nodeA = nodeIds.get(i);
                String nodeB = nodeIds.get(j);
                // Define criteria for connection (e.g., recent communication)
                PeerInfo infoA = peers.get(nodeA);
                PeerInfo infoB = peers.get(nodeB);
                if (areNodesConnected(infoA, infoB)) {
                    adjMatrix.addEdge(nodeA, nodeB);
                }
            }
        }
        return adjMatrix;
    }

    private boolean areNodesConnected(PeerInfo a, PeerInfo b) {
        // Define logic to determine if two nodes are connected
        // Example: Nodes are connected if they have exchanged messages within the last X seconds
        long currentTime = System.currentTimeMillis();
        long freshnessThreshold = 60000; // 60 seconds, for example
        boolean connected = (currentTime - a.getLastSeen() <= freshnessThreshold) &&
                            (currentTime - b.getLastSeen() <= freshnessThreshold);
        return connected;
    }
}
3.8.2. com.vrouting.network.adhoc.PeerInfo
Purpose:

Stores information about a peer node.
Implementation:

public class PeerInfo {
    private String nodeId;
    private long lastSeen;
    private NodeState state;
    private boolean isClusterHead;
    private List<String> routingHistory; // Optional

    public PeerInfo(String nodeId) {
        this.nodeId = nodeId;
        this.lastSeen = System.currentTimeMillis();
        this.state = NodeState.ACTIVE;
        this.isClusterHead = false;
        this.routingHistory = new ArrayList<>();
    }

    // Getters and Setters
    public String getNodeId() { return nodeId; }
    public long getLastSeen() { return lastSeen; }
    public void setLastSeen(long lastSeen) { this.lastSeen = lastSeen; }
    public NodeState getState() { return state; }
    public void setState(NodeState state) { this.state = state; }
    public boolean isClusterHead() { return isClusterHead; }
    public void setClusterHead(boolean isClusterHead) { this.isClusterHead = isClusterHead; }
    public List<String> getRoutingHistory() { return routingHistory; }
    public void addToRoutingHistory(String nodeId) { this.routingHistory.add(nodeId); }
}
3.8.3. com.vrouting.network.adhoc.AdjacencyMatrix
Purpose:

Represents the network topology as an adjacency matrix for centrality calculations.
Implementation:

public class AdjacencyMatrix {
    private int size;
    private int[][] matrix;
    private Map<String, Integer> nodeIndexMap;
    private Map<Integer, String> indexNodeMap;

    public AdjacencyMatrix(int size) {
        this.size = size;
        this.matrix = new int[size][size];
        this.nodeIndexMap = new HashMap<>();
        this.indexNodeMap = new HashMap<>();
    }

    public void addEdge(String nodeA, String nodeB) {
        int indexA = getIndex(nodeA);
        int indexB = getIndex(nodeB);
        matrix[indexA][indexB] = 1;
        matrix[indexB][indexA] = 1; // Assuming undirected graph
    }

    private int getIndex(String nodeId) {
        if (!nodeIndexMap.containsKey(nodeId)) {
            int index = nodeIndexMap.size();
            nodeIndexMap.put(nodeId, index);
            indexNodeMap.put(index, nodeId);
            return index;
        }
        return nodeIndexMap.get(nodeId);
    }

    public int[][] getMatrix() { return matrix; }

    public Map<String, Integer> getNodeIndexMap() { return nodeIndexMap; }

    public Map<Integer, String> getIndexNodeMap() { return indexNodeMap; }
}
3.9. Secure Tunnel Management
3.9.1. com.vrouting.network.security.SecureTunnel
Purpose:

Manages the establishment and closure of secure tunnels between cluster heads.
Implementation:

public class SecureTunnel {
    private ClusterHeadNode localClusterHead;
    private ClusterHeadNode remoteClusterHead;
    private EncryptionManager encryptionManager;
    private boolean established;

    public SecureTunnel(ClusterHeadNode local, ClusterHeadNode remote, EncryptionManager encryptionManager) {
        this.localClusterHead = local;
        this.remoteClusterHead = remote;
        this.encryptionManager = encryptionManager;
        this.established = false;
    }

    public void establish() {
        // Implement secure tunnel establishment using SSH keys
        // This could involve exchanging keys, setting up encrypted channels, etc.
        // Placeholder for actual implementation
        encryptionManager.establishSecureTunnel(remoteClusterHead);
        established = true;
        Logger.logInfo("Secure tunnel established between " + localClusterHead.getNodeId() + " and " + remoteClusterHead.getNodeId());
    }

    public void close() {
        // Implement secure tunnel closure
        encryptionManager.closeSecureTunnel(remoteClusterHead);
        established = false;
        Logger.logInfo("Secure tunnel closed between " + localClusterHead.getNodeId() + " and " + remoteClusterHead.getNodeId());
    }

    public boolean isEstablished() {
        return established;
    }
}
4. Message Handling and Node Reactions
4.1. Enumerated Message Types and Their Meanings
MessageType Enumerations:

public enum MessageType {
    HEARTBEAT,                  // Heartbeat signals to monitor node status
    BROADCAST,                  // General broadcast messages to all nodes
    UNICAST,                    // Direct messages between specific nodes
    ROUTE_REQUEST,              // Requests for route discovery
    ROUTE_REPLY,                // Replies to route requests
    ACKNOWLEDGMENT,             // Acknowledgments for received messages
    CLUSTER_HEAD_ANNOUNCEMENT,  // Announcements from cluster heads
    SECURE_TUNNEL_ESTABLISH,    // Messages to establish secure tunnels
    SECURE_TUNNEL_CLOSE,        // Messages to close secure tunnels
    DATA,                       // Data payload messages
    // Add other types as needed
}
Descriptions:

HEARTBEAT: Periodic signals sent to monitor the presence and status of nodes.
BROADCAST: Messages sent to all nodes within the network or a specific cluster.
UNICAST: Messages directed to a specific node.
ROUTE_REQUEST: Initiates route discovery to a destination node.
ROUTE_REPLY: Responds to a route request with available routes.
ACKNOWLEDGMENT: Confirms the receipt of a message.
CLUSTER_HEAD_ANNOUNCEMENT: Announces a node's role as a cluster head.
SECURE_TUNNEL_ESTABLISH: Requests to establish a secure tunnel between nodes.
SECURE_TUNNEL_CLOSE: Requests to close an existing secure tunnel.
DATA: Carries actual data payloads between nodes.
4.2. Node Reactions to Different Message Types
4.2.1. Regular Nodes
HEARTBEAT:
Action: Update PeerDirectory with the sender's status.
Effect: Maintains an up-to-date view of network peers.
BROADCAST:
Action: Process the broadcast message or forward it within the cluster.
Effect: Disseminates important information network-wide without duplication.
UNICAST:
Action: If the message is for the node, process the payload; otherwise, forward the message using RoutingManager.
Effect: Ensures direct communication between specific nodes.
ROUTE_REQUEST:
Action: Evaluate if the node can provide a route to the destination; respond with ROUTE_REPLY if a route is found.
Effect: Facilitates dynamic route discovery, enhancing network connectivity.
ROUTE_REPLY:
Action: Update RoutingTable with the new route information.
Effect: Improves routing efficiency by incorporating discovered paths.
ACKNOWLEDGMENT:
Action: Confirm the successful receipt of a previously sent message.
Effect: Ensures reliable message delivery and prevents unnecessary retransmissions.
CLUSTER_HEAD_ANNOUNCEMENT:
Action: Update PeerDirectory with the new cluster head; possibly establish secure tunnels.
Effect: Maintains accurate cluster head information for routing and secure communications.
SECURE_TUNNEL_ESTABLISH:
Action: Engage EncryptionManager to establish the secure tunnel.
Effect: Enhances security for sensitive communications between nodes or clusters.
SECURE_TUNNEL_CLOSE:
Action: Engage EncryptionManager to close the secure tunnel.
Effect: Terminates secure communications as needed, freeing resources.
DATA:
Action: Decrypt and process the data payload.
Effect: Enables nodes to exchange and utilize information securely.
4.2.2. Cluster Head Nodes
HEARTBEAT:
Action: Same as regular nodes; additionally, manage intra-cluster routing based on heartbeat data.
Effect: Monitors and maintains cluster health and connectivity.
BROADCAST:
Action: Broadcast important cluster-wide information or updates.
Effect: Ensures all cluster members are informed of significant events or changes.
UNICAST:
Action: Directly handle or route messages within the cluster.
Effect: Facilitates efficient intra-cluster communication.
ROUTE_REQUEST:
Action: Provide or assist in finding routes within or between clusters.
Effect: Enhances inter-cluster routing capabilities.
ROUTE_REPLY:
Action: Update routing tables and inform relevant nodes.
Effect: Improves routing accuracy and efficiency within the cluster.
ACKNOWLEDGMENT:
Action: Confirm receipt of critical messages.
Effect: Ensures reliable communication within the cluster.
CLUSTER_HEAD_ANNOUNCEMENT:
Action: Inform other cluster heads about role changes; update PeerDirectory.
Effect: Maintains accurate cluster head information across the network.
SECURE_TUNNEL_ESTABLISH:
Action: Initiate and manage secure tunnels with peer cluster heads.
Effect: Ensures secure inter-cluster communications.
SECURE_TUNNEL_CLOSE:
Action: Terminate secure tunnels as required.
Effect: Maintains secure communication integrity by closing unnecessary tunnels.
DATA:
Action: Handle intra-cluster data communication securely.
Effect: Facilitates secure and efficient data exchange within the cluster.
4.3. Heartbeat Phases
The heartbeat mechanism is designed to help nodes understand the network structure and achieve stability through a three-phase approach with negative exponential decay intervals.

Phase 1: Rapid Heartbeats (Discovery Phase)
Heartbeat Intervals: High-frequency (e.g., 1 second, 4 seconds).
Purpose:
Quickly gather information about nearby nodes.
Build the initial adjacency matrix for centrality calculation.
Node Actions:
Send heartbeats at rapid intervals.
Update PeerDirectory with responses.
Assess if the node qualifies as a cluster head based on centrality and configuration.
Phase 2: Moderate Heartbeats (Evaluation Phase)
Heartbeat Intervals: Medium-frequency (e.g., 18 seconds).
Purpose:
Continue monitoring network stability.
Refine centrality calculations based on updated adjacency matrix.
Node Actions:
Send heartbeats at decreasing intervals.
Re-evaluate centrality scores.
Transition to Phase 3 if network stability is achieved.
Phase 3: Low Heartbeats (Stability Phase)
Heartbeat Intervals: Low-frequency (e.g., 60 seconds).
Purpose:
Maintain network monitoring with minimal overhead.
Ensure long-term network stability.
Node Actions:
Send heartbeats at infrequent intervals.
Maintain updated PeerDirectory with minimal resource usage.
Transitioning Between Phases:

Nodes automatically transition through phases based on predefined intervals.
Stability assessments determine when to complete the transition.
External configuration flags can override phase behaviors to allow/disallow cluster head roles.
4.4. Routing Table Management
Responsibilities:

Maintains an up-to-date routing table reflecting available paths to all nodes within the cluster.
Dynamically updates the routing table based on received ROUTE_REPLY messages, periodic evaluations, and heartbeat signals.
Key Functionalities:

4.4.1. Updating Frequency
After Receiving a ROUTE_REPLY:
Immediately update the routing table with the new route information.
Periodic Timer:
Set a timer (e.g., every 5 minutes) to perform routine checks and updates to routing tables.
Detection of Network Changes via Heartbeats:
Adjust routes based on changes detected through heartbeat signals, such as node failures or new node arrivals.
4.4.2. Route Selection Criteria
Dynamic and Configurable:

Implement a weighted scoring system based on multiple KPIs (e.g., latency, packet loss, hop count).
Allow configurability through external settings to adjust weights based on user preferences.
Balanced Approach:

Use a balanced weighted average of KPIs to compute route scores.
Facilitate the addition of new KPIs without major architectural changes.
Implementation Example:

private double calculateRouteScore(Route route) {
    double totalLatency = 0.0;
    double totalPacketLoss = 0.0;
    int hopCount = route.getHops().size();

    for (String hopNodeId : route.getHops()) {
        PeerInfo peer = node.peerDirectory.getPeer(hopNodeId);
        if (peer != null) {
            Connection connection = node.connectionManager.findConnection(peer);
            if (connection != null) {
                ConnectionMetrics metrics = connection.getMetrics();
                totalLatency += metrics.getLatency();
                totalPacketLoss += metrics.getPacketLossRate();
            }
        }
    }

    // Fetch weights from configuration
    double latencyWeight = node.config.getLatencyWeight();
    double packetLossWeight = node.config.getPacketLossWeight();
    double hopCountWeight = node.config.getHopCountWeight();

    // Calculate weighted score
    double score = (totalLatency * latencyWeight) +
                   (totalPacketLoss * packetLossWeight) +
                   (hopCount * hopCountWeight);
    return score;
}
4.5. Secure Tunnel Management
4.5.1. Dual Security Aspects
Secure Tunnels via Internet:

Purpose: Enable secure communication channels between cluster heads over the internet.
Implementation:
Utilize SSH keys stored in KeyManager for authentication and encryption.
Secure tunnels are established using external configurations (JSON files or objects).
Tunnels facilitate secure inter-cluster communications regardless of physical proximity.
Secure Tunnels via Ad-Hoc Network (Multi-Hop):

Purpose: Establish secure communication channels within the ad-hoc network through multi-hop paths.
Implementation:
Use SSH keys from KeyManager for mutual authentication and encryption.
Tunnels are established over existing ad-hoc connections, leveraging multi-hop routing for extended reach.
Secure tunnel information is maintained in directories for reuse and efficiency.
4.5.2. Secure Tunnel Establishment and Management
Steps:

Configuration Input:
Secure tunnel configurations (e.g., SSH keys, target cluster heads) are supplied externally via JSON files or objects.
Handshake Protocol:
Initiation: The initiating cluster head sends a SECURE_TUNNEL_ESTABLISH message to the target cluster head.
Authentication: Mutual authentication using SSH keys ensures both parties are legitimate.
Encryption Setup: Establish an encrypted channel using agreed-upon encryption protocols (e.g., AES-256).
Tunnel Maintenance:
Integrity Checks: Periodic verification of tunnel integrity to detect and prevent breaches.
Re-establishment: Automatic re-establishment of tunnels upon detection of disruptions.
Tunnel Closure:
Initiation: Either cluster head can send a SECURE_TUNNEL_CLOSE message to terminate the tunnel.
Resource Cleanup: Proper closure of encrypted channels and resource deallocation.
Implementation Example:

public class SecureTunnelManager {
    private EncryptionManager encryptionManager;
    private KeyManager keyManager;

    public SecureTunnelManager(EncryptionManager encryptionManager, KeyManager keyManager) {
        this.encryptionManager = encryptionManager;
        this.keyManager = keyManager;
    }

    public void establishTunnel(ClusterHeadNode local, ClusterHeadNode remote) {
        // Load SSH keys from KeyManager
        SecretKey key = keyManager.getPreSharedKey(remote.getNodeId());
        if (key == null) {
            Logger.logError("No pre-shared key found for " + remote.getNodeId());
            return;
        }

        // Establish secure tunnel
        SecureTunnel tunnel = new SecureTunnel(local, remote, encryptionManager);
        tunnel.establish();
        local.getSecureTunnels().add(tunnel);
    }

    public void closeTunnel(ClusterHeadNode local, ClusterHeadNode remote) {
        SecureTunnel tunnel = findTunnel(local, remote);
        if (tunnel != null && tunnel.isEstablished()) {
            tunnel.close();
            local.getSecureTunnels().remove(tunnel);
        }
    }

    private SecureTunnel findTunnel(ClusterHeadNode local, ClusterHeadNode remote) {
        for (SecureTunnel tunnel : local.getSecureTunnels()) {
            if (tunnel.getRemoteClusterHead().equals(remote)) {
                return tunnel;
            }
        }
        return null;
    }
}
5. Text-Based UML Diagram
Below is an updated text-based UML-like representation of the class interactions within your architecture, reflecting the enhancements based on your feedback:

+----------------------+           +-------------------------+
|        Node          |<>-------->|    ConnectionManager    |
|----------------------|           |-------------------------|
| - nodeId             |           | - activeConnections     |
| - address            |           | - socketHandler         |
| - state              |           +-------------------------+
| - connections        |                     |
| - currentCluster     |                     |
| - connectionManager  |                     |
| - heartbeatManager   |                     |
| - clusterManager     |                     |
| - routingManager     |                     |
| - encryptionManager  |                     |
| - networkMetrics     |                     |
| - peerDirectory      |                     |
| - isAllowedClusterHead|                    |
| - centralityScore    |                     |
| - centralityCalculator|                    |
| - config             |                     |
|----------------------|                     |
| + initialize()       |                     |
| + shutdown()         |                     |
| + addConnection()    |                     |
| + removeConnection() |                     |
| + joinCluster()      |                     |
| + leaveCluster()     |                     |
| + sendHeartbeat()    |                     |
| + receiveHeartbeat() |                     |
| + sendMessage()      |                     |
| + receiveMessage()   |                     |
| + evaluateAndDecideClusterHead() |          |
| + promoteToClusterHead() |                  |
+----------------------+                     |
            ^                                    |
            |                                    |
            |                                    |
    +----------------------+           +-------------------------+
    |    RegularNode       |           |    ClusterHeadNode      |
    |----------------------|           |-------------------------|
    |                      |           | - peerClusterHeads      |
    |                      |           | - secureTunnels         |
    |----------------------|           | - intraClusterRoutingTable |
    | + manageIntraCluster |           |-------------------------|
    | + broadcastAnnouncement |        | + broadcastClusterHeadAnnouncement() |
    | + calculateCentrality  |        | + manageIntraClusterRouting() |
    +----------------------+           | + establishSecureTunnel() |
                                       | + closeAllSecureTunnels() |
                                       | + maintainRoutingTable() |
                                       +-------------------------+
            ^                                    |
            |                                    |
            |                                    |
    +----------------------+           +-------------------------+
    |      Cluster         |<>-------->|    ClusterManager       |
    |----------------------|           |-------------------------|
    | - clusterHead        |           | - clusters              |
    | - members            |           | - nodeToClusterMap      |
    | - intraClusterConns  |           +-------------------------+
    |----------------------|           | + discoverClusters()    |
    | + addMember()        |           | + integrateClusterHead()|
    | + removeMember()     |           | + handleClusterHeadAnnouncement() |
    | + setClusterHead()   |           | + handleNodeJoin()      |
    | + broadcastNewClusterHead() |    | + handleNodeLeave()     |
    | + routeMessage()     |           | + updateRoutingTables() |
    +----------------------+           +-------------------------+
            ^                                    ^
            |                                    |
            |                                    |
    +----------------------+           +-------------------------+
    |  HeartbeatManager    |           |     RoutingManager      |
    |----------------------|           |-------------------------|
    | - scheduler          |           | - routingTable          |
    | - heartbeatIntervals |           | - clusterManager        |
    | - currentPhase       |           +-------------------------+
    | - running            |           | + discoverRoute()       |
    |----------------------|           | + maintainRoutes()      |
    | + startHeartbeat()   |           | + selectOptimalRoute()  |
    | + stopHeartbeat()    |           | + handleIncomingMessage() |
    | + sendHeartbeat()    |           | + routeMessage()        |
    | + receiveHeartbeat() |           | + updateRoutingTable()  |
    | + adjustHeartbeatFrequency() |    +-------------------------+
    +----------------------+                     ^
            ^                                    |
            |                                    |
            |                                    |
    +----------------------+           +-------------------------+
    |  EncryptionManager   |           |     MultiHopManager     |
    |----------------------|           |-------------------------|
    | - keyManager         |           | - routingManager        |
    | - cipher             |           | - clusterManager        |
    |----------------------|           | - node                  |
    | + encrypt()          |           +-------------------------+
    | + decrypt()          |           | + forwardMessage()      |
    | + establishTunnel()  |           | + processMessage()      |
    | + closeTunnel()      |           | + incrementHopCount()   |
    +----------------------+           | + isMaxHopCount()       |
            ^                            +-------------------------+
            |
            |
    +----------------------+           
    |     KeyManager       |
    |----------------------|
    | - preSharedKeys      |
    | - keyPairs           |
    |----------------------|
    | + getPreSharedKey()  |
    | + setPreSharedKey()  |
    | + getKeyPair()       |
    | + generateKeyPair()  |
    | + rotateKeys()       |
    +----------------------+

    +----------------------+           
    |   SecureTunnel       |
    |----------------------|
    | - localClusterHead   |
    | - remoteClusterHead  |
    | - encryptionManager  |
    | - established        |
    |----------------------|
    | + establish()        |
    | + close()            |
    | + isEstablished()    |
    +----------------------+

    +----------------------+           
    |   CentralityCalculatorInterface |
    |----------------------|
    | + calculateEigenvectorCentrality() |
    | + findHighestCentralityNode()      |
    +----------------------+

    +----------------------+           
    | JGraphTCentralityCalculator |
    |----------------------|
    | + calculateEigenvectorCentrality() |
    | + findHighestCentralityNode()      |
    +----------------------+
6. Comprehensive Node Behavior and Network Health
6.1. Cluster Head Selection and Registration
Network Discovery:

Upon joining the network, a node enters Phase 1 of the heartbeat mechanism, sending frequent heartbeats to discover and map nearby nodes.
Centrality Calculation:

As heartbeats are exchanged, nodes build their PeerDirectory and construct an adjacency matrix.
Using the CentralityCalculatorInterface, nodes calculate their eigenvector centrality scores.
Automatic Cluster Head Promotion:

If a node's centrality score exceeds the threshold defined in the configuration and it is allowed to be a cluster head (isAllowedClusterHead flag), it automatically promotes itself to a ClusterHeadNode.
Upon promotion, the node broadcasts a CLUSTER_HEAD_ANNOUNCEMENT to inform other nodes of its new role.
Cluster Formation:

Other nodes recognize the cluster head announcement and update their cluster memberships accordingly.
The cluster head initializes its intra-cluster routing table and establishes secure tunnels as necessary.
6.2. Heartbeat Mechanism Phases
Phase 1: Discovery Phase

Heartbeat Interval: 1 second, then 4 seconds, then 18 seconds.
Actions:
Send frequent heartbeats to discover nearby nodes.
Collect routing history and heartbeat responses to build the adjacency matrix.
Evaluate centrality scores to determine cluster head eligibility.
Phase 2: Evaluation Phase

Heartbeat Interval: Increases based on the negative exponential decay (e.g., 18 seconds to 60 seconds).
Actions:
Continue monitoring network stability.
Re-calculate centrality scores periodically.
Transition to Phase 3 upon achieving network stability.
Phase 3: Stability Phase

Heartbeat Interval: 60 seconds (configurable).
Actions:
Maintain minimal heartbeat signals to monitor long-term network health.
Ensure ongoing communication integrity and adjust routing as needed.
6.3. Message Handling and Network Health
Summary of Message Types and Node Reactions:

Message Type	Description	Node Reaction	Effect on Network Health
HEARTBEAT	Monitor node status and network connectivity	- Update PeerDirectory with sender's status.
- Adjust heartbeat frequency if necessary.	Ensures up-to-date knowledge of network peers, enabling accurate centrality calculations and routing decisions.
BROADCAST	General messages to all nodes	- Process or forward the message within the cluster.
- Prevent duplication to maintain efficiency.	Facilitates widespread information dissemination without overloading the network.
UNICAST	Direct messages between specific nodes	- If the message is for the node, process the payload.
- Otherwise, forward the message using RoutingManager.	Enables targeted communication, ensuring messages reach intended recipients efficiently.
ROUTE_REQUEST	Initiate route discovery to a destination node	- If capable, provide route information.
- Otherwise, forward the request to discover routes.	Enhances dynamic route discovery, improving network flexibility and connectivity.
ROUTE_REPLY	Respond to route requests with available routes	- Update RoutingTable with the new route information.
- Potentially acknowledge the route discovery.	Improves routing efficiency by incorporating discovered paths, reducing message latency and hop counts.
ACKNOWLEDGMENT	Confirm receipt of a message	- Confirm successful receipt of messages.
- Optionally trigger further actions based on acknowledgment.	Ensures reliable message delivery and prevents unnecessary retransmissions, maintaining network integrity.
CLUSTER_HEAD_ANNOUNCEMENT	Announce a node's role as a cluster head	- Update PeerDirectory with the new cluster head.
- Establish secure tunnels if necessary.
- Adjust routing tables to include new routes.	Maintains accurate cluster head information, enabling efficient intra-cluster and inter-cluster routing and secure communications.
SECURE_TUNNEL_ESTABLISH	Request to establish a secure tunnel between nodes	- Engage EncryptionManager to establish the tunnel.
- Respond with acknowledgment or error based on tunnel establishment success.	Ensures secure communication channels between cluster heads, enhancing data security and trust within the network.
SECURE_TUNNEL_CLOSE	Request to close an existing secure tunnel	- Engage EncryptionManager to close the tunnel.
- Confirm tunnel closure to the requesting node.	Terminates unnecessary or compromised secure channels, conserving resources and maintaining communication security.
DATA	Carry actual data payloads between nodes	- Decrypt and process the data payload.
- Deliver the data to the application layer or forward it as needed.	Facilitates the exchange of information, enabling application-specific functionalities while maintaining data security through encryption.
Impact on Network Health:

Reliability: Acknowledgments and route updates ensure that messages are delivered reliably, preventing data loss.
Security: Secure tunnel management protects sensitive data, fostering trust among nodes.
Efficiency: Dynamic routing and intelligent heartbeat management optimize network performance, reducing latency and conserving resources.
Scalability: Automated cluster head promotion and secure tunnel establishment support network growth without manual interventions.
7. Implementation of Missing Perspectives
7.1. Cluster Head Election and Centrality Calculation
7.1.1. Interface and Implementation
Centrality Calculation Interface:

package com.vrouting.network.centrality;

import com.vrouting.network.adhoc.AdjacencyMatrix;
import java.util.List;

public interface CentralityCalculatorInterface {
    double calculateEigenvectorCentrality(AdjacencyMatrix adjMatrix, String nodeId);
    String findHighestCentralityNode(AdjacencyMatrix adjMatrix, List<String> nodeIds, List<Boolean> eligibilityFlags);
}
JGraphT-Based Implementation:

package com.vrouting.network.centrality;

import com.vrouting.network.adhoc.AdjacencyMatrix;
import org.jgrapht.Graph;
import org.jgrapht.alg.scoring.EigenvectorCentrality;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;

import java.util.List;
import java.util.Map;

public class JGraphTCentralityCalculator implements CentralityCalculatorInterface {

    @Override
    public double calculateEigenvectorCentrality(AdjacencyMatrix adjMatrix, String nodeId) {
        Graph<String, DefaultEdge> graph = buildGraph(adjMatrix);
        EigenvectorCentrality<String, DefaultEdge> centrality = new EigenvectorCentrality<>(graph);
        centrality.initialize();
        return centrality.getVertexScore(nodeId);
    }

    @Override
    public String findHighestCentralityNode(AdjacencyMatrix adjMatrix, List<String> nodeIds, List<Boolean> eligibilityFlags) {
        Graph<String, DefaultEdge> graph = buildGraph(adjMatrix);
        EigenvectorCentrality<String, DefaultEdge> centrality = new EigenvectorCentrality<>(graph);
        centrality.initialize();

        double maxCentrality = Double.MIN_VALUE;
        String highestNode = null;

        for (int i = 0; i < nodeIds.size(); i++) {
            String nodeId = nodeIds.get(i);
            if (eligibilityFlags.get(i)) {
                double score = centrality.getVertexScore(nodeId);
                if (score > maxCentrality) {
                    maxCentrality = score;
                    highestNode = nodeId;
                }
            }
        }

        return highestNode;
    }

    private Graph<String, DefaultEdge> buildGraph(AdjacencyMatrix adjMatrix) {
        Graph<String, DefaultEdge> graph = new SimpleGraph<>(DefaultEdge.class);
        Map<String, Integer> nodeIndexMap = adjMatrix.getNodeIndexMap();
        Map<Integer, String> indexNodeMap = adjMatrix.getIndexNodeMap();

        // Add vertices
        for (String nodeId : nodeIndexMap.keySet()) {
            graph.addVertex(nodeId);
        }

        // Add edges
        int[][] matrix = adjMatrix.getMatrix();
        for (int i = 0; i < matrix.length; i++) {
            for (int j = i + 1; j < matrix.length; j++) {
                if (matrix[i][j] == 1) {
                    String nodeA = indexNodeMap.get(i);
                    String nodeB = indexNodeMap.get(j);
                    graph.addEdge(nodeA, nodeB);
                }
            }
        }

        return graph;
    }
}
7.1.2. Automatic Cluster Head Promotion
Implementation in Node Class:

public void evaluateAndDecideClusterHead() {
    // Only proceed if allowed to be a cluster head
    if (!isAllowedClusterHead) {
        return;
    }

    // Calculate centrality score using the centrality calculator
    AdjacencyMatrix adjMatrix = peerDirectory.getAdjacencyMatrix();
    centralityScore = centralityCalculator.calculateEigenvectorCentrality(adjMatrix, nodeId);
    Logger.logInfo("Node " + nodeId + " calculated centrality score: " + centralityScore);

    // Check if centrality exceeds threshold from configuration
    double threshold = config.getCentralityThreshold();
    if (centralityScore >= threshold && currentCluster == null) {
        // Promote to cluster head
        promoteToClusterHead();
    }
}
Promotion Method:

private void promoteToClusterHead() {
    // Transition to ClusterHeadNode behavior
    // This could involve casting or creating a new ClusterHeadNode instance
    // For simplicity, assuming Node can handle ClusterHead responsibilities
    Logger.logInfo("Node " + nodeId + " promoting itself to Cluster Head.");
    // Broadcast cluster head announcement
    Message announcement = new Message(this.nodeId, "ALL", "I am now a Cluster Head".getBytes(), MessageType.CLUSTER_HEAD_ANNOUNCEMENT);
    sendMessage(announcement);
}
7.2. Message Serialization and Deserialization
7.2.1. Interface and Implementations
Serialization Interface:

package com.vrouting.network.adhoc;

public interface MessageSerializer {
    byte[] serialize(Message message);
    Message deserialize(byte[] data);
}
JSON-Based Implementation:

package com.vrouting.network.adhoc;

import org.json.JSONArray;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class JSONMessageSerializer implements MessageSerializer {

    @Override
    public byte[] serialize(Message message) {
        JSONObject json = new JSONObject();
        json.put("sourceNodeId", message.getSourceNodeId());
        json.put("destinationNodeId", message.getDestinationNodeId());
        json.put("payload", Base64.getEncoder().encodeToString(message.getPayload()));
        json.put("hopCount", message.getHopCount());
        json.put("type", message.getType().toString());
        json.put("routingHistory", message.getRoutingHistory());
        json.put("timestamp", message.getTimestamp());
        return json.toString().getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public Message deserialize(byte[] data) {
        String jsonString = new String(data, StandardCharsets.UTF_8);
        JSONObject json = new JSONObject(jsonString);
        String source = json.getString("sourceNodeId");
        String destination = json.getString("destinationNodeId");
        byte[] payload = Base64.getDecoder().decode(json.getString("payload"));
        MessageType type = MessageType.valueOf(json.getString("type"));
        int hopCount = json.getInt("hopCount");
        List<String> routingHistory = new ArrayList<>();
        JSONArray historyArray = json.getJSONArray("routingHistory");
        for (int i = 0; i < historyArray.length(); i++) {
            routingHistory.add(historyArray.getString(i));
        }
        long timestamp = json.getLong("timestamp");
        Message message = new Message(source, destination, payload, type);
        message.setHopCount(hopCount);
        message.routingHistory = routingHistory;
        message.timestamp = timestamp;
        return message;
    }
}
Protocol Buffers-Based Implementation:

// Assuming you've defined a protobuf schema for Message
// and generated the corresponding Java classes.

package com.vrouting.network.adhoc;

public class ProtoBufMessageSerializer implements MessageSerializer {

    @Override
    public byte[] serialize(Message message) {
        // Convert to protobuf Message object
        ProtoMessage protoMsg = ProtoMessage.newBuilder()
                .setSourceNodeId(message.getSourceNodeId())
                .setDestinationNodeId(message.getDestinationNodeId())
                .setPayload(ByteString.copyFrom(message.getPayload()))
                .setHopCount(message.getHopCount())
                .setType(message.getType().name())
                .addAllRoutingHistory(message.getRoutingHistory())
                .setTimestamp(message.getTimestamp())
                .build();
        return protoMsg.toByteArray();
    }

    @Override
    public Message deserialize(byte[] data) {
        try {
            ProtoMessage protoMsg = ProtoMessage.parseFrom(data);
            Message message = new Message(
                    protoMsg.getSourceNodeId(),
                    protoMsg.getDestinationNodeId(),
                    protoMsg.getPayload().toByteArray(),
                    MessageType.valueOf(protoMsg.getType())
            );
            message.setHopCount(protoMsg.getHopCount());
            message.routingHistory = protoMsg.getRoutingHistoryList();
            message.timestamp = protoMsg.getTimestamp();
            return message;
        } catch (InvalidProtocolBufferException e) {
            Logger.logError("Failed to deserialize protobuf message: " + e.getMessage());
            return null;
        }
    }
}
Integration in Message Class:

public class Message {
    // Existing fields and methods

    // Serialization using the chosen serializer
    private static MessageSerializer serializer = new JSONMessageSerializer(); // Switch to ProtoBufMessageSerializer() in production

    public byte[] toBytes() {
        return serializer.serialize(this);
    }

    public static Message fromBytes(byte[] data) {
        return serializer.deserialize(data);
    }
}
7.3. Routing Table Management
Responsibilities:

Maintains an up-to-date routing table reflecting available paths to all nodes within the cluster.
Dynamically updates the routing table based on received ROUTE_REPLY messages, periodic evaluations, and heartbeat signals.
Implements dynamic route selection based on multiple KPIs with configurable weights.
Key Functionalities:

7.3.1. Updating Frequency
After Receiving a ROUTE_REPLY:
Immediately update the routing table with the new route information.
Periodic Timer:
Set a timer (e.g., every 5 minutes) to perform routine checks and updates to routing tables.
Detection of Network Changes via Heartbeats:
Adjust routes based on changes detected through heartbeat signals, such as node failures or new node arrivals.
7.3.2. Route Selection Criteria
Dynamic and Configurable:

Implement a weighted scoring system based on multiple KPIs (e.g., latency, packet loss, hop count).
Allow configurability through external settings to adjust weights based on user preferences.
Balanced Approach:

Use a balanced weighted average of KPIs to compute route scores.
Facilitate the addition of new KPIs without major architectural changes.
Implementation Example in RoutingManager:

private double calculateRouteScore(Route route) {
    double totalLatency = 0.0;
    double totalPacketLoss = 0.0;
    int hopCount = route.getHops().size();

    for (String hopNodeId : route.getHops()) {
        PeerInfo peer = node.peerDirectory.getPeer(hopNodeId);
        if (peer != null) {
            Connection connection = node.connectionManager.findConnection(peer);
            if (connection != null) {
                ConnectionMetrics metrics = connection.getMetrics();
                totalLatency += metrics.getLatency();
                totalPacketLoss += metrics.getPacketLossRate();
            }
        }
    }

    // Fetch weights from configuration
    double latencyWeight = node.config.getLatencyWeight();
    double packetLossWeight = node.config.getPacketLossWeight();
    double hopCountWeight = node.config.getHopCountWeight();

    // Calculate weighted score
    double score = (totalLatency * latencyWeight) +
                   (totalPacketLoss * packetLossWeight) +
                   (hopCount * hopCountWeight);
    return score;
}
Dynamic Route Selection:

Weighted Average Calculation: Routes are scored based on the weighted average of selected KPIs.
Configuration-Based Weights: Users can adjust weights via external configuration files to prioritize different KPIs according to their needs.
Extensibility: New KPIs can be added with minimal changes, enhancing the flexibility of the routing logic.
8. Summary and Next Steps
The enhanced architecture now comprehensively addresses your project's requirements, ensuring robust node role management, secure communication, dynamic routing, and efficient network health maintenance. Here's a concise action plan to proceed:

8.1. Implement Centrality Calculation Interface and Implementation
Define CentralityCalculatorInterface with methods for calculating eigenvector centrality and identifying the highest centrality node.
Implement JGraphTCentralityCalculator using the JGraphT library to perform centrality calculations based on the adjacency matrix derived from PeerDirectory.
8.2. Finalize Cluster Head Promotion Mechanism
Automate Promotion: Nodes automatically promote themselves to cluster heads when their centrality score exceeds the configured threshold and they are eligible.
Cluster Head Announcement: Ensure that upon promotion, cluster heads broadcast their new role using CLUSTER_HEAD_ANNOUNCEMENT messages.
8.3. Develop Message Serialization Strategy
Define MessageSerializer Interface with methods for serialization and deserialization.
Implement JSONMessageSerializer for testing and human readability.
Implement ProtoBufMessageSerializer for production to enhance performance and efficiency.
Integrate Serializer into Message Class to allow easy switching between serializers based on the environment.
8.4. Enhance Routing Table Management
Implement Dynamic Routing Table Updates in RoutingManager to handle incoming ROUTE_REPLY messages, periodic evaluations, and heartbeat signals.
Develop Route Scoring Mechanism based on multiple KPIs with configurable weights to select the optimal route dynamically.
Ensure Extensibility by allowing additional KPIs to be incorporated without significant architectural changes.
8.5. Secure Tunnel Management Implementation
Develop Secure Tunnel Handshake Protocols within EncryptionManager to establish and maintain secure tunnels using SSH keys.
Implement Secure Tunnel Lifecycle Management in SecureTunnel and SecureTunnelManager to handle establishment, maintenance, and closure of tunnels based on network needs.
Store and Reuse SSH Keys effectively within KeyManager to facilitate efficient tunnel management.
8.6. Testing and Validation
Unit Tests:
Create unit tests for individual classes and methods to ensure they function as expected.
Integration Tests:
Develop integration tests to simulate multi-node interactions, cluster formations, secure tunnel establishments, and routing scenarios.
Network Simulations:
Enhance NetworkSimulator to emulate various network topologies, node churn, message flooding, and secure tunnel disruptions.
Performance Testing:
Assess the system's performance under different loads and configurations to identify and optimize bottlenecks.
8.7. Documentation and Configuration Management
Comprehensive Documentation:
Provide detailed documentation for all classes, interfaces, methods, and configuration parameters.
External Configuration Files:
Define JSON files or objects to supply configurations such as centrality thresholds, routing weights, SSH keys, and other customizable settings.
Usage Guides:
Develop guides to help developers and users integrate, configure, and extend the networking package effectively.
8.8. Iterate and Refine
Continuous Feedback:
Gather feedback from initial tests and real-world deployments to identify areas for improvement.
Performance Optimization:
Profile the system to optimize routing efficiency, encryption processes, and secure tunnel management.
Scalability Enhancements:
Ensure the architecture supports scaling to larger networks with minimal performance degradation.
9. Conclusion
This detailed and refined architecture provides a solid foundation for your Java-based ad-hoc networking package, addressing all specified requirements with an emphasis on modularity, scalability, security, and maintainability. By implementing the outlined components and following the action plan, you can develop a robust system capable of dynamic cluster management, secure communications, and efficient multi-hop routing, ensuring a healthy and resilient network.

Feel free to reach out for further assistance, clarifications, or elaborations on specific components as you progress with your implementation!