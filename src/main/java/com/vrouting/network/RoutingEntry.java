package com.vrouting.network;

public class RoutingEntry {
    private final String destination;
    private final String nextHop;
    private final int hopCount;
    private final long timestamp;

    public RoutingEntry(String destination, String nextHop, int hopCount) {
        this.destination = destination;
        this.nextHop = nextHop;
        this.hopCount = hopCount;
        this.timestamp = System.currentTimeMillis();
    }

    public String getDestination() {
        return destination;
    }

    public String getNextHop() {
        return nextHop;
    }

    public int getHopCount() {
        return hopCount;
    }

    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RoutingEntry that = (RoutingEntry) o;
        return destination.equals(that.destination);
    }

    @Override
    public int hashCode() {
        return destination.hashCode();
    }
}
