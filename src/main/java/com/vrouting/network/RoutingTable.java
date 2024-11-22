package com.vrouting.network;

import java.util.HashMap;
import java.util.Map;

public class RoutingTable {
    private final Map<String, RoutingEntry> routes;

    public RoutingTable() {
        this.routes = new HashMap<>();
    }

    public void updateEntry(RoutingEntry entry) {
        routes.put(entry.getDestination(), entry);
    }

    public RoutingEntry getEntry(String destination) {
        return routes.get(destination);
    }

    public boolean hasRouteTo(String destination) {
        return routes.containsKey(destination);
    }

    public Map<String, RoutingEntry> getRoutes() {
        return new HashMap<>(routes);
    }

    public void clear() {
        routes.clear();
    }
}
