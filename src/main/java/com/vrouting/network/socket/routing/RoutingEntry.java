package com.vrouting.network.socket.routing;

public class RoutingEntry {
    private String destinationId;
    private String nextHopId;
    private int hopCount;

    public RoutingEntry(String destinationId, String nextHopId, int hopCount) {
        this.destinationId = destinationId;
        this.nextHopId = nextHopId;
        this.hopCount = hopCount;
    }

    public String getDestinationId() {
        return destinationId;
    }

    public void setDestinationId(String destinationId) {
        this.destinationId = destinationId;
    }

    public String getNextHopId() {
        return nextHopId;
    }

    public void setNextHopId(String nextHopId) {
        this.nextHopId = nextHopId;
    }

    public int getHopCount() {
        return hopCount;
    }

    public void setHopCount(int hopCount) {
        this.hopCount = hopCount;
    }
}
