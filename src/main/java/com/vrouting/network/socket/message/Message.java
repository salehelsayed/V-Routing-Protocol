package com.vrouting.network.socket.message;

import com.vrouting.network.socket.routing.RoutingEntry;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Message {
    private String id;
    private String sourceId;
    private String destinationId;
    private MessageType type;
    private RoutingEntry routingEntry;
    private List<String> routeHistory;
    private Object payload;
    private int hopCount;
    private int maxHops;
    private Phase phase;
    private int depth;
    private NodeMetrics metrics;

    public Message() {
        this.id = UUID.randomUUID().toString();
        this.routeHistory = new ArrayList<>();
        this.hopCount = 0;
        this.maxHops = 10; // Default max hops
    }

    public Message(String sourceId, String destinationId, MessageType type, Object payload) {
        this();
        this.sourceId = sourceId;
        this.destinationId = destinationId;
        this.type = type;
        this.payload = payload;
    }

    public String getId() {
        return id;
    }

    public String getSourceId() {
        return sourceId;
    }

    public String getSourceNodeId() {
        return sourceId;
    }

    public void setSourceId(String sourceId) {
        this.sourceId = sourceId;
    }

    public String getDestinationId() {
        return destinationId;
    }

    public String getDestinationNodeId() {
        return destinationId;
    }

    public void setDestinationId(String destinationId) {
        this.destinationId = destinationId;
    }

    public void setDestinationNodeId(String destinationId) {
        this.destinationId = destinationId;
    }

    public MessageType getType() {
        return type;
    }

    public void setType(MessageType type) {
        this.type = type;
    }

    public RoutingEntry getRoutingEntry() {
        return routingEntry;
    }

    public void setRoutingEntry(RoutingEntry routingEntry) {
        this.routingEntry = routingEntry;
    }

    public List<String> getRouteHistory() {
        return routeHistory;
    }

    public void addToRoute(String nodeId) {
        this.routeHistory.add(nodeId);
        this.hopCount++;
    }

    public Object getPayload() {
        return payload;
    }

    public void setPayload(Object payload) {
        this.payload = payload;
    }

    public int getHopCount() {
        return hopCount;
    }

    public int getMaxHops() {
        return maxHops;
    }

    public void setMaxHops(int maxHops) {
        this.maxHops = maxHops;
    }

    public Phase getPhase() {
        return phase;
    }

    public void setPhase(Phase phase) {
        this.phase = phase;
    }

    public int getDepth() {
        return depth;
    }

    public void setDepth(int depth) {
        this.depth = depth;
    }

    public NodeMetrics getMetrics() {
        return metrics;
    }

    public void setMetrics(NodeMetrics metrics) {
        this.metrics = metrics;
    }

    public Message copy() {
        Message copy = new Message(sourceId, destinationId, type);
        copy.routingEntry = this.routingEntry;
        copy.routeHistory = new ArrayList<>(this.routeHistory);
        copy.payload = this.payload;
        copy.hopCount = this.hopCount;
        copy.maxHops = this.maxHops;
        copy.phase = this.phase;
        copy.depth = this.depth;
        copy.metrics = this.metrics;
        return copy;
    }

    public static Message createClusterResponse(String sourceId, String destinationId, MessageType type, String clusterId) {
        Message response = new Message(sourceId, destinationId, type);
        response.setPayload(clusterId);
        return response;
    }
}
