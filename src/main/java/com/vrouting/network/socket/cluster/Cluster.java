package com.vrouting.network.socket.cluster;

import com.vrouting.network.Node;
import com.vrouting.network.socket.message.Message;

import java.util.*;

public class Cluster {
    private Node clusterHead;
    private Set<Node> members;
    private final String clusterId;

    public Cluster(Node clusterHead) {
        this.clusterHead = clusterHead;
        this.members = new HashSet<>();
        this.clusterId = UUID.randomUUID().toString();
        addMember(clusterHead);
        clusterHead.setClusterHead(true);
    }

    public String getClusterId() {
        return clusterId;
    }

    public Node getClusterHead() {
        return clusterHead;
    }

    public void setClusterHead(Node newHead) {
        if (newHead != null && members.contains(newHead)) {
            if (clusterHead != null) {
                clusterHead.setClusterHead(false);
            }
            clusterHead = newHead;
            clusterHead.setClusterHead(true);
        }
    }

    public void addMember(Node node) {
        if (node != null) {
            members.add(node);
        }
    }

    public void removeMember(Node node) {
        if (node != null) {
            members.remove(node);
            if (node.equals(clusterHead)) {
                electNewClusterHead();
            }
        }
    }

    public Set<Node> getMembers() {
        return Collections.unmodifiableSet(members);
    }

    public boolean containsMember(Node node) {
        return members.contains(node);
    }

    public int getSize() {
        return members.size();
    }

    private void electNewClusterHead() {
        if (members.isEmpty()) {
            clusterHead = null;
            return;
        }

        Node newHead = members.stream()
                .filter(Node::isActive)
                .filter(Node::isEligibleForClusterHead)
                .max(Comparator.comparingInt(node -> node.getNeighbors().size()))
                .orElse(null);

        if (newHead != null) {
            setClusterHead(newHead);
        }
    }

    public void broadcastMessage(Message message) {
        if (clusterHead != null && clusterHead.isActive()) {
            for (Node member : members) {
                if (member.isActive()) {
                    member.processMessage(message);
                }
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Cluster cluster = (Cluster) o;
        return clusterId.equals(cluster.clusterId);
    }

    @Override
    public int hashCode() {
        return clusterId.hashCode();
    }

    @Override
    public String toString() {
        return "Cluster{" +
                "clusterId='" + clusterId + '\'' +
                ", clusterHead=" + (clusterHead != null ? clusterHead.getId() : "null") +
                ", members=" + members.size() +
                '}';
    }
}
