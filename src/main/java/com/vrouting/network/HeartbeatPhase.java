package com.vrouting.network;

public enum HeartbeatPhase {
    DISCOVERY,      // Initial phase with 1s intervals
    STABILIZATION,  // Second phase with 4s intervals
    MAINTENANCE     // Final phase with 18s intervals
}
