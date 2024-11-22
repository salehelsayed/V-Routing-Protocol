# V-Routing Protocol Implementation Plan

## 1. Core Message Types and Node States

### 1.1 Message Types (MessageType.java)
```java
public enum MessageType {
    // Node Discovery & Network Formation
    HEARTBEAT_DISCOVERY,          // Initial high-frequency heartbeats (Phase 1)
    HEARTBEAT_REGULAR,           // Regular heartbeats with increased depth (Phase 2)
    HEARTBEAT_RESPONSE,          // Response containing network topology info
    HEARTBEAT_CLUSTER_HEAD,      // Heartbeat from cluster head with routing info
    
    // Routing & Path Management
    ROUTE_DISCOVERY,             // Find path to destination
    ROUTE_RESPONSE,             // Response with available paths
    ROUTE_UPDATE,               // Update existing route metrics
    ROUTE_ERROR,               // Notify route failure/metric degradation
    
    // Cluster Management
    CLUSTER_HEAD_ANNOUNCE,      // Node declaring cluster head status
    CLUSTER_HEAD_UPDATE,        // Updates about cluster head state/metrics
    CLUSTER_JOIN_REQUEST,       // Request to join a cluster
    CLUSTER_JOIN_RESPONSE,      // Accept/reject cluster join
    
    // Security & Tunneling
    SECURE_TUNNEL_REQUEST,      // Request secure tunnel establishment
    SECURE_TUNNEL_RESPONSE,     // Tunnel parameters and key exchange
    SECURE_TUNNEL_ESTABLISHED,  // Confirm tunnel establishment
    SECURE_TUNNEL_ERROR,       // Report tunnel establishment failure
    
    // Data Transfer
    DATA_TRANSFER,             // Regular data message
    DATA_ACKNOWLEDGMENT,      // Confirm data receipt
    DATA_FORWARD             // Forward data through multi-hop path
}
```

### 1.2 Node States and Phases
```java
public enum NodeState {
    DISCOVERY,           // Initial state, discovering network
    REGULAR,            // Normal operation
    CLUSTER_HEAD,       // Operating as cluster head
    CLUSTER_MEMBER,     // Part of a cluster
    ISOLATED,          // Not connected to any cluster
    ERROR             // Error state
}

public enum Phase {
    DISCOVERY,         // Initial discovery phase
    STABILIZATION,     // Network stabilization and cluster head election
    REGULAR,          // Normal operation phase
    STABLE           // Stable operation phase
}
```

## 2. Core Components

### 2.1 Node Base Class
```java
public abstract class Node {
    private final String nodeId;
    private final NetworkConfig config;
    private final AtomicReference<NodeState> state;
    private final AtomicReference<Phase> phase;
    private final PeerDirectory peerDirectory;
    private final MessageHandler messageHandler;
    private final HeartbeatManager heartbeatManager;
    private final RoutingManager routingManager;
    private final MessageDispatcher messageDispatcher;
    private final CentralityCalculator centralityCalculator;
    private NodeMetrics metrics;
    
    // Core functionality
    public void start();
    public void stop();
    public void broadcast(Message message);
    public void sendMessage(Message message);
    public void processMessage(Message message);
    
    // State management
    public void updateState(NodeState newState);
    public void onPhaseChange(Phase newPhase);
    public void recalculateCentrality();
    
    // Cluster head functionality
    public boolean isClusterHead();
    public boolean isEligibleForClusterHead();
}
```

### 2.2 HeartbeatManager
```java
public class HeartbeatManager {
    // Three-phase heartbeat mechanism
    private void sendDiscoveryHeartbeat();    // Phase 1: High frequency
    private void sendRegularHeartbeat();      // Phase 2: Normal interval
    private void sendClusterHeartbeat();      // Phase 3: Cluster-aware
    
    // Heartbeat response handling
    private void handleHeartbeatResponse(Message response);
    private void updatePeerState(String peerId, Message heartbeat);
}
```

### 2.3 RoutingManager
```java
public class RoutingManager {
    // Route management
    public void updateRoutes();
    public boolean hasPathTo(String destination);
    public void forward(Message message);
    
    // Route metrics
    private double calculateRouteScore(Route route);
    private void cleanupExpiredRoutes();
}
```

### 2.4 CentralityCalculator
```java
public class CentralityCalculator {
    // Centrality calculation
    public double calculateEigenvectorCentrality();
    private double calculateCentralityUsingPowerIteration(double[][] matrix);
    
    // Cluster head eligibility
    public boolean isEligibleForClusterHead();
    private boolean meetsResourceRequirements();
}
```

### 2.5 PeerDirectory
```java
public class PeerDirectory {
    // Peer management
    public void updatePeer(String peerId, Message message);
    public Set<String> getAllPeers();
    public Map<String, Set<String>> getAdjacencyMatrix();
    
    // Route tracking
    public void updateRouteHistory(String peerId, RouteMetrics metrics);
    private void cleanupStaleEntries();
}
```

### 2.6 NodeMetrics
```java
public class NodeMetrics {
    private double batteryLevel;
    private double processingCapacity;
    private double networkStrength;
    private double reliability;
    
    // Metric management
    public double getOverallScore();
    public void updateMetrics();
}
```

## 3. Implementation Status

### 3.1 Completed Components
- Message Types and Node States
- Node Base Class
- HeartbeatManager with three-phase mechanism
- RoutingManager with multi-metric routing
- CentralityCalculator with JGraphT integration
- PeerDirectory with route history
- NodeMetrics with resource tracking

### 3.2 Next Steps
1. Implement secure tunnel management
2. Add cluster member management
3. Enhance route optimization
4. Implement resource monitoring
5. Add performance metrics collection
6. Create comprehensive tests

### 3.3 Future Enhancements
1. Advanced security features
2. Dynamic resource optimization
3. Machine learning for route prediction
4. Cross-cluster optimization
5. Performance analytics dashboard
