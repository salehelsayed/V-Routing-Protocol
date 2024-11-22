package com.vrouting.network.socket.core;

/**
 * Represents the main phases a node can be in during its lifecycle.
 * Each phase has different characteristics in terms of heartbeat frequency,
 * message propagation depth, and network participation.
 */
public enum Phase {
    /**
     * Initial state of the node.
     */
    INITIAL,
    
    /**
     * Initial discovery phase where node is discovering its neighbors.
     */
    DISCOVERY,
    
    /**
     * Network stabilization phase where centrality is calculated
     * and cluster heads are elected.
     */
    STABILIZATION,
    
    /**
     * Maintenance phase where the node performs periodic checks and adjustments.
     */
    MAINTENANCE,

    REGULAR
}
