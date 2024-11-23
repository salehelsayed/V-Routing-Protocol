package com.vrouting.network.socket.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents the main phases a node can be in during its lifecycle.
 * Each phase has different characteristics in terms of heartbeat frequency,
 * message propagation depth, and network participation.
 */
public enum Phase {
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
     * Normal operation phase where routing and message forwarding occur.
     */
    REGULAR,
    
    /**
     * Stable operation phase with optimized routing and cluster structure.
     */
    STABLE
}
