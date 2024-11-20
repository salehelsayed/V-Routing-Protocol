package com.vrouting.network.socket;

/**
 * Tracks and manages connection health metrics for a socket connection.
 */
public class ConnectionMetrics {
    private long latency; // in milliseconds
    private double jitter; // in milliseconds
    private double packetLossRate; // percentage (0-100)
    private long lastUpdateTime;

    public ConnectionMetrics() {
        this.latency = 0;
        this.jitter = 0.0;
        this.packetLossRate = 0.0;
        this.lastUpdateTime = System.currentTimeMillis();
    }

    public void updateLatency(long newLatency) {
        this.latency = newLatency;
        this.lastUpdateTime = System.currentTimeMillis();
    }

    public void updateJitter(double newJitter) {
        this.jitter = newJitter;
        this.lastUpdateTime = System.currentTimeMillis();
    }

    public void updatePacketLossRate(double newPacketLossRate) {
        this.packetLossRate = Math.max(0, Math.min(100, newPacketLossRate));
        this.lastUpdateTime = System.currentTimeMillis();
    }

    public long getLatency() {
        return latency;
    }

    public double getJitter() {
        return jitter;
    }

    public double getPacketLossRate() {
        return packetLossRate;
    }

    public long getLastUpdateTime() {
        return lastUpdateTime;
    }

    public boolean isHealthy() {
        return latency < 1000 && jitter < 100 && packetLossRate < 5.0;
    }

    @Override
    public String toString() {
        return String.format(
            "ConnectionMetrics{latency=%dms, jitter=%.2fms, packetLoss=%.2f%%, lastUpdate=%d}",
            latency, jitter, packetLossRate, lastUpdateTime
        );
    }
}
