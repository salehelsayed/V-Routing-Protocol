package com.vrouting.network.socket.core;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import com.vrouting.network.socket.message.Message;
import com.vrouting.network.Node;

public class PeerDirectory {
    private final Map<String, PeerInfo> peers;
    private final Map<String, Set<String>> adjacencyMatrix;
    private final ScheduledExecutorService executorService;

    public PeerDirectory() {
        this.peers = new ConcurrentHashMap<>();
        this.adjacencyMatrix = new ConcurrentHashMap<>();
        this.executorService = Executors.newSingleThreadScheduledExecutor();
        startCleanupTask();
    }

    private void startCleanupTask() {
        executorService.scheduleAtFixedRate(this::cleanupInactivePeers, 30, 30, TimeUnit.SECONDS);
    }

    public void addPeer(Node peer) {
        if (peer != null) {
            peers.put(peer.getId(), new PeerInfo(peer));
            adjacencyMatrix.putIfAbsent(peer.getId(), new HashSet<>());
        }
    }

    public void updatePeerLastSeen(String peerId) {
        PeerInfo peerInfo = peers.get(peerId);
        if (peerInfo != null) {
            peerInfo.updateLastSeen();
        }
    }

    public void addConnection(String fromPeerId, String toPeerId) {
        adjacencyMatrix.computeIfAbsent(fromPeerId, k -> new HashSet<>()).add(toPeerId);
        adjacencyMatrix.computeIfAbsent(toPeerId, k -> new HashSet<>()).add(fromPeerId);
    }

    public void removeConnection(String fromPeerId, String toPeerId) {
        Set<String> fromConnections = adjacencyMatrix.get(fromPeerId);
        Set<String> toConnections = adjacencyMatrix.get(toPeerId);
        
        if (fromConnections != null) fromConnections.remove(toPeerId);
        if (toConnections != null) toConnections.remove(fromPeerId);
    }

    public Set<String> getConnectedPeers(String peerId) {
        return adjacencyMatrix.getOrDefault(peerId, new HashSet<>());
    }

    public boolean isPeerActive(String peerId) {
        PeerInfo peerInfo = peers.get(peerId);
        return peerInfo != null && peerInfo.isActive();
    }

    public void removePeer(String peerId) {
        peers.remove(peerId);
        adjacencyMatrix.remove(peerId);
        adjacencyMatrix.values().forEach(connections -> connections.remove(peerId));
    }

    private void cleanupInactivePeers() {
        long now = System.currentTimeMillis();
        peers.entrySet().removeIf(entry -> {
            if (!entry.getValue().isActive(now)) {
                String peerId = entry.getKey();
                adjacencyMatrix.remove(peerId);
                adjacencyMatrix.values().forEach(connections -> connections.remove(peerId));
                return true;
            }
            return false;
        });
    }

    public void shutdown() {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    public double[][] getAdjacencyMatrixAsArray() {
        int size = peers.size();
        double[][] matrix = new double[size][size];
        List<String> peerIds = new ArrayList<>(peers.keySet());
        
        for (int i = 0; i < size; i++) {
            String peerId = peerIds.get(i);
            Set<String> connections = adjacencyMatrix.get(peerId);
            if (connections != null) {
                for (String connection : connections) {
                    int j = peerIds.indexOf(connection);
                    if (j != -1) {
                        matrix[i][j] = 1.0;
                    }
                }
            }
        }
        return matrix;
    }

    public Set<String> getAllPeerIds() {
        return new HashSet<>(peers.keySet());
    }

    private static class PeerInfo {
        private final Node peer;
        private long lastSeen;
        private static final long INACTIVE_THRESHOLD = 60000; // 60 seconds

        public PeerInfo(Node peer) {
            this.peer = peer;
            this.lastSeen = System.currentTimeMillis();
        }

        public void updateLastSeen() {
            this.lastSeen = System.currentTimeMillis();
        }

        public boolean isActive() {
            return isActive(System.currentTimeMillis());
        }

        public boolean isActive(long now) {
            return (now - lastSeen) < INACTIVE_THRESHOLD;
        }

        public Node getPeer() {
            return peer;
        }
    }

    public int getNodeIndex(String nodeId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getNodeIndex'");
    }
}
