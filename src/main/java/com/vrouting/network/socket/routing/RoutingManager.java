package com.vrouting.network.socket.routing;

import com.vrouting.network.Node;
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
    private static final long ROUTING_UPDATE_INTERVAL = 30000; // 30 seconds
    
    public RoutingManager(Node node) {
        this.node = node;
        this.routingTable = new ConcurrentHashMap<>();
        this.scheduler = Executors.newSingleThreadScheduledExecutor();
        startPeriodicUpdates();
    }
    
    private void startPeriodicUpdates() {
        scheduler.scheduleAtFixedRate(
            this::broadcastRoutingUpdate,
            ROUTING_UPDATE_INTERVAL,
            ROUTING_UPDATE_INTERVAL,
            TimeUnit.MILLISECONDS
        );
    }
    
    public void updateRoute(String destination, RoutingEntry entry) {
        if (entry != null && entry.isValid()) {
            routingTable.put(destination, entry);
            logger.debug("Updated route to {} via {}", destination, entry.getNextHop());
        }
    }
    
    public RoutingEntry getRoute(String destination) {
        return routingTable.get(destination);
    }
    
    public void removeRoute(String destination) {
        routingTable.remove(destination);
        logger.debug("Removed route to {}", destination);
    }
    
    public Set<String> getKnownDestinations() {
        return new HashSet<>(routingTable.keySet());
    }
    
    public void handleRoutingMessage(Message message) {
        if (message.getType() == MessageType.ROUTING_UPDATE) {
            processRoutingUpdate(message);
        }
    }
    
    private void processRoutingUpdate(Message message) {
        try {
            String sourceId = message.getSourceId();
            RoutingEntry entry = message.getRoutingEntry();
            
            if (entry != null && !sourceId.equals(node.getId())) {
                // Update the routing table with the new information
                updateRoute(entry.getDestination(), entry);
                
                // Propagate the update to neighbors if needed
                if (shouldPropagateUpdate(entry)) {
                    propagateRoutingUpdate(entry);
                }
            }
        } catch (Exception e) {
            logger.error("Error processing routing update: {}", e.getMessage());
        }
    }
    
    private boolean shouldPropagateUpdate(RoutingEntry entry) {
        // Implement routing update propagation logic
        // For example, only propagate if the route is new or better
        RoutingEntry existingEntry = routingTable.get(entry.getDestination());
        return existingEntry == null || entry.getMetric() < existingEntry.getMetric();
    }
    
    private void propagateRoutingUpdate(RoutingEntry entry) {
        Message updateMessage = new Message();
        updateMessage.setType(MessageType.ROUTING_UPDATE);
        updateMessage.setSourceId(node.getId());
        updateMessage.setRoutingEntry(entry);
        
        // Broadcast the update to neighbors
        node.broadcast(updateMessage);
    }
    
    private void broadcastRoutingUpdate() {
        if (!node.isActive()) {
            return;
        }
        
        // Create a routing update message with current routes
        Message updateMessage = new Message();
        updateMessage.setType(MessageType.ROUTING_UPDATE);
        updateMessage.setSourceId(node.getId());
        
        // Add all current routes to the message
        for (RoutingEntry entry : routingTable.values()) {
            updateMessage.setRoutingEntry(entry);
            node.broadcast(updateMessage);
        }
    }
    
    public void shutdown() {
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
    
    private static class RoutingEntry {
        private final String destination;
        private final String nextHop;
        private final int metric;
        
        public RoutingEntry(String destination, String nextHop, int metric) {
            this.destination = destination;
            this.nextHop = nextHop;
            this.metric = metric;
        }
        
        public String getDestination() {
            return destination;
        }
        
        public String getNextHop() {
            return nextHop;
        }
        
        public int getMetric() {
            return metric;
        }
        
        public boolean isValid() {
            return destination != null && nextHop != null && metric >= 0;
        }
    }
}
