# Heartbeat Mechanism

## Overview
The V-Routing Protocol implements a three-phase heartbeat mechanism with negative exponential decay for intervals. This approach ensures efficient network discovery while minimizing network overhead in stable states.

## Phases

### Phase 1: Discovery
- **Interval**: 1 second
- **Purpose**: Rapid network discovery
- **Scope**: Immediate neighbors only
- **Message Types**:
  - `HEARTBEAT_DISCOVERY`: Initial discovery broadcast
  - `HEARTBEAT_RESPONSE`: Neighbor response with basic info

### Phase 2: Network Formation
- **Interval**: 4 seconds
- **Purpose**: Extended network mapping
- **Scope**: Multi-hop discovery
- **Message Types**:
  - `HEARTBEAT`: Regular network presence
  - `HEARTBEAT_UPDATE`: Network topology updates
  - `HEARTBEAT_RESPONSE`: Extended network info

### Phase 3: Stable Operation
- **Interval**: 18 seconds
- **Purpose**: Network maintenance
- **Scope**: Full network coverage
- **Message Types**:
  - `HEARTBEAT_STABLE`: Regular presence broadcast
  - `HEARTBEAT_ACK`: Acknowledgment and health check

## Transition Logic

### Discovery to Formation
- Triggered by:
  - Sufficient neighbor discovery
  - Initial network map formation
  - Stable connection count

### Formation to Stable
- Triggered by:
  - Complete network topology
  - Route table establishment
  - Cluster head identification

## Message Processing

### Heartbeat Reception
1. Update sender's last seen timestamp
2. Extract network topology information
3. Update routing tables
4. Process any piggy-backed data

### Response Generation
1. Include current network view
2. Add routing information
3. Include cluster status
4. Append health metrics

## Network Health

### Monitoring
- Track message success rates
- Monitor response times
- Evaluate connection quality
- Detect network changes

### Adaptation
- Adjust intervals based on network stability
- Modify scope based on network size
- Update depth based on topology

## Implementation Details

### Interval Calculation
```java
long calculateInterval(Phase phase, long timeInPhase) {
    switch(phase) {
        case DISCOVERY:
            return 1000; // 1 second
        case FORMATION:
            return 4000; // 4 seconds
        case STABLE:
            return 18000; // 18 seconds
        default:
            return 1000;
    }
}
```

### Message Structure
```java
class HeartbeatMessage {
    String nodeId;
    Phase phase;
    long timestamp;
    Map<String, NodeInfo> knownNodes;
    Map<String, RouteInfo> routes;
    NetworkMetrics metrics;
}
```

## Configuration

### Tunable Parameters
- Initial interval
- Maximum interval
- Decay rate
- Phase transition thresholds

### Network Adaptations
- Dynamic interval adjustment
- Scope modifications
- Depth control
- Response throttling
