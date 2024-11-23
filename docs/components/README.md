# Core Components

## Node Management
The V-Routing Protocol is built around a sophisticated node management system that handles network discovery, routing, and cluster formation.

### Node States
Nodes can be in one of the following states:
- `DISCOVERY`: Initial state, discovering network
- `REGULAR`: Normal operation
- `CLUSTER_HEAD`: Operating as a cluster head
- `CLUSTER_MEMBER`: Part of a cluster
- `ISOLATED`: Not connected to any cluster
- `ERROR`: Error state

### Node Phases
Nodes progress through different phases during their lifecycle:
- `DISCOVERY`: Initial discovery phase where node is discovering its neighbors
- `STABILIZATION`: Network stabilization phase where centrality is calculated and cluster heads are elected
- `REGULAR`: Normal operation phase where routing and message forwarding occur
- `STABLE`: Stable operation phase with optimized routing and cluster structure

## Message Types
The protocol uses various message types for different purposes:

### Network Discovery
- `HEARTBEAT_DISCOVERY`: Initial high-frequency heartbeats
- `HEARTBEAT_REGULAR`: Regular heartbeats with increased depth
- `HEARTBEAT_RESPONSE`: Response containing network topology info
- `HEARTBEAT_CLUSTER_HEAD`: Heartbeat from cluster head with routing info

### Routing Operations
- `ROUTE_DISCOVERY`: Find path to destination
- `ROUTE_RESPONSE`: Response with available paths
- `ROUTE_UPDATE`: Update existing route metrics
- `ROUTE_ERROR`: Notify route failure/metric degradation

### Cluster Management
- `CLUSTER_HEAD_ANNOUNCE`: Node declaring cluster head status
- `CLUSTER_HEAD_UPDATE`: Updates about cluster head state/metrics
- `CLUSTER_JOIN_REQUEST`: Request to join a cluster
- `CLUSTER_JOIN_RESPONSE`: Accept/reject cluster join

### Security & Tunneling
- `SECURE_TUNNEL_REQUEST`: Request secure tunnel establishment
- `SECURE_TUNNEL_RESPONSE`: Tunnel parameters and key exchange
- `SECURE_TUNNEL_ESTABLISHED`: Confirm tunnel establishment
- `SECURE_TUNNEL_ERROR`: Report tunnel establishment failure

### Data Transfer
- `DATA_TRANSFER`: Regular data message
- `DATA_ACKNOWLEDGMENT`: Confirm data receipt
- `DATA_FORWARD`: Forward data through multi-hop path

## Key Components

### HeartbeatManager
Implements a three-phase heartbeat mechanism:
- Phase 1: High-frequency discovery heartbeats
- Phase 2: Regular heartbeats with increased depth
- Phase 3: Cluster-aware heartbeats with routing info
- Uses negative exponential decay intervals for network stabilization

### RoutingManager
Handles dynamic routing table management:
- Multi-metric route selection (latency, bandwidth, hop count, stability)
- Periodic route table updates
- Route expiration and cleanup
- Path optimization based on network conditions

### CentralityCalculator
Calculates node centrality for cluster head election:
- Uses JGraphT library for eigenvector centrality calculation
- Fallback power iteration method for reliability
- Considers multiple factors:
  - Network centrality score
  - Battery level (minimum 30%)
  - Processing capacity (minimum 50%)
  - Node configuration eligibility

### PeerDirectory
Manages peer information and network topology:
- Maintains peer connection states
- Tracks route history
- Manages adjacency matrix for centrality calculations
- Handles periodic cleanup of stale entries

### MessageDispatcher
Handles message routing and delivery:
- Message deduplication
- Route-based forwarding
- Network-wide broadcasts
- Message validation and error handling

### NodeMetrics
Tracks node performance metrics:
- Battery level (0.0 - 1.0)
- Processing capacity (0.0 - 1.0)
- Network strength (0.0 - 1.0)
- Reliability score (0.0 - 1.0)
- Overall node score calculation
