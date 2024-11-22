package com.vrouting.network;

public enum MessageType {
    HEARTBEAT,          // Regular heartbeat messages
    ROUTING,            // Routing table updates
    CLUSTER_JOIN,       // Request to join a cluster
    CLUSTER_LEAVE,      // Notification of leaving a cluster
    CLUSTER_HEAD,       // Cluster head election messages
    TUNNEL_REQUEST,     // Request to establish secure tunnel
    TUNNEL_RESPONSE,    // Response to tunnel request
    DATA               // Regular data messages
}
