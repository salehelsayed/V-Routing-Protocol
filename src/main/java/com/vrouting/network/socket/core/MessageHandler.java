package com.vrouting.network.socket.core;

import com.vrouting.network.socket.message.Message;

/**
 * Interface for handling different types of messages in the V-Routing Protocol.
 */
public interface MessageHandler {
    /**
     * Starts the message handler.
     */
    void start();
    
    /**
     * Stops the message handler.
     */
    void stop();
    
    /**
     * Processes an incoming message.
     */
    void processMessage(Message message);
    
    /**
     * Handles heartbeat messages for network discovery and maintenance.
     */
    void handleHeartbeat(Message heartbeat);
    
    /**
     * Handles routing-related messages.
     */
    void handleRouting(Message routingMessage);
    
    /**
     * Handles cluster management messages.
     */
    void handleClusterManagement(Message clusterMessage);
    
    /**
     * Handles secure tunnel establishment and management messages.
     */
    void handleSecureTunnel(Message tunnelMessage);
    
    /**
     * Handles data transfer messages.
     */
    void handleDataTransfer(Message dataMessage);
    
    /**
     * Sends a message to the network.
     */
    void sendMessage(Message message);
}
