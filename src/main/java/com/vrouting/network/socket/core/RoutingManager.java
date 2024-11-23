package com.vrouting.network.socket.core;

import com.vrouting.network.socket.message.Message;
import com.vrouting.network.socket.message.MessageType;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RoutingManager {
    private static final Logger logger = LoggerFactory.getLogger(RoutingManager.class);
    private final Node node;
    private final Map<String, RoutingTable> routingTables;
    private final ScheduledExecutorService scheduler;
    private final Map<String, Double> routeMetrics;
    
    // Constants for route management
    private static final long ROUTE_UPDATE_INTERVAL = 300000; // 5 minutes
    private static final int MAX_ROUTE_AGE = 600000; // 10 minutes
    private static final double LATENCY_WEIGHT = 0.3;
    private static final double BANDWIDTH_WEIGHT = 0.2;
    private static final double HOP_COUNT_WEIGHT = 0.2;
    private static final double STABILITY_WEIGHT = 0.3;
    
    public RoutingManager(Node node) {
        this.node = node;
        this.routingTables = new ConcurrentHashMap<>();
        this.routeMetrics = new ConcurrentHashMap<>();
        this.scheduler = Executors.newSingleThreadScheduledExecutor();
    }
    
    public void start() {
        // Schedule periodic route table updates
        scheduler.scheduleAtFixedRate(
            this::updateRoutingTables,
            ROUTE_UPDATE_INTERVAL,
            ROUTE_UPDATE_INTERVAL,
            TimeUnit.MILLISECONDS
        );
        logger.info("RoutingManager started for node {}", node.getNodeId());
    }
    
    public void stop() {
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(1, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
        logger.info("RoutingManager stopped for node {}", node.getNodeId());
    }
    
    public Message handleRouteRequest(Message request) {
        String destinationId = request.getDestinationNodeId();
        String sourceId = request.getSourceNodeId();
        
        // Check if we have a route to the destination
        Route route = findBestRoute(destinationId);
        if (route != null) {
            // Send route reply
            Message reply = new Message(
                node.getNodeId(),
                sourceId,
                MessageType.ROUTE_REPLY
            );
            reply.setPayload("route", route);
            return reply;
        }
        return null;
    }
    
    public Message handleRouteReply(Message reply) {
        try {
            Route route = reply.getPayload("route", Route.class);
            if (route != null) {
                updateRoute(route);
            }
        } catch (Exception e) {
            logger.error("Error handling route reply: {}", e.getMessage());
        }
        return null;
    }
    
    public String getNextHop(String destinationId) {
        Route route = findBestRoute(destinationId);
        return route != null ? route.getNextHop() : null;
    }
    
    private void updateRoutingTables() {
        long currentTime = System.currentTimeMillis();
        routingTables.values().forEach(table -> 
            table.removeExpiredRoutes(currentTime - MAX_ROUTE_AGE)
        );
    }
    
    private Route findBestRoute(String destinationId) {
        RoutingTable table = routingTables.get(destinationId);
        return table != null ? table.getBestRoute() : null;
    }
    
    private void updateRoute(Route route) {
        String destinationId = route.getDestinationId();
        RoutingTable table = routingTables.computeIfAbsent(
            destinationId,
            k -> new RoutingTable()
        );
        table.updateRoute(route);
        updateRouteMetric(route);
    }
    
    private void updateRouteMetric(Route route) {
        double metric = calculateRouteMetric(route);
        routeMetrics.put(route.getDestinationId(), metric);
    }
    
    private double calculateRouteMetric(Route route) {
        double latencyScore = 1.0 / (1.0 + route.getLatency());
        double bandwidthScore = route.getBandwidth();
        double hopCountScore = 1.0 / route.getHopCount();
        double stabilityScore = route.getStability();
        
        return (LATENCY_WEIGHT * latencyScore +
                BANDWIDTH_WEIGHT * bandwidthScore +
                HOP_COUNT_WEIGHT * hopCountScore +
                STABILITY_WEIGHT * stabilityScore);
    }
    
    // Inner class for Route
    public static class Route {
        private final String id;
        private final String destinationId;
        private final List<String> hops;
        private long lastUpdated;
        private double latency;
        private double bandwidth;
        private int hopCount;
        private double stability;
        
        public Route(String destinationId, List<String> hops) {
            this.id = UUID.randomUUID().toString();
            this.destinationId = destinationId;
            this.hops = new ArrayList<>(hops);
            this.lastUpdated = System.currentTimeMillis();
        }
        
        public String getId() { return id; }
        public String getDestinationId() { return destinationId; }
        public List<String> getHops() { return Collections.unmodifiableList(hops); }
        public long getLastUpdated() { return lastUpdated; }
        public double getLatency() { return latency; }
        public double getBandwidth() { return bandwidth; }
        public int getHopCount() { return hopCount; }
        public double getStability() { return stability; }
        
        public void updateTimestamp() {
            this.lastUpdated = System.currentTimeMillis();
        }
        
        public String getNextHop() {
            return hops.isEmpty() ? null : hops.get(0);
        }
        
        public void setLatency(double latency) {
            this.latency = latency;
        }
        
        public void setBandwidth(double bandwidth) {
            this.bandwidth = bandwidth;
        }
        
        public void setHopCount(int hopCount) {
            this.hopCount = hopCount;
        }
        
        public void setStability(double stability) {
            this.stability = stability;
        }
    }
    
    // Inner class for RoutingTable
    private static class RoutingTable {
        private final Set<Route> routes;
        
        public RoutingTable() {
            this.routes = new HashSet<>();
        }
        
        public void addRoute(Route route) {
            routes.removeIf(r -> r.getId().equals(route.getId()));
            routes.add(route);
        }
        
        public void updateRoute(Route route) {
            routes.removeIf(r -> r.getId().equals(route.getId()));
            routes.add(route);
        }
        
        public void removeExpiredRoutes(long maxAge) {
            routes.removeIf(route -> (System.currentTimeMillis() - route.getLastUpdated()) > maxAge);
        }
        
        public Route getBestRoute() {
            return routes.stream()
                .max(Comparator.comparingDouble(route -> calculateRouteMetric(route)))
                .orElse(null);
        }
        
        private double calculateRouteMetric(Route route) {
            double latencyScore = 1.0 / (1.0 + route.getLatency());
            double bandwidthScore = route.getBandwidth();
            double hopCountScore = 1.0 / route.getHopCount();
            double stabilityScore = route.getStability();
            
            return (LATENCY_WEIGHT * latencyScore +
                    BANDWIDTH_WEIGHT * bandwidthScore +
                    HOP_COUNT_WEIGHT * hopCountScore +
                    STABILITY_WEIGHT * stabilityScore);
        }
    }
    
    public void routeMessage(Message message) {
        logger.info("Routing message: {}", message);
        // ... existing code ...
        logger.debug("Message routed successfully");
    }
}
