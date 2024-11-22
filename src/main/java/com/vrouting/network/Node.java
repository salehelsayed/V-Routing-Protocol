package com.vrouting.network;

import java.util.UUID;

public class Node {
    private final String id;
    private final String name;
    private boolean isActive;
    private boolean isClusterHead;
    private HeartbeatPhase currentPhase;
    private RoutingTable routingTable;
    private ClusterManager clusterManager;

    public Node(String name) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.isActive = true;
        this.isClusterHead = false;
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

    public HeartbeatPhase getCurrentHeartbeatPhase() {
        return currentPhase;
    }

    public void updateHeartbeatPhase() {
        switch (currentPhase) {
            case DISCOVERY:
                currentPhase = HeartbeatPhase.STABILIZATION;
                break;
            case STABILIZATION:
                currentPhase = HeartbeatPhase.MAINTENANCE;
                break;
            case MAINTENANCE:
                // Stay in maintenance phase
                break;
        }
    }

    public void sendMessage(Message message) {
        if (!isActive) {
            throw new IllegalStateException("Cannot send message from inactive node");
        }
        // Message sending logic will be implemented here
        if (clusterManager != null) {
            clusterManager.propagateMessage(message, this);
        }
    }

    public void setClusterManager(ClusterManager manager) {
        this.clusterManager = manager;
    }

    public RoutingTable getRoutingTable() {
        return routingTable;
    }

    public void updateRoutingTable(RoutingEntry entry) {
        routingTable.updateEntry(entry);
    }
}
