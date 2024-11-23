package com.vrouting.network.socket.core;

import com.vrouting.network.socket.message.Message;
import com.vrouting.network.socket.message.MessageType;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.Map;
import java.util.HashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HeartbeatManager {
    private static final Logger logger = LoggerFactory.getLogger(HeartbeatManager.class);
    private final Node node;
    private ScheduledExecutorService scheduler;
    private final Map<Phase, Long> phaseIntervals;
    private Phase currentPhase;
    private long currentInterval;
    private final AtomicInteger heartbeatCount;
    
    // Constants for heartbeat phases
    private static final long INITIAL_INTERVAL = 1000; // 1 second
    private static final long STABLE_INTERVAL = 30000; // 30 seconds
    private static final long MAX_INTERVAL = 60000;    // 1 minute
    private static final int PHASE1_THRESHOLD = 30;    // Number of heartbeats before moving to phase 2
    private static final int PHASE2_THRESHOLD = 60;    // Number of heartbeats before moving to phase 3
    
    public HeartbeatManager(Node node) {
        this.node = node;
        this.scheduler = Executors.newSingleThreadScheduledExecutor();
        this.phaseIntervals = new HashMap<>();
        this.heartbeatCount = new AtomicInteger(0);
        
        initializePhaseIntervals();
        this.currentPhase = Phase.DISCOVERY;
        this.currentInterval = INITIAL_INTERVAL;
    }
    
    private void initializePhaseIntervals() {
        phaseIntervals.put(Phase.DISCOVERY, INITIAL_INTERVAL);
        phaseIntervals.put(Phase.STABILIZATION, STABLE_INTERVAL);
        phaseIntervals.put(Phase.REGULAR, MAX_INTERVAL);
    }
    
    public void start() {
        logger.info("Starting heartbeat manager with initial interval: {} ms", currentInterval);
        scheduler.scheduleAtFixedRate(this::sendHeartbeat, 0, currentInterval, TimeUnit.MILLISECONDS);
    }
    
    private void sendHeartbeat() {
        try {
            MessageType heartbeatType;
            switch (currentPhase) {
                case DISCOVERY:
                    heartbeatType = MessageType.HEARTBEAT_DISCOVERY;
                    break;
                case STABILIZATION:
                    heartbeatType = MessageType.HEARTBEAT_STABLE;
                    break;
                case REGULAR:
                    heartbeatType = MessageType.HEARTBEAT_UPDATE;
                    break;
                default:
                    heartbeatType = MessageType.HEARTBEAT_STABLE;
            }
            
            // Create and send heartbeat message
            Message heartbeat = new Message(node.getNodeId(), "broadcast", heartbeatType);
            node.broadcast(heartbeat);
            
            // Update phase based on heartbeat count
            updatePhase();
        } catch (Exception e) {
            logger.error("Failed to send heartbeat", e);
        }
    }
    
    private void updatePhase() {
        int count = heartbeatCount.incrementAndGet();
        
        if (currentPhase == Phase.DISCOVERY && count >= PHASE1_THRESHOLD) {
            transitionToPhase(Phase.STABILIZATION);
        } else if (currentPhase == Phase.STABILIZATION && count >= PHASE2_THRESHOLD) {
            // Check if node is eligible to become cluster head
            if (node.isEligibleForClusterHead()) {
                transitionToPhase(Phase.REGULAR);
            }
        }
    }
    
    private void transitionToPhase(Phase newPhase) {
        this.currentPhase = newPhase;
        this.currentInterval = phaseIntervals.get(newPhase);
        
        // Reschedule heartbeats with new interval
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.warn("Interrupted while shutting down heartbeat scheduler", e);
        }
        
        scheduler = Executors.newSingleThreadScheduledExecutor();
        start();
        
        // Notify node of phase change
        node.onPhaseChange(newPhase);
    }
    
    public Message handleHeartbeat(Message message) {
        // Process incoming heartbeat
        node.updatePeerInfo(message.getSourceNodeId(), message);
        
        // Create and return heartbeat response
        Message response = new Message(node.getNodeId(), message.getSourceNodeId(), MessageType.HEARTBEAT_RESPONSE);
        response.setPhase(currentPhase);
        response.setDepth(node.getDepth());
        response.setMetrics(node.getMetrics());
        return response;
    }
    
    public void handleHeartbeatResponse(Message response) {
        // Process heartbeat response
        // Update routing table and peer directory
        node.updatePeerInfo(response.getSourceNodeId(), response);
    }
    
    public void stop() {
        logger.info("Stopping heartbeat manager");
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.warn("Interrupted while stopping heartbeat manager", e);
        }
    }
    
    public Phase getCurrentPhase() {
        return currentPhase;
    }
    
    public long getCurrentInterval() {
        return currentInterval;
    }
}
