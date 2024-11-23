package com.vrouting.network;

import java.util.*;

public class Cluster {
    private final String id;
    private final String name;
    private final Set<Node> nodes;
    private Node clusterHead;
    private final Map<String, SecurityTunnel> tunnels;

    public Cluster(String name) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.nodes = new HashSet<>();
        this.tunnels = new HashMap<>();
    }

    public void addNode(Node node) {
        nodes.add(node);
    }

    public void removeNode(Node node) {
        nodes.remove(node);
        if (node == clusterHead) {
            clusterHead = null;
        }
    }

    public boolean containsNode(Node node) {
        return nodes.contains(node);
    }

    public int getNodeCount() {
        return nodes.size();
    }

    public Node getClusterHead() {
        return clusterHead;
    }

    public void promoteToClusterHead(Node node) {
        if (!nodes.contains(node)) {
            throw new IllegalArgumentException("Node must be part of the cluster");
        }
        if (clusterHead != null) {
            clusterHead.setClusterHead(false);
        }
        clusterHead = node;
        node.setClusterHead(true);
    }

    public SecurityTunnel establishTunnelWith(Cluster otherCluster) {
        if (clusterHead == null || otherCluster.getClusterHead() == null) {
            throw new IllegalStateException("Both clusters must have cluster heads");
        }
        SecurityTunnel tunnel = new SecurityTunnel(this, otherCluster);
        tunnels.put(otherCluster.getId(), tunnel);
        return tunnel;
    }

    public boolean sendMessageToCluster(Cluster targetCluster, Message message) {
        SecurityTunnel tunnel = tunnels.get(targetCluster.getId());
        if (tunnel == null || !tunnel.isEstablished()) {
            return false;
        }
        // Message sending logic will be implemented here
        return true;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Set<Node> getNodes() {
        return new HashSet<>(nodes);
    }
}
