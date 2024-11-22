package com.vrouting.network.socket.core;

import com.vrouting.network.socket.message.Message;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * Handles message routing and dispatching to appropriate handlers.
 */
public class MessageDispatcher {
    private static final Logger logger = Logger.getLogger(MessageDispatcher.class.getName());
    
    private final Node node;
    private final Set<String> processedMessages;
    
    public MessageDispatcher(Node node) {
        this.node = node;
        this.processedMessages = ConcurrentHashMap.newKeySet();
    }
    
    public void start() {
        // Nothing to start - this is event-driven
    }
    
    public void stop() {
        processedMessages.clear();
    }
    
    public void dispatch(Message message) {
        if (!isValidMessage(message)) {
            return;
        }
        
        String destinationId = message.getDestinationNodeId();
        
        if (node.getNodeId().equals(destinationId)) {
            // Message is for us
            node.processMessage(message);
        } else if (destinationId == null) {
            // Broadcast message
            broadcast(message);
        } else {
            // Forward to next hop
            forwardMessage(message);
        }
    }
    
    public void broadcast(Message message) {
        if (!isValidMessage(message)) {
            return;
        }
        
        // Add ourselves to the routing history
        message.addToRoute(node.getNodeId());
        
        // Send to all peers except those in routing history
        node.getPeerDirectory().getAllPeers().stream()
            .filter(peerId -> !message.hasVisited(peerId))
            .forEach(peerId -> {
                Message copy = message.copy();
                copy.setDestinationNodeId(peerId);
                node.sendMessage(copy);
            });
    }
    
    public Message handleData(Message message) {
        // Process the data message
        if (!isValidMessage(message)) {
            return null;
        }
        
        // Add ourselves to the routing history
        message.addToRoute(node.getNodeId());
        
        String destinationId = message.getDestinationNodeId();
        
        if (node.getNodeId().equals(destinationId)) {
            // Message is for us, process it
            return node.processMessage(message);
        } else {
            // Forward the message
            forwardMessage(message);
            return null;
        }
    }
    
    private boolean isValidMessage(Message message) {
        if (!processedMessages.add(message.getId())) {
            logger.fine("Dropping duplicate message: " + message.getId());
            return false;
        }
        
        if (message.getHopCount() >= message.getMaxHops()) {
            logger.fine("Dropping message that exceeded max hops: " + message.getId());
            return false;
        }
        
        return true;
    }
    
    private void forwardMessage(Message message) {
        // Add ourselves to the routing history
        message.addToRoute(node.getNodeId());
        
        // Get next hop from routing manager
        String nextHop = node.getRoutingManager().getNextHop(message.getDestinationNodeId());
        
        if (nextHop != null) {
            Message copy = message.copy();
            copy.setDestinationNodeId(nextHop);
            node.sendMessage(copy);
        } else {
            logger.warning("No route to destination: " + message.getDestinationNodeId());
        }
    }
    
    public void cleanup() {
        processedMessages.clear();
    }
}
