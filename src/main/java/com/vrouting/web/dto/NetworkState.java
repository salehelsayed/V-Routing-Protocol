package com.vrouting.web.dto;

import com.vrouting.network.Node;
import com.vrouting.simulation.SimulationMetrics;

import java.util.List;
import java.util.Objects;

public class NetworkState {
    private final List<Node> nodes;
    private final SimulationMetrics metrics;
    private NetworkState previousState;

    private NetworkState(Builder builder) {
        this.nodes = builder.nodes;
        this.metrics = builder.metrics;
    }

    public List<Node> getNodes() {
        return nodes;
    }

    public SimulationMetrics getMetrics() {
        return metrics;
    }

    public boolean hasChanges() {
        if (previousState == null) {
            return true;
        }

        // Check if node states have changed
        if (nodes.size() != previousState.nodes.size()) {
            return true;
        }

        for (int i = 0; i < nodes.size(); i++) {
            Node currentNode = nodes.get(i);
            Node previousNode = previousState.nodes.get(i);
            
            if (!Objects.equals(currentNode.getId(), previousNode.getId()) ||
                !Objects.equals(currentNode.getState(), previousNode.getState())) {
                return true;
            }
        }

        // Check if metrics have changed
        return !Objects.equals(metrics.getActiveNodeCount(), previousState.metrics.getActiveNodeCount()) ||
               !Objects.equals(metrics.getClusterCount(), previousState.metrics.getClusterCount()) ||
               !Objects.equals(metrics.getMessageDeliveryRate(), previousState.metrics.getMessageDeliveryRate());
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private List<Node> nodes;
        private SimulationMetrics metrics;

        public Builder nodes(List<Node> nodes) {
            this.nodes = nodes;
            return this;
        }

        public Builder metrics(SimulationMetrics metrics) {
            this.metrics = metrics;
            return this;
        }

        public NetworkState build() {
            NetworkState newState = new NetworkState(this);
            // Store the previous state for change detection
            if (previousState != null) {
                newState.previousState = previousState;
            }
            previousState = newState;
            return newState;
        }

        private static NetworkState previousState;
    }
}
