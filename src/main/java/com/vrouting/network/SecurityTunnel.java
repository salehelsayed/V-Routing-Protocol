package com.vrouting.network;

public class SecurityTunnel {
    private final Cluster source;
    private final Cluster destination;
    private boolean established;
    private final long creationTime;

    public SecurityTunnel(Cluster source, Cluster destination) {
        this.source = source;
        this.destination = destination;
        this.established = false;
        this.creationTime = System.currentTimeMillis();
        establishTunnel();
    }

    private void establishTunnel() {
        // In a real implementation, this would:
        // 1. Exchange SSH keys between cluster heads
        // 2. Set up secure channel
        // 3. Verify connection
        this.established = true;
    }

    public boolean isEstablished() {
        return established;
    }

    public Cluster getSource() {
        return source;
    }

    public Cluster getDestination() {
        return destination;
    }

    public long getCreationTime() {
        return creationTime;
    }
}
