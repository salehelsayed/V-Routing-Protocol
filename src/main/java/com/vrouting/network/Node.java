package com.vrouting.network;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.vrouting.network.socket.config.NetworkConfig;
import com.vrouting.network.socket.message.Message;
import com.vrouting.network.socket.message.MessageHandler;
import com.vrouting.network.socket.message.MessageHandlerImpl;
import com.vrouting.network.socket.message.MessageType;
import com.vrouting.network.socket.cluster.CentralityCalculator;
import com.vrouting.network.socket.core.Phase;
import com.vrouting.network.socket.core.PeerDirectory;
import com.vrouting.network.socket.core.NodeState;
import com.vrouting.network.socket.core.HeartbeatManager;
import com.vrouting.network.socket.core.RoutingManager;
import com.vrouting.network.socket.core.MessageDispatcher;
import com.vrouting.network.socket.core.NodeMetrics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

public class Node {
    private static final Logger logger = LoggerFactory.getLogger(Node.class);

    private final String id;
    private final String name;
    private boolean isActive;
    private boolean isClusterHead;
    private HeartbeatPhase currentPhase;
    private RoutingTable routingTable;
    private ClusterManager clusterManager;
    private PeerDirectory peerDirectory;
    @JsonManagedReference
    private Set<Node> neighbors = new HashSet<>();

    private final String nodeId;
    private final NetworkConfig config;
    private final AtomicReference<NodeState> state;
    private final AtomicReference<Phase> phase;
    private final MessageHandler messageHandler;
    private final HeartbeatManager heartbeatManager;
    private final RoutingManager routingManager;
    private final MessageDispatcher messageDispatcher;
    private final CentralityCalculator centralityCalculator;
    private NodeMetrics metrics;

    public Node(String name, NetworkConfig config) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.isActive = true;
        this.nodeId = UUID.randomUUID().toString();
        this.config = config;
        this.state = new AtomicReference<>(NodeState.DISCOVERY);
        this.phase = new AtomicReference<>(Phase.DISCOVERY);
        this.peerDirectory = new PeerDirectory();
        this.messageHandler = new MessageHandlerImpl(this);
        this.heartbeatManager = new HeartbeatManager(this);
        this.routingManager = new RoutingManager(this);
        this.messageDispatcher = new MessageDispatcher(this);
        this.centralityCalculator = new CentralityCalculator(this, peerDirectory);
        this.metrics = new NodeMetrics();
        this.currentPhase = HeartbeatPhase.DISCOVERY;
        this.routingTable = new RoutingTable();
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public boolean isActive() {
        return isActive;
    }

    public void markAsFailed() {
        this.isActive = false;
    }

    public void recover() {
        this.isActive = true;
    }

    public boolean isClusterHead() {
        return isClusterHead;
    }

    public void setClusterHead(boolean clusterHead) {
        isClusterHead = clusterHead;
    }

    public HeartbeatPhase getCurrentPhase() {
        return currentPhase;
    }

    public void setCurrentPhase(HeartbeatPhase phase) {
        this.currentPhase = phase;
    }

    public void setClusterManager(ClusterManager clusterManager) {
        this.clusterManager = clusterManager;
    }

    public ClusterManager getClusterManager() {
        return clusterManager;
    }

    public PeerDirectory getPeerDirectory() {
        return peerDirectory;
    }

    public void addNeighbor(Node node) {
        if (node != null && !node.equals(this)) {
            neighbors.add(node);
            node.neighbors.add(this);
        }
    }

    public void removeNeighbor(Node node) {
        if (node != null) {
            neighbors.remove(node);
            node.neighbors.remove(this);
        }
    }

    public Set<Node> getNeighbors() {
        return Collections.unmodifiableSet(neighbors);
    }

    public RoutingTable getRoutingTable() {
        return routingTable;
    }

    public void updateRoutingTable(RoutingEntry entry) {
        routingTable.updateEntry(entry);
    }

    public String getNodeId() {
        return nodeId;
    }

    public Message processMessage(Message message) {
        return messageHandler.handleMessage(message);
    }

    public void sendMessage(Message message) {
        messageDispatcher.dispatch(message);
    }

    public void broadcast(Message message) {
        for (String peerId : peerDirectory.getPeerIds()) {
            sendMessage(new Message(this.nodeId, peerId, message.getType(), message.getPayload()));
        }
    }

    public void onStop() {
        // Add any cleanup code here
    }

    public void onPhaseChange(Phase newPhase) {
        this.phase.set(newPhase);
    }

    public void updatePeerInfo(String peerId, Message message) {
        // Implement logic for updating peer information
    }

    public int getDepth() {
        return 0; // Placeholder
    }

    public NodeMetrics getMetrics() {
        return this.metrics;
    }

    protected MessageHandler createMessageHandler() {
        return new MessageHandlerImpl(this);
    }

    public boolean isEligibleForClusterHead() {
        // Implement logic to determine if the node is eligible to be a cluster head
        // Example criteria: node is active and has a sufficient number of neighbors
        return isActive && neighbors.size() >= 2;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Node node = (Node) o;
        return id.equals(node.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return "Node{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", isActive=" + isActive +
                ", isClusterHead=" + isClusterHead +
                ", phase=" + currentPhase +
                '}';
    }
    public RoutingManager getRoutingManager() {
        return routingManager;
    }

    public void setRoutingManager(RoutingManager routingManager) {
        this.routingManager = routingManager;
    }
}
