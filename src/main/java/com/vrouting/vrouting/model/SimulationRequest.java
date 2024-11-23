package com.vrouting.vrouting.model;

public class SimulationRequest {
    private int nodeCount;
    private int duration;
    private boolean enableClustering;

    // Getters and Setters
    public int getNodeCount() {
        return nodeCount;
    }

    public void setNodeCount(int nodeCount) {
        this.nodeCount = nodeCount;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public boolean isEnableClustering() {
        return enableClustering;
    }

    public void setEnableClustering(boolean enableClustering) {
        this.enableClustering = enableClustering;
    }

    @Override
    public String toString() {
        return "SimulationRequest{" +
                "nodeCount=" + nodeCount +
                ", duration=" + duration +
                ", enableClustering=" + enableClustering +
                '}';
    }
}
