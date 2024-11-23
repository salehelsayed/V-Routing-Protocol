package com.vrouting.simulation;

import java.io.Serializable;

public class SimulationMetrics implements Serializable {
    private static final long serialVersionUID = 1L;

    private int timestep;
    private int activeNodeCount;
    private int failedNodeCount;
    private double messageDeliveryRate;
    private double averageLatency;
    private int totalMessagesSent;
    private int totalMessagesDelivered;

    // Constructor
    public SimulationMetrics(
            int timestep,
            int activeNodeCount,
            int failedNodeCount,
            double messageDeliveryRate,
            double averageLatency,
            int totalMessagesSent,
            int totalMessagesDelivered
    ) {
        this.timestep = timestep;
        this.activeNodeCount = activeNodeCount;
        this.failedNodeCount = failedNodeCount;
        this.messageDeliveryRate = messageDeliveryRate;
        this.averageLatency = averageLatency;
        this.totalMessagesSent = totalMessagesSent;
        this.totalMessagesDelivered = totalMessagesDelivered;
    }

    // Getter methods
    public int getTimestep() {
        return timestep;
    }

    public int getActiveNodeCount() {
        return activeNodeCount;
    }

    public int getFailedNodeCount() {
        return failedNodeCount;
    }

    public double getMessageDeliveryRate() {
        return messageDeliveryRate;
    }

    public double getAverageLatency() {
        return averageLatency;
    }

    public int getTotalMessagesSent() {
        return totalMessagesSent;
    }

    public int getTotalMessagesDelivered() {
        return totalMessagesDelivered;
    }
}
