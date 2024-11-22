package com.vrouting.web.dto;

public class SimulationRequest {
    private int nodeCount;
    private double failureProbability;
    private double recoveryProbability;

    // Getters and setters
    public int getNodeCount() {
        return nodeCount;
    }

    public void setNodeCount(int nodeCount) {
        this.nodeCount = nodeCount;
    }

    public double getFailureProbability() {
        return failureProbability;
    }

    public void setFailureProbability(double failureProbability) {
        this.failureProbability = failureProbability;
    }

    public double getRecoveryProbability() {
        return recoveryProbability;
    }

    public void setRecoveryProbability(double recoveryProbability) {
        this.recoveryProbability = recoveryProbability;
    }
}
