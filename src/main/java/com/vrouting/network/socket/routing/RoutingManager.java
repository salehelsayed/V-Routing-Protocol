package com.vrouting.network.socket.routing;

import com.vrouting.network.socket.core.Node;
import com.vrouting.network.socket.message.Message;
import com.vrouting.network.socket.message.MessageType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.*;
import java.util.concurrent.*;

public class RoutingManager {
    private static final Logger logger = LoggerFactory.getLogger(RoutingManager.class);
    private final Node node;
    private final Map<String, RoutingEntry> routingTable;
    private final ScheduledExecutorService scheduler;
    
    public RoutingManager(Node node) {
        this.node = node;
        this.routingTable = new ConcurrentHashMap<>();
        this.scheduler = Executors.newSingleThreadScheduledExecutor();
    }
    
    public void start() {
        // Schedule periodic route table cleanup
        scheduler.scheduleAtFixedRate(this::cleanupRoutes, 30, 30, TimeUnit.SECONDS);
    }
    
    public void stop() {
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.warn("Interrupted while stopping routing manager", e);
        }
    }
    
    public String getNextHop(String destinationId) {
        RoutingEntry entry = routingTable.get(destinationId);
        if (entry != null && !entry.isExpired()) {
            return entry.getNextHop();
        }
        return null;
    }
    
    public void updateRoute(Message message) {
        if (message == null) {
            return;
        }
        
        String sourceId = message.getSourceNodeId();
        List<String> routeHistory = message.getRouteHistory();
        
        if (sourceId != null && !routeHistory.isEmpty()) {
            // Update or create routing entry
            String nextHop = routeHistory.get(routeHistory.size() - 1);
            RoutingEntry entry = new RoutingEntry(nextHop, routeHistory.size(), System.currentTimeMillis());
            routingTable.put(sourceId, entry);
            
            logger.debug("Updated route to {} via {}", sourceId, nextHop);
        }
    }
    
    private void cleanupRoutes() {
        long now = System.currentTimeMillis();
        routingTable.entrySet().removeIf(entry -> entry.getValue().isExpired(now));
    }
    
    private static class RoutingEntry {
        private final String nextHop;
        private final int hopCount;
        private final long timestamp;
        private static final long ROUTE_TIMEOUT = TimeUnit.MINUTES.toMillis(5);
        
        public RoutingEntry(String nextHop, int hopCount, long timestamp) {
            this.nextHop = nextHop;
            this.hopCount = hopCount;
            this.timestamp = timestamp;
        }
        
        public String getNextHop() {
            return nextHop;
        }
        
        public boolean isExpired() {
            return isExpired(System.currentTimeMillis());
        }
        
        public boolean isExpired(long now) {
            return now - timestamp > ROUTE_TIMEOUT;
        }
    }
}
