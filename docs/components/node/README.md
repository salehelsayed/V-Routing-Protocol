# Node Components and Behavior Documentation

## Node Hierarchy

### Abstract Base Node
```java
public abstract class Node {
    // Core components
    protected final String nodeId;
    protected final NodeState state;
    protected final PeerDirectory peerDirectory;
    protected final RoutingTable routingTable;
    protected final AdjacencyMatrix adjacencyMatrix;
    protected final HeartbeatManager heartbeatManager;
    
    // Configuration
    protected final NodeConfig config;
    protected final boolean isAllowedClusterHead;
    
    // Network metrics
    protected double centralityScore;
    protected int networkDepth;
    protected Phase currentPhase;
}
```

### Node States
```java
public enum NodeState {
    INITIALIZING,    // Initial setup
    DISCOVERING,     // Network discovery phase
    REGULAR,         // Regular node operation
    CLUSTER_HEAD,    // Operating as cluster head
    DISCONNECTED     // Temporarily disconnected
}
```

### Node Phases
```java
public enum Phase {
    NEWBIE(0.3),      // First 30% of exponential curve
    REGULAR(0.7),     // Middle phase
    STABLE(1.0);      // Final stable phase
    
    private final double threshold;
    Phase(double threshold) {
        this.threshold = threshold;
    }
}
```

## Core Components

### 1. Heartbeat Management
```java
public class HeartbeatManager {
    private final Node node;
    private final ScheduledExecutorService scheduler;
    private final List<Long> heartbeatIntervals;
    private int currentIntervalIndex;
    
    // Exponential backoff configuration
    private static final long INITIAL_INTERVAL = 1000; // 1 second
    private static final long MAX_INTERVAL = 60000;    // 1 minute
    private static final double BACKOFF_FACTOR = 2.0;  // Double interval each time
    
    public void initialize() {
        // Calculate intervals following negative exponential curve
        heartbeatIntervals = calculateIntervals();
        startHeartbeat();
    }
    
    private void startHeartbeat() {
        scheduler.scheduleAtFixedRate(
            this::sendHeartbeat,
            0,
            getCurrentInterval(),
            TimeUnit.MILLISECONDS
        );
    }
    
    private void sendHeartbeat() {
        Message heartbeat = new Message(
            node.getNodeId(),
            MessageType.HEARTBEAT,
            createHeartbeatPayload()
        );
        
        // In NEWBIE phase, limit propagation
        if (node.getCurrentPhase() == Phase.NEWBIE) {
            heartbeat.setMaxHops(1);
        } else {
            heartbeat.setMaxHops(node.getNetworkDepth());
        }
        
        node.broadcast(heartbeat);
        updateInterval();
    }
}
```

### 2. Peer Directory
```java
public class PeerDirectory {
    private final Map<String, PeerInfo> peers;
    private final Map<String, List<Route>> routingHistory;
    
    public class PeerInfo {
        private final String nodeId;
        private NodeState state;
        private long lastSeen;
        private int hopCount;
        private double reliability;
        private List<Route> knownRoutes;
    }
    
    public void updatePeer(String nodeId, Message message) {
        PeerInfo info = peers.computeIfAbsent(nodeId, PeerInfo::new);
        info.lastSeen = System.currentTimeMillis();
        info.hopCount = message.getHopCount();
        
        // Update routing history
        if (message.hasRouteInfo()) {
            updateRoutingHistory(nodeId, message.getRouteInfo());
        }
        
        // Clean up old entries
        cleanupOldEntries();
    }
}
```

### 3. Adjacency Matrix
```java
public class AdjacencyMatrix {
    private final int[][] matrix;
    private final Map<String, Integer> nodeIndexMap;
    private final Map<Integer, String> indexNodeMap;
    
    public void updateConnection(String nodeA, String nodeB, boolean connected) {
        int indexA = getOrCreateNodeIndex(nodeA);
        int indexB = getOrCreateNodeIndex(nodeB);
        
        matrix[indexA][indexB] = connected ? 1 : 0;
        matrix[indexB][indexA] = connected ? 1 : 0;
    }
    
    public double calculateEigenvectorCentrality(String nodeId) {
        // Use JGraphT or custom implementation
        return CentralityCalculator.calculate(this, nodeId);
    }
}
```

