package com.vrouting.network.socket.core;

import com.vrouting.network.socket.config.NetworkConfig;
import com.vrouting.network.socket.message.Message;
import com.vrouting.network.socket.message.MessageHandler;
import com.vrouting.network.socket.message.MessageHandlerImpl;
import com.vrouting.network.socket.message.MessageType;
import com.vrouting.network.socket.cluster.CentralityCalculator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.concurrent.atomic.AtomicReference;

public abstract class Node {
    private static final Logger logger = LoggerFactory.getLogger(Node.class);
    
    private final String nodeId;
    private final NetworkConfig config;
    private final AtomicReference<NodeState> state;
    private final AtomicReference<Phase> phase;
    private final PeerDirectory peerDirectory;
    private final MessageHandler messageHandler;
    private final HeartbeatManager heartbeatManager;
    private final RoutingManager routingManager;
    private final MessageDispatcher messageDispatcher;
    private final CentralityCalculator centralityCalculator;
    private NodeMetrics metrics;
    private boolean isClusterHead;
    
    protected Node(String nodeId, NetworkConfig config) {
        this.nodeId = nodeId;
        this.config = config;
        this.state = new AtomicReference<>(NodeState.DISCOVERY);
        this.phase = new AtomicReference<>(Phase.DISCOVERY);
        this.peerDirectory = new PeerDirectory(this);
        this.messageHandler = new MessageHandlerImpl(this);
        this.heartbeatManager = new HeartbeatManager(this);
        this.routingManager = new RoutingManager(this);
        this.messageDispatcher = new MessageDispatcher(this);
        this.centralityCalculator = new CentralityCalculator(this, peerDirectory);
        this.metrics = new NodeMetrics();
        this.isClusterHead = false;
    }
    
    public void start() {
        messageHandler.start();
        heartbeatManager.start();
        routingManager.start();
        messageDispatcher.start();
        onStart();
        logger.info("Node {} started", nodeId);
    }
    
    public void stop() {
        messageHandler.stop();
        heartbeatManager.stop();
        routingManager.stop();
        messageDispatcher.stop();
        onStop();
        logger.info("Node {} stopped", nodeId);
    }
    
    public void broadcast(Message message) {
        messageDispatcher.broadcast(message);
    }
    
    public void sendMessage(Message message) {
        messageDispatcher.dispatch(message);
    }
    
    public Message processMessage(Message message) {
        return messageHandler.handleMessage(message);
    }
    
    public void onPhaseChange(Phase newPhase) {
        phase.set(newPhase);
        logger.info("Node {} phase changed to {}", nodeId, newPhase);
    }
    
    public String getNodeId() {
        return nodeId;
    }
    
    public NetworkConfig getConfig() {
        return config;
    }
    
    public NodeState getState() {
        return state.get();
    }
    
    public Phase getPhase() {
        return phase.get();
    }
    
    public PeerDirectory getPeerDirectory() {
        return peerDirectory;
    }
    
    public MessageHandler getMessageHandler() {
        return messageHandler;
    }
    
    public HeartbeatManager getHeartbeatManager() {
        return heartbeatManager;
    }
    
    public RoutingManager getRoutingManager() {
        return routingManager;
    }
    
    public MessageDispatcher getMessageDispatcher() {
        return messageDispatcher;
    }
    
    public CentralityCalculator getCentralityCalculator() {
        return centralityCalculator;
    }
    
    public NodeMetrics getMetrics() {
        return metrics;
    }
    
    public Phase getCurrentPhase() {
        return phase.get();
    }

    public boolean isClusterHead() {
        return isClusterHead;
    }

    public void setClusterHead(boolean isClusterHead) {
        this.isClusterHead = isClusterHead;
    }

    public boolean isEligibleForClusterHead() {
        return centralityCalculator.isEligibleForClusterHead();
    }

    public void recalculateCentrality() {
        centralityCalculator.recalculateCentrality();
    }

    public void updatePeerInfo(String nodeId, Message message) {
        peerDirectory.updatePeerInfo(nodeId, message);
    }

    public Message handleHeartbeat(Message message) {
        Message result = heartbeatManager.handleHeartbeat(message);
        return result != null ? result : new Message(nodeId, message.getSourceNodeId(), MessageType.HEARTBEAT_ACK);
    }

    public Message handleRouteRequest(Message message) {
        Message result = routingManager.handleRouteRequest(message);
        return result != null ? result : new Message(nodeId, message.getSourceNodeId(), MessageType.ROUTE_REPLY);
    }

    public Message handleRouteReply(Message message) {
        Message result = routingManager.handleRouteReply(message);
        return result != null ? result : new Message(nodeId, message.getSourceNodeId(), MessageType.ROUTE_REPLY_ACK);
    }

    public Message handleData(Message message) {
        Message result = messageDispatcher.handleData(message);
        return result != null ? result : new Message(nodeId, message.getSourceNodeId(), MessageType.DATA_ACK);
    }

    public int getDepth() {
        return metrics != null ? (int) metrics.getProcessingCapacity() : 0;
    }

    protected abstract MessageHandler createMessageHandler();
    protected abstract void onStart();
    protected abstract void onStop();
}
