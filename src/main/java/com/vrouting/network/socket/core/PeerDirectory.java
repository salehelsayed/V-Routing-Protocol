package com.vrouting.network.socket.core;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import com.vrouting.network.socket.message.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PeerDirectory {
    private static final Logger logger = LoggerFactory.getLogger(PeerDirectory.class);

    private final Node node;
    private final Map<String, PeerInfo> peers;
    private final Map<String, Set<String>> adjacencyMatrix;
    private final ScheduledExecutorService scheduler;
    private NodeState state;
    
    // Constants for peer management
    private static final long CLEANUP_INTERVAL = 60000; // 1 minute
    private static final long MAX_PEER_AGE = 300000;   // 5 minutes
    private static final long ROUTE_HISTORY_MAX_AGE = 600000; // 10 minutes
    
    public PeerDirectory(Node node) {
        this.node = node;
        this.peers = new ConcurrentHashMap<>();
        this.adjacencyMatrix = new ConcurrentHashMap<>();
        this.scheduler = Executors.newSingleThreadScheduledExecutor();
        this.state = NodeState.DISCOVERY;
        
        // Schedule periodic cleanup
        scheduler.scheduleAtFixedRate(
            () -> cleanupStaleEntries(MAX_PEER_AGE),
            CLEANUP_INTERVAL,
            CLEANUP_INTERVAL,
            TimeUnit.MILLISECONDS
        );
    }
    
    public void updatePeer(String peerId, Message message) {
        PeerInfo info = peers.computeIfAbsent(peerId, k -> new PeerInfo(peerId));
        info.setLastSeen(System.currentTimeMillis());
        info.setMessage(message);
        info.addToRouteHistory(message);
        
        // Update adjacency matrix
        updateAdjacencyMatrix(peerId, message);
        
        // Check if we should recalculate centrality
        if (shouldRecalculateCentrality()) {
            node.recalculateCentrality();
        }
    }
    
    private boolean shouldRecalculateCentrality() {
        // Recalculate if we're in STABILIZATION phase and have enough peers
        return node.getCurrentPhase() == Phase.STABILIZATION && 
               peers.size() >= 3 && // Minimum peers for meaningful centrality
               !node.isClusterHead();
    }
    
    public void updatePeer(String peerId, Phase phase, int depth, long timestamp) {
        PeerInfo info = peers.computeIfAbsent(peerId, k -> new PeerInfo(peerId));
        info.setLastSeen(timestamp);
        info.setPhase(phase);
        info.setDepth(depth);
    }
    
    public void updatePeerMetrics(String peerId, NodeMetrics metrics) {
        PeerInfo info = peers.get(peerId);
        if (info != null) {
            info.setMetrics(metrics);
        }
    }
    
    public Set<String> getAllPeers() {
        return new HashSet<>(peers.keySet());
    }
    
    public PeerInfo getPeerInfo(String peerId) {
        return peers.get(peerId);
    }
    
    public void removePeer(String peerId) {
        peers.remove(peerId);
        adjacencyMatrix.remove(peerId);
        adjacencyMatrix.values().forEach(connections -> connections.remove(peerId));
    }
    
    public void updateAdjacencyMatrix(String peerId, Message message) {
        Set<String> connections = adjacencyMatrix.computeIfAbsent(peerId, k -> new HashSet<>());
        connections.add(node.getNodeId());
        
        // Add bidirectional connection
        Set<String> myConnections = adjacencyMatrix.computeIfAbsent(node.getNodeId(), k -> new HashSet<>());
        myConnections.add(peerId);
        
        // Update connections based on message route history
        if (message.getRouteHistory() != null) {
            for (int i = 0; i < message.getRouteHistory().size() - 1; i++) {
                String source = message.getRouteHistory().get(i);
                String target = message.getRouteHistory().get(i + 1);
                
                Set<String> sourceConns = adjacencyMatrix.computeIfAbsent(source, k -> new HashSet<>());
                sourceConns.add(target);
                
                Set<String> targetConns = adjacencyMatrix.computeIfAbsent(target, k -> new HashSet<>());
                targetConns.add(source);
            }
        }
    }
    
    public Map<String, Set<String>> getAdjacencyMatrix() {
        return new HashMap<>(adjacencyMatrix);
    }
    
    public double[][] getAdjacencyMatrixAsArray() {
        List<String> nodeIds = new ArrayList<>(adjacencyMatrix.keySet());
        int size = nodeIds.size();
        double[][] matrix = new double[size][size];
        
        for (int i = 0; i < size; i++) {
            Set<String> connections = adjacencyMatrix.get(nodeIds.get(i));
            if (connections != null) {
                for (int j = 0; j < size; j++) {
                    matrix[i][j] = connections.contains(nodeIds.get(j)) ? 1.0 : 0.0;
                }
            }
        }
        
        return matrix;
    }
    
    public Set<String> getConnectedPeers(String nodeId) {
        return adjacencyMatrix.getOrDefault(nodeId, new HashSet<>());
    }
    
    public int getNodeIndex(String nodeId) {
        List<String> nodeIds = new ArrayList<>(adjacencyMatrix.keySet());
        return nodeIds.indexOf(nodeId);
    }
    
    public void cleanupStaleEntries(long maxAge) {
        long now = System.currentTimeMillis();
        peers.entrySet().removeIf(entry -> 
            now - entry.getValue().getLastSeen() > maxAge
        );
        
        // Clean up route history
        peers.values().forEach(peer -> 
            peer.cleanupRouteHistory(ROUTE_HISTORY_MAX_AGE)
        );
    }
    
    public void stop() {
        scheduler.shutdown();
        try {
            scheduler.awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    public static class PeerInfo {
        private final String peerId;
        private long lastSeen;
        private Message lastMessage;
        private Phase phase;
        private int depth;
        private NodeMetrics metrics;
        private final List<RouteHistoryEntry> routeHistory;
        
        public PeerInfo(String peerId) {
            this.peerId = peerId;
            this.lastSeen = System.currentTimeMillis();
            this.routeHistory = new ArrayList<>();
        }
        
        public void addToRouteHistory(Message message) {
            if (message.getRouteHistory() != null) {
                routeHistory.add(new RouteHistoryEntry(
                    message.getRouteHistory(),
                    System.currentTimeMillis()
                ));
            }
        }
        
        public void cleanupRouteHistory(long maxAge) {
            long now = System.currentTimeMillis();
            routeHistory.removeIf(entry -> now - entry.timestamp > maxAge);
        }
        
        // Getters and setters
        public String getPeerId() { return peerId; }
        public long getLastSeen() { return lastSeen; }
        public void setLastSeen(long lastSeen) { this.lastSeen = lastSeen; }
        public Message getMessage() { return lastMessage; }
        public void setMessage(Message message) { this.lastMessage = message; }
        public Phase getPhase() { return phase; }
        public void setPhase(Phase phase) { this.phase = phase; }
        public int getDepth() { return depth; }
        public void setDepth(int depth) { this.depth = depth; }
        public NodeMetrics getMetrics() { return metrics; }
        public void setMetrics(NodeMetrics metrics) { this.metrics = metrics; }
        public List<RouteHistoryEntry> getRouteHistory() { return new ArrayList<>(routeHistory); }
    }
    
    private static class RouteHistoryEntry {
        private final List<String> route;
        private final long timestamp;
        
        public RouteHistoryEntry(List<String> route, long timestamp) {
            this.route = new ArrayList<>(route);
            this.timestamp = timestamp;
        }
        
        public List<String> getRoute() { return new ArrayList<>(route); }
        public long getTimestamp() { return timestamp; }
    }
}