## Node Behavior by Phase

### 1. Network Discovery (NEWBIE Phase)
```java
private void handleNewbiePhase() {
    // 1. Send frequent heartbeats (1-second intervals)
    heartbeatManager.setToNewbieMode();
    
    // 2. Build initial network vision
    peerDirectory.enableRapidUpdates();
    
    // 3. Monitor network responses
    if (hasEnoughNetworkInfo()) {
        transitionToRegularPhase();
    }
}
```

### 2. Regular Operation
```java
private void handleRegularPhase() {
    // 1. Update adjacency matrix
    updateAdjacencyMatrix();
    
    // 2. Calculate centrality if allowed to be cluster head
    if (isAllowedClusterHead) {
        calculateAndCheckCentrality();
    }
    
    // 3. Participate in routing
    handleRoutingRequests();
}
```

### 3. Cluster Head Operation
```java
private void handleClusterHeadPhase() {
    // 1. Manage secure tunnels
    manageTunnels();
    
    // 2. Coordinate cluster communication
    coordinateCluster();
    
    // 3. Maintain inter-cluster routes
    maintainInterClusterRoutes();
}
```

## Security Management

### 1. Secure Tunnel Handling
```java
public class SecureTunnelManager {
    private final Map<String, SecureTunnel> tunnels;
    private final KeyManager keyManager;
    
    public void establishTunnel(String remoteClusterHead) {
        // 1. Check for existing SSH keys
        SSHKey key = keyManager.getKeyForNode(remoteClusterHead);
        if (key == null) {
            // Use pre-shared keys from configuration
            key = keyManager.getPreSharedKey(remoteClusterHead);
        }
        
        // 2. Create and store tunnel
        SecureTunnel tunnel = new SecureTunnel(remoteClusterHead, key);
        tunnels.put(remoteClusterHead, tunnel);
    }
}
```

### 2. Message Encryption
```java
public class MessageEncryption {
    private final KeyManager keyManager;
    
    public byte[] encryptMessage(Message message, String destinationId) {
        // 1. Get appropriate key (tunnel or pre-shared)
        EncryptionKey key = keyManager.getKeyForDestination(destinationId);
        
        // 2. Encrypt using AES-256
        return encrypt(message.serialize(), key);
    }
}
```

## Route Management

### 1. Route Selection
```java
public class RouteManager {
    private final Map<String, List<Route>> routes;
    private final RouteSelectionConfig config;
    
    public Route selectBestRoute(String destination) {
        List<Route> available = routes.get(destination);
        if (available == null || available.isEmpty()) {
            return null;
        }
        
        return available.stream()
            .max(Comparator.comparingDouble(this::calculateRouteScore))
            .orElse(null);
    }
    
    private double calculateRouteScore(Route route) {
        // Apply configured weights to various metrics
        return config.getWeightedScore(route);
    }
}
```

### 2. Route Table Updates
```java
public class RouteTable {
    private final Map<String, List<Route>> routes;
    private final ScheduledExecutorService scheduler;
    
    public void initialize() {
        // Schedule periodic updates
        scheduler.scheduleAtFixedRate(
            this::updateRoutes,
            config.getUpdateInterval(),
            config.getUpdateInterval(),
            TimeUnit.MILLISECONDS
        );
    }
    
    private void updateRoutes() {
        // 1. Remove expired routes
        removeExpiredRoutes();
        
        // 2. Request updates for critical routes
        requestRouteUpdates();
        
        // 3. Notify dependents of changes
        notifyRouteChanges();
    }
}
```
