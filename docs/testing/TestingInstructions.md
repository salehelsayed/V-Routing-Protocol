# V-Routing Protocol Testing Instructions

## 1. Core Component Testing

### 1.1 Heartbeat Mechanism Testing
```java
public class HeartbeatTest {
    @Test
    void testPhaseTransitions() {
        Node node = new RegularNode("test-node", "127.0.0.1");
        HeartbeatManager manager = node.getHeartbeatManager();
        
        // Phase 1: Discovery (1s intervals)
        assertEquals(1000, manager.getCurrentInterval());
        assertTrue(manager.isInDiscoveryPhase());
        
        // Phase 2: Formation (4s intervals)
        simulateNetworkStabilization(node);
        assertEquals(4000, manager.getCurrentInterval());
        assertTrue(manager.isInFormationPhase());
        
        // Phase 3: Stable (18s intervals)
        simulateFullStabilization(node);
        assertEquals(18000, manager.getCurrentInterval());
        assertTrue(manager.isInStablePhase());
    }
}
```

### 1.2 Message Propagation Testing
```java
public class MessagePropagationTest {
    @Test
    void testMessageDepthControl() {
        Network network = createTestNetwork(10); // 10 nodes
        Node source = network.getNode(0);
        
        // Test message depth in different phases
        Message msg = new Message(MessageType.HEARTBEAT_DISCOVERY);
        source.broadcast(msg);
        
        // Phase 1: Depth should be 1
        assertEquals(1, msg.getMaxDepth());
        
        // Phase 2: Depth should increase
        network.advanceToFormationPhase();
        msg = new Message(MessageType.HEARTBEAT_REGULAR);
        source.broadcast(msg);
        assertTrue(msg.getMaxDepth() > 1);
    }
}
```

## 2. Network Formation Testing

### 2.1 Cluster Formation Test
```java
public class ClusterFormationTest {
    @Test
    void testClusterHeadPromotion() {
        NetworkConfig config = new NetworkConfig()
            .setAllowClusterHead(true)
            .setCentralityThreshold(0.7);
            
        Node node = new RegularNode("test-node", config);
        simulateHighCentrality(node);
        
        // Verify promotion to cluster head
        assertTrue(node instanceof ClusterHeadNode);
        assertTrue(node.isClusterHead());
    }
}
```

### 2.2 Routing Table Test
```java
public class RoutingTableTest {
    @Test
    void testRouteDiscovery() {
        Network network = createMeshNetwork(5, 5); // 5x5 grid
        Node source = network.getNode(0, 0);
        Node dest = network.getNode(4, 4);
        
        // Test route discovery
        Route route = source.findRouteTo(dest);
        assertNotNull(route);
        assertTrue(route.getHops().size() <= 8); // Max possible hops
    }
}
```

## 3. Security Testing

### 3.1 Tunnel Establishment
```java
public class TunnelTest {
    @Test
    void testSecureTunnelCreation() {
        ClusterHeadNode head1 = createClusterHead("CH1");
        ClusterHeadNode head2 = createClusterHead("CH2");
        
        // Test tunnel establishment
        SecureTunnel tunnel = head1.establishTunnelWith(head2);
        assertTrue(tunnel.isEstablished());
        assertTrue(tunnel.isEncrypted());
    }
}
```

### 3.2 Message Security
```java
public class MessageSecurityTest {
    @Test
    void testMessageEncryption() {
        Message msg = new Message("sensitive-data");
        Node source = new RegularNode("source");
        Node dest = new RegularNode("dest");
        
        // Test end-to-end encryption
        Message encrypted = source.prepareMessage(msg, dest);
        assertTrue(encrypted.isEncrypted());
        assertEquals(msg.getContent(), 
                    dest.decryptMessage(encrypted).getContent());
    }
}
```

## 4. Test Scenarios

### 4.1 Network Health Scenarios
1. **Node Join/Leave**
   - Add new nodes during operation
   - Remove nodes abruptly
   - Verify network adapts correctly

2. **Message Routing**
   - Test with increasing network size
   - Verify route optimization
   - Check message deduplication

3. **Cluster Management**
   - Test cluster head changes
   - Verify cluster member updates
   - Check inter-cluster routing

### 4.2 Performance Scenarios
1. **Load Testing**
   - Increase message frequency
   - Add more nodes
   - Monitor resource usage

2. **Stability Testing**
   - Run long-duration tests
   - Simulate network partitions
   - Test recovery mechanisms

## 5. Test Environment Setup

### 5.1 Local Testing
```bash
# Setup test environment
./gradlew setupTests

# Run specific test categories
./gradlew test --tests "*.HeartbeatTest"
./gradlew test --tests "*.ClusterTest"
./gradlew test --tests "*.SecurityTest"

# Run all tests with coverage
./gradlew testWithCoverage
```

### 5.2 Network Simulation
```java
public class NetworkSimulator {
    public static Network createTestNetwork(int size) {
        Network network = new Network();
        for (int i = 0; i < size; i++) {
            network.addNode(new RegularNode("node-" + i));
        }
        return network;
    }
    
    public static void simulateNetworkTraffic(Network network) {
        // Generate random messages
        // Simulate node movements
        // Inject failures
    }
}
```

## 6. Test Validation

### 6.1 Success Criteria
- All unit tests pass
- Integration tests pass
- Performance meets requirements:
  - Message latency < 100ms
  - Memory usage < 512MB
  - CPU usage < 60%

### 6.2 Test Coverage
- Minimum 80% code coverage
- All critical paths tested
- Edge cases covered

## 7. Debugging Guidelines

### 7.1 Logging Setup
```java
public class TestLogger {
    public static void setupTestLogging() {
        Logger.setLevel(Level.DEBUG);
        Logger.addHandler(new FileHandler("test.log"));
    }
}
```

### 7.2 Common Issues
1. **Message Loss**
   - Check network connectivity
   - Verify message queues
   - Check routing tables

2. **Performance Issues**
   - Monitor memory usage
   - Check message queue sizes
   - Verify thread pool usage

3. **Security Issues**
   - Verify key management
   - Check encryption status
   - Validate tunnel establishment
