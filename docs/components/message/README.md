# Message Types and Node Behavior Documentation

## Message Types (MessageType.java)

```java
public enum MessageType {
    // Discovery and Network Health
    HEARTBEAT,                // Initial network discovery and health check
    HEARTBEAT_RESPONSE,       // Response to heartbeat with node info and routes
    CLUSTER_HEAD_ANNOUNCE,    // Node announcing its cluster head status
    
    // Routing
    ROUTE_REQUEST,           // Request for route to destination
    ROUTE_RESPONSE,          // Response containing route information
    ROUTE_ERROR,            // Notification of route failure
    
    // Data Transfer
    UNICAST,                // Direct message to specific node
    BROADCAST,              // Network-wide broadcast message
    MULTICAST,              // Message to multiple specific nodes
    
    // Security
    TUNNEL_REQUEST,         // Request to establish secure tunnel
    TUNNEL_RESPONSE,        // Response to tunnel establishment request
    TUNNEL_UPDATE,          // Update existing tunnel parameters
    TUNNEL_CLOSE,           // Close existing tunnel
    
    // Network Management
    TOPOLOGY_UPDATE,        // Updates about network topology changes
    METRIC_UPDATE,          // Updates about network metrics
    ERROR                   // Error conditions and exceptions
}
```

## Node States and Phases

### Phase 1: Network Discovery (Newbie Node)
- **Duration**: First 30% of exponential curve
- **Heartbeat Interval**: Starts at 1 second, increases exponentially
- **Behavior**:
  - Sends frequent HEARTBEAT messages
  - Limited to single-hop communication
  - Builds initial network vision
  - Collects responses for adjacency matrix

### Phase 2: Regular Node
- **Duration**: Middle phase until potential cluster head promotion
- **Heartbeat Interval**: Follows negative exponential growth
- **Behavior**:
  - Participates in message routing
  - Maintains routing table
  - Updates adjacency matrix
  - Calculates eigenvector centrality

### Phase 3: Cluster Head Node
- **Duration**: After promotion based on centrality and configuration
- **Heartbeat Interval**: Stabilized at maximum interval
- **Behavior**:
  - Manages secure tunnels
  - Coordinates cluster communication
  - Maintains inter-cluster routes
  - Handles security handshakes

## Message Handling and Network Health

### 1. HEARTBEAT Message Flow
```java
void handleHeartbeat(Message message) {
    // 1. Update sender's info in peer directory
    peerDirectory.updatePeer(message.getSourceNodeId(), message);
    
    // 2. Check message depth vs current phase
    if (getCurrentPhase() == Phase.NEWBIE) {
        // Limit propagation to single hop
        return;
    }
    
    // 3. Check if message should be propagated
    if (message.getHopCount() < getMaxAllowedHops()) {
        // Increment hop count and propagate
        message.incrementHopCount();
        broadcast(message);
    }
    
    // 4. Send HEARTBEAT_RESPONSE
    sendHeartbeatResponse(message.getSourceNodeId());
}
```

### 2. Route Management
```java
void handleRouteRequest(Message message) {
    String destination = message.getDestinationNodeId();
    
    // 1. Check routing table for existing route
    Route route = routingTable.getRoute(destination);
    if (route != null && route.isValid()) {
        sendRouteResponse(message.getSourceNodeId(), route);
        return;
    }
    
    // 2. Check if we're at max hop count
    if (message.getHopCount() >= getMaxAllowedHops()) {
        sendRouteError(message.getSourceNodeId());
        return;
    }
    
    // 3. Propagate route request
    message.incrementHopCount();
    broadcast(message);
}
```

### 3. Secure Tunnel Management
```java
void handleTunnelRequest(Message message) {
    // 1. Verify if we're a cluster head
    if (!isClusterHead()) {
        forwardToClusterHead(message);
        return;
    }
    
    // 2. Check for existing tunnel
    SecureTunnel tunnel = findExistingTunnel(message.getSourceNodeId());
    if (tunnel != null) {
        sendTunnelResponse(message.getSourceNodeId(), tunnel);
        return;
    }
    
    // 3. Create new tunnel
    tunnel = createSecureTunnel(message);
    sendTunnelResponse(message.getSourceNodeId(), tunnel);
}
```

## Route Selection Criteria

### KPI Weights Configuration
```java
public class RouteSelectionConfig {
    private double latencyWeight = 0.3;
    private double bandwidthWeight = 0.25;
    private double reliabilityWeight = 0.25;
    private double hopCountWeight = 0.2;
    
    // Custom KPIs can be added here
    private Map<String, Double> customKpiWeights = new HashMap<>();
}
```

### Route Score Calculation
```java
double calculateRouteScore(Route route) {
    double score = 0.0;
    
    // Standard KPIs
    score += config.getLatencyWeight() * normalizeLatency(route.getLatency());
    score += config.getBandwidthWeight() * normalizeBandwidth(route.getBandwidth());
    score += config.getReliabilityWeight() * route.getReliability();
    score += config.getHopCountWeight() * normalizeHopCount(route.getHopCount());
    
    // Custom KPIs
    for (Map.Entry<String, Double> kpi : route.getCustomKpis().entrySet()) {
        Double weight = config.getCustomKpiWeights().get(kpi.getKey());
        if (weight != null) {
            score += weight * kpi.getValue();
        }
    }
    
    return score;
}
```

## Message Serialization

### Interface
```java
public interface MessageSerializer {
    byte[] serialize(Message message);
    Message deserialize(byte[] data);
}
```

### JSON Implementation (Development)
```java
public class JsonMessageSerializer implements MessageSerializer {
    private final ObjectMapper mapper = new ObjectMapper();
    
    @Override
    public byte[] serialize(Message message) {
        return mapper.writeValueAsBytes(message);
    }
    
    @Override
    public Message deserialize(byte[] data) {
        return mapper.readValue(data, Message.class);
    }
}
```

### Java Serialization (Production)
```java
public class JavaMessageSerializer implements MessageSerializer {
    @Override
    public byte[] serialize(Message message) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(message);
            return baos.toByteArray();
        }
    }
    
    @Override
    public Message deserialize(byte[] data) {
        try (ByteArrayInputStream bais = new ByteArrayInputStream(data);
             ObjectInputStream ois = new ObjectInputStream(bais)) {
            return (Message) ois.readObject();
        }
    }
}
```
