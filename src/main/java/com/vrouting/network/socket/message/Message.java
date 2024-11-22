package com.vrouting.network.socket.message;

import com.vrouting.network.socket.core.Phase;
import com.vrouting.network.socket.core.NodeMetrics;

import java.io.Serializable;
import java.util.*;
import com.google.gson.Gson;

/**
 * Represents a message in the V-Routing Protocol.
 * Messages contain routing information, payload data, and metadata for network management.
 */
public class Message implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final Gson gson = new Gson();
    
    private final String id;
    private final String sourceNodeId;
    private String destinationNodeId; 
    private final MessageType type;
    private final List<String> routeHistory;
    private final Map<String, String> payload;
    private int hopCount;
    private static final int MAX_HOPS = 10;
    
    /**
     * Creates a new message with the specified parameters.
     */
    public Message(String sourceNodeId, String destinationNodeId, MessageType type) {
        this.id = UUID.randomUUID().toString();
        this.sourceNodeId = sourceNodeId;
        this.destinationNodeId = destinationNodeId;
        this.type = type;
        this.routeHistory = new ArrayList<>();
        this.payload = new HashMap<>();
        this.hopCount = 0;
    }
    
    public static Message createHeartbeat(String sourceNodeId, MessageType type) {
        if (!isHeartbeatType(type)) {
            throw new IllegalArgumentException("Invalid heartbeat message type: " + type);
        }
        return new Message(sourceNodeId, null, type);
    }
    
    public static Message createClusterResponse(String sourceNodeId, String destinationNodeId, 
                                              MessageType type, String message) {
        Message response = new Message(sourceNodeId, destinationNodeId, type);
        response.setPayload("message", message);
        return response;
    }
    
    private static boolean isHeartbeatType(MessageType type) {
        return type == MessageType.HEARTBEAT_DISCOVERY ||
               type == MessageType.HEARTBEAT_STABLE ||
               type == MessageType.HEARTBEAT_UPDATE;
    }
    
    public String getId() {
        return id;
    }
    
    public String getSourceNodeId() {
        return sourceNodeId;
    }
    
    public String getSource() { 
        return sourceNodeId; 
    }  
    
    public String getDestinationNodeId() {
        return destinationNodeId;
    }
    
    public void setDestinationNodeId(String destinationNodeId) {
        this.destinationNodeId = destinationNodeId;
    }
    
    public MessageType getType() {
        return type;
    }
    
    public List<String> getRouteHistory() {
        return Collections.unmodifiableList(routeHistory);
    }
    
    public void addToRoute(String nodeId) {
        if (routeHistory.size() >= MAX_HOPS) {
            throw new IllegalStateException("Maximum hop count exceeded");
        }
        routeHistory.add(nodeId);
        hopCount++;
    }
    
    public int getHopCount() {
        return hopCount;
    }
    
    public int getMaxHops() {
        return MAX_HOPS;
    }
    
    public <T> void setPayload(String key, T value) {
        payload.put(key, gson.toJson(value));
    }
    
    public <T> T getPayload(String key, Class<T> type) {
        String json = payload.get(key);
        return json != null ? gson.fromJson(json, type) : null;
    }
    
    public boolean hasVisited(String nodeId) {
        return routeHistory.contains(nodeId);
    }
    
    public void setPhase(Phase phase) {
        payload.put("phase", gson.toJson(phase));
    }
    
    public void setDepth(int depth) {
        payload.put("depth", gson.toJson(depth));
    }
    
    public void setMetrics(NodeMetrics metrics) {
        payload.put("metrics", gson.toJson(metrics));
    }
    
    public int getDepth() {
        return gson.fromJson(payload.get("depth"), Integer.class);
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Message message = (Message) o;
        return id.equals(message.id);
    }
    
    @Override
    public int hashCode() {
        return java.util.Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return String.format("Message[id=%s, type=%s, source=%s, destination=%s, hops=%d]",
                           id, type, sourceNodeId, destinationNodeId, hopCount);
    }
    
    public Message copy() {
        Message copy = new Message(sourceNodeId, destinationNodeId, type);
        copy.routeHistory.addAll(this.routeHistory);
        copy.payload.putAll(this.payload);
        copy.hopCount = this.hopCount;
        return copy;
    }
}
