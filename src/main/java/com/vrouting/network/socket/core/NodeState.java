package com.vrouting.network.socket.core;

/**
 * Represents the possible states of a node in the network.
 * Each state corresponds to a specific phase in the node's lifecycle.
 */
public enum NodeState {
    /**
     * Initial state, discovering network.
     */
    DISCOVERY,

    /**
     * Normal operation.
     */
    REGULAR,

    /**
     * Node is a cluster head.
     */
    CLUSTER_HEAD,

    /**
     * Node is part of a cluster.
     */
    CLUSTER_MEMBER,

    /**
     * Node is not connected to any cluster.
     */
    ISOLATED,

    /**
     * Node is in error state.
     */
    ERROR
}
