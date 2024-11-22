package com.vrouting.network.socket.message;

/**
 * Defines all possible message types in the V-Routing Protocol.
 * Messages are categorized by their function in the network.
 */
public enum MessageType {
    // Discovery and heartbeat messages
    HEARTBEAT,
    HEARTBEAT_DISCOVERY,
    HEARTBEAT_STABLE,
    HEARTBEAT_UPDATE,
    HEARTBEAT_RESPONSE,  // Added HEARTBEAT_RESPONSE
    HEARTBEAT_ACK,  // Added HEARTBEAT_ACK
    
    // Cluster management messages
    JOIN,
    LEAVE,
    UPDATE,
    ACK,
    
    // Routing messages
    ROUTE_REQUEST,
    ROUTE_REPLY,
    ROUTE_REPLY_ACK,  // Added ROUTE_REPLY_ACK
    ROUTE_RESPONSE,
    
    // Data messages
    DATA,
    DATA_ACK  // Added DATA_ACK
}
