package com.vrouting.network.socket.routing;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * Maintains routing information and paths for network communication.
 */
public class RoutingTable {
    private static final Logger logger = Logger.getLogger(RoutingTable.class.getName());
    private static final long ROUTE_TIMEOUT = 300000; // 5 minutes
    
    private final Map<String, RouteEntry> routes;
    private final Map<String, Double> metricWeights;
    
    public RoutingTable() {
        this.routes = new ConcurrentHashMap<>();
        this.metricWeights = new ConcurrentHashMap<>();
        initializeMetricWeights();
    }
    
    /**
     * Represents a route entry with multiple possible paths.
     */
    private static class RouteEntry {
        private final String destination;
        private final List<RoutingPath> paths;
        private volatile RoutingPath primaryPath;
        private final long creationTime;
        private volatile long lastUpdated;
        
        public RouteEntry(String destination) {
            this.destination = destination;
            this.paths = new ArrayList<>();
            this.creationTime = System.currentTimeMillis();
            this.lastUpdated = creationTime;
        }
        
        public void addPath(RoutingPath path) {
            paths.add(path);
            updatePrimaryPath();
            lastUpdated = System.currentTimeMillis();
        }
        
        public void updatePrimaryPath() {
            primaryPath = paths.stream()
                .filter(RoutingPath::isValid)
                .max(Comparator.comparingDouble(RoutingPath::getScore))
                .orElse(null);
        }
        
        public boolean isValid() {
            return System.currentTimeMillis() - lastUpdated < ROUTE_TIMEOUT;
        }
        
        public List<RoutingPath> getPaths() {
            return Collections.unmodifiableList(paths);
        }
        
        public Optional<RoutingPath> getPrimaryPath() {
            return Optional.ofNullable(primaryPath);
        }
    }
    
    /**
     * Initializes default metric weights.
     */
    private void initializeMetricWeights() {
        metricWeights.put("latency", 0.4);
        metricWeights.put("bandwidth", 0.3);
        metricWeights.put("reliability", 0.3);
    }
    
    /**
     * Updates route information.
     */
    public void updateRoute(Map<String, Object> routeInfo) {
        String destination = (String) routeInfo.get("destination");
        if (destination == null) {
            logger.warning("Route update missing destination");
            return;
        }
        
        @SuppressWarnings("unchecked")
        List<String> path = (List<String>) routeInfo.get("path");
        @SuppressWarnings("unchecked")
        Map<String, Double> metrics = (Map<String, Double>) routeInfo.get("metrics");
        
        if (path != null && metrics != null) {
            RouteEntry entry = routes.computeIfAbsent(destination, RouteEntry::new);
            entry.addPath(new RoutingPath(path, metrics));
            logger.fine("Updated route to " + destination);
        }
    }
    
    /**
     * Gets the best path to a destination.
     */
    public Optional<RoutingPath> getBestPath(String destination) {
        RouteEntry entry = routes.get(destination);
        if (entry != null && entry.isValid()) {
            return entry.getPrimaryPath();
        }
        return Optional.empty();
    }
    
    /**
     * Gets all paths to a destination.
     */
    public List<RoutingPath> getAllPaths(String destination) {
        RouteEntry entry = routes.get(destination);
        if (entry != null && entry.isValid()) {
            return entry.getPaths();
        }
        return Collections.emptyList();
    }
    
    /**
     * Updates metric weights.
     */
    public void updateMetricWeights(Map<String, Double> weights) {
        metricWeights.putAll(weights);
        // Recalculate primary paths
        routes.values().forEach(RouteEntry::updatePrimaryPath);
    }
    
    /**
     * Gets current metric weights.
     */
    public Map<String, Double> getMetricWeights() {
        return Collections.unmodifiableMap(metricWeights);
    }
    
    /**
     * Removes invalid routes.
     */
    public void cleanup() {
        routes.entrySet().removeIf(entry -> !entry.getValue().isValid());
    }
    
    /**
     * Gets the number of active routes.
     */
    public int getRouteCount() {
        return (int) routes.values().stream()
            .filter(RouteEntry::isValid)
            .count();
    }
    
    /**
     * Clears all routes.
     */
    public void clear() {
        routes.clear();
    }
}
