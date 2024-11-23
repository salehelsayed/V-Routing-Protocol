package com.vrouting.network.socket.cluster;

import com.vrouting.network.socket.core.Node;
import com.vrouting.network.socket.message.Message;
import com.vrouting.network.socket.message.MessageType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.*;
import java.util.concurrent.*;

public class ClusterManager {
    private static final Logger logger = LoggerFactory.getLogger(ClusterManager.class);
    private final Node node;
    private final CentralityCalculator centralityCalculator;
    private String clusterHeadId;
    private final Map<String, Long> clusterMembers;
    private final ScheduledExecutorService scheduler;
    
    public ClusterManager(Node node) {
        this.node = node;
        this.centralityCalculator = new CentralityCalculator(node, node.getPeerDirectory());
        this.clusterMembers = new ConcurrentHashMap<>();
        this.scheduler = Executors.newSingleThreadScheduledExecutor();
    }
    
    public void start() {
        scheduler.scheduleAtFixedRate(this::performMaintenance, 0, 30, TimeUnit.SECONDS);
    }
    
    private void performMaintenance() {
        try {
            // Remove expired members
            long now = System.currentTimeMillis();
            clusterMembers.entrySet().removeIf(entry ->
                now - entry.getValue() > TimeUnit.MINUTES.toMillis(5));
            
            // Check if we should become cluster head
            if (clusterHeadId == null || !clusterMembers.containsKey(clusterHeadId)) {
                evaluateClusterHeadRole();
            }
        } catch (Exception e) {
            logger.error("Error during cluster maintenance", e);
        }
    }
    
    public void handleClusterMessage(Message message) {
        if (message == null) {
            logger.warn("Received null cluster message");
            return;
        }
        
        try {
            switch (message.getType()) {
                case JOIN:
                    handleJoinRequest(message);
                    break;
                case LEAVE:
                    handleLeaveRequest(message);
                    break;
                case UPDATE:
                    handleClusterUpdate(message);
                    break;
                default:
                    logger.warn("Unknown cluster message type: {}", message.getType());
            }
        } catch (Exception e) {
            logger.error("Error handling cluster message", e);
        }
    }
    
    private void handleJoinRequest(Message message) {
        String sourceId = message.getSourceNodeId();
        if (sourceId == null) {
            logger.warn("Join request has no source ID");
            return;
        }
        
        if (node.getNodeId().equals(clusterHeadId)) {
            // We are the cluster head, process join request
            clusterMembers.put(sourceId, System.currentTimeMillis());
            Message response = Message.createClusterResponse(
                node.getNodeId(),
                sourceId,
                MessageType.ACK,
                "Join request accepted"
            );
            node.sendMessage(response);
            logger.info("Accepted join request from node: {}", sourceId);
        }
    }
    
    private void handleLeaveRequest(Message message) {
        String sourceId = message.getSourceNodeId();
        if (sourceId != null && clusterMembers.remove(sourceId) != null) {
            logger.info("Node {} left the cluster", sourceId);
        }
    }
    
    private void handleClusterUpdate(Message message) {
        // Handle updates about cluster state
        String newClusterHead = message.getSourceNodeId();
        if (newClusterHead != null && !newClusterHead.equals(clusterHeadId)) {
            clusterHeadId = newClusterHead;
            logger.info("Updated cluster head to: {}", newClusterHead);
        }
    }
    
    public void stop() {
        logger.info("Stopping cluster manager");
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.warn("Interrupted while stopping cluster manager", e);
        }
    }
    
    public boolean isClusterHead() {
        return node.getNodeId().equals(clusterHeadId);
    }
    
    public String getClusterHeadId() {
        return clusterHeadId;
    }
    
    public Set<String> getClusterMembers() {
        return new HashSet<>(clusterMembers.keySet());
    }
    
    public void evaluateClusterHeadRole() {
        if (centralityCalculator.isEligibleForClusterHead()) {
            clusterHeadId = node.getNodeId();
            broadcastClusterHeadStatus();
        }
    }
    
    private void broadcastClusterHeadStatus() {
        Message announcement = Message.createClusterResponse(
            node.getNodeId(),
            null, // broadcast to all
            MessageType.UPDATE,
            "New cluster head"
        );
        node.broadcast(announcement);
    }
}
