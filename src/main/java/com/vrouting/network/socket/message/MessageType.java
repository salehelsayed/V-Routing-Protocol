package com.vrouting.network.socket.message;

/**
 * Defines all possible message types in the V-Routing Protocol.
 * Messages are categorized by their function in the network.
 */
public enum MessageType {
    HEARTBEAT,
    HEARTBEAT_RESPONSE,
    ROUTING_UPDATE,
    CLUSTER_UPDATE,
    DATA,
    ACK,
    UPDATE,
    HEARTBEAT_DISCOVERY,
    HEARTBEAT_STABLE,
    HEARTBEAT_UPDATE,
    ROUTE_REPLY,
    ROUTE_REPLY_ACK,
    DATA_ACK
}
