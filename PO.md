Revised Layered Architecture Design
1. Application Layer
Responsibilities
This layer provides the interface and orchestrates the network's functionalities for the upper-level user or system.
Expected outputs
Secure Connection Ensures TLS 1.3 connections for encrypted communication.
Connection Metadata Provides details such as
Number of physical connections (for ad-hoc).
Connection health (e.g., latency, jitter, and packet loss rate).
Connection type (ad-hoc, tunnel, or hybrid).
Connection security level (e.g., TLS vs. fallback to AES256 for offline mode).
Implementation Details
Abstract Connection Manager Interface
Exposes API for requesting, managing, and closing connections.
Automatically determines the most secure available connection method.
Connection Health Monitor
Runs in parallel and gathers metrics such as latency, availability of fallback routes, and current security status.
Communicates health data via a dedicated monitoring channel alongside the socket connection.
2. Network Management Layer
This layer is critical and needs to be split into two sublayers for better modularity and focus.

2.1 Cluster Management Sublayer
Responsibilities
Handles cluster formation, maintenance, and role assignments (e.g., Group Owner and Cluster Head).
Manages the cluster head election using eigenvector centrality or similar methods.
Ensures scalability by adapting to changes in cluster size and membership.
Implementation Details
Cluster Discovery
Broadcasts periodic discovery signals to identify neighboring nodes.
Maintains a registry of known nodes with roles, status, and available connections.
Cluster Head Election
Calculates eigenvector centrality for eligible nodes.
Implements redundancy for the cluster head role to ensure robustness.
Cluster Hierarchy Synchronization
Shares updates on cluster membership and routing pathways with peer clusters.
2.2 Routing Sublayer
Responsibilities
Manages the flow of messages within clusters (intra-cluster) and between clusters (inter-cluster).
Implements routing mechanisms, including
Dynamic Source Routing (DSR) Within clusters.
Hybrid Routing Protocol For inter-cluster communication (utilizing encrypted tunnels and fallback mechanisms).
Implementation Details
Routing Table Management
Maintains real-time updates of reachable nodes and optimal paths.
Prioritizes routes based on user preferences (configurable via properties file).
Fallback Mechanism
Switches between ad-hoc, tunnel, and hybrid connections dynamically based on availability and performance metrics.
3. Security Layer
Responsibilities
Provides encryption and secure communication mechanisms at all layers of the network stack.
Handles key exchange, certificate validation, and connection integrity.
Implementation Details
TLS 1.3
Primary protocol for secure connections where internet access is available.
Provides forward secrecy and reduced latency for secure handshakes.
Offline Mode Security
AES-256 encryption with pre-shared keys for ad-hoc communication.
Publicprivate key pairs for inter-cluster secure tunnels.
Session Management
Manages the lifecycle of security keys and certificates.
Periodically rotates keys to enhance security.
4. Communication Layer
Responsibilities
Establishes and maintains the raw connections for message exchange and network operations.
Implements the heartbeat mechanism to monitor node status and ensure connectivity.
Expanded Heartbeat Implementation
Broadcast Mechanism
Sends periodic signals to neighboring nodes to confirm availability.
Signal frequency decays over time starts every second, scales up to every minute.
Heartbeat Phases
Phase 1 Initialization
New node broadcasts “I’m here” to neighboring nodes.
Receives acknowledgment to confirm entry into the cluster.
Phase 2 Stabilization
Stability indicators ensure the node is part of a functioning cluster.
Neighboring nodes propagate the node's status to second-degree nodes.
Phase 3 Expansion
Supports up to 4 hops, after which the node either joins a cluster or acts as a standalone unit.
Failure Detection
Absence of heartbeat signals triggers a node removal event.
Routing tables update to reflect the node's unavailability.
Integration with Monitoring
Heartbeat data feeds into the Application Layer’s health monitoring system.
Enables proactive rerouting and resource optimization.
Overall Key Features
Separation of Concerns
Clear modularity ensures that each layer handles a specific set of responsibilities.
Facilitates testing, debugging, and future enhancements.
Interface-Based Design
Both the Network Management Layer and Security Layer rely on interfaces, making it easy to swap implementations for different environments (e.g., replacing Java Sockets with gRPC in the future).
Scalability
Hierarchical cluster management and hybrid routing protocols ensure that the architecture scales well in dynamic environments.
Extensibility
Routing priorities, fallback mechanisms, and connection policies are configurable via a properties file.