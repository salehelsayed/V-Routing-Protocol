### **1. Unit Testing**
#### **Tests to Cover:**
##### **Node Class**
1. **Initialization Tests**
   - Verify that a new `Node` instance initializes with default state, ID, and address.
   - Confirm dependencies (`ConnectionManager`, `HeartbeatManager`, etc.) are properly instantiated.
2. **State Management**
   - Verify `setState()` updates the node's state correctly.
   - Ensure `getState()` returns the correct current state.
3. **Connection Management**
   - Test `addConnection()` adds a connection to the list and updates `ConnectionManager`.
   - Test `removeConnection()` removes a connection and updates `ConnectionManager`.
   - Validate behavior when attempting to remove a non-existent connection.
4. **Cluster Management**
   - Verify `joinCluster()` adds the node to a cluster and updates its current cluster reference.
   - Test `leaveCluster()` removes the node from a cluster and clears its reference.
   - Confirm correct log entries for `joinCluster()` and `leaveCluster()`.
5. **Heartbeat Management**
   - Test `sendHeartbeat()` invokes the `HeartbeatManager` and sends a proper heartbeat signal.
   - Verify `receiveHeartbeat()` processes an incoming signal and updates node metrics.
6. **Message Handling**
   - Test `sendMessage()` routes the message using `RoutingManager`.
   - Verify `receiveMessage()` decrypts the payload, updates routing history, and processes the message.

##### **Cluster Class**
1. **Membership Management**
   - Verify `addMember()` adds a node to the cluster and establishes intra-cluster connections.
   - Test `removeMember()` removes a node and cleans up connections.
2. **Cluster Head Management**
   - Test `electClusterHead()` calculates centrality and selects the correct cluster head.
   - Confirm `setClusterHead()` updates the cluster head and logs appropriately.
3. **Routing**
   - Verify `routeMessage()` forwards messages to the correct destination within the cluster.

##### **HeartbeatManager**
1. **Frequency Adjustment**
   - Test that `adjustHeartbeatFrequency()` increases intervals on stability.
   - Verify max interval constraints are respected.
2. **Heartbeat Sending**
   - Confirm `sendHeartbeat()` broadcasts a properly formatted heartbeat.
   - Test failure scenarios where no nodes respond.
3. **Heartbeat Reception**
   - Verify `receiveHeartbeat()` updates the status of the sending node.
   - Test handling of repeated or invalid heartbeat signals.

---

### **2. Integration Testing**
#### **Tests to Cover:**
##### **Cluster Formation**
1. Validate the discovery of neighboring nodes using `ClusterManager.discoverClusters()`.
2. Verify that nodes group into clusters based on proximity or configuration.
3. Ensure the proper designation of cluster heads during cluster formation.

##### **Cluster Management**
1. Test adding a node to a cluster updates the cluster list and routing table.
2. Verify removing a node adjusts cluster membership and routing tables.
3. Simulate a node joining multiple clusters and confirm expected behavior.

##### **Routing**
1. Test message routing within a cluster to ensure the shortest path is selected.
2. Verify routing updates when a node leaves or fails within the cluster.
3. Confirm messages are forwarded to the correct cluster head in inter-cluster scenarios.

##### **Secure Communication**
1. Validate encrypted tunnels are established using `ConnectionManager` and `EncryptionManager`.
2. Test data transmission over secure tunnels for data integrity and security.
3. Simulate key rotation and validate session continuity.

---

### **3. System Testing**
#### **Tests to Cover:**
##### **Multi-Node Deployment**
1. Test a 10-node network simulating heartbeat signals, message routing, and cluster formation.
2. Increase node count incrementally to evaluate scalability and latency.
3. Validate proper cluster reorganization during node mobility.

##### **Message Routing**
1. Send a message through multiple clusters and confirm correct delivery.
2. Simulate link failures and verify the fallback routing mechanism.

##### **Secure Communication**
1. Test a message's end-to-end encryption and decryption.
2. Verify secure tunnel establishment between clusters.
3. Simulate key or certificate compromise and confirm proper failure response.

##### **Performance Metrics**
1. Measure network latency under normal and high traffic conditions.
2. Validate throughput by simulating large data transfers between clusters.
3. Test resource utilization (CPU, memory, bandwidth) across varying node counts.

---

Expanding the **System Testing** section into a Monte Carlo simulation framework that dynamically changes network properties and events based on randomized probabilities:

---

### **System Testing Using Monte Carlo Simulation**
**Objective:** Simulate the behavior of the ad-hoc network under different randomized conditions to evaluate robustness, scalability, security, and performance. The simulation will involve probabilistic events like node failures, network topology changes, and cluster interactions, including secure handshakes.

---

#### **1. Simulation Components**

##### **Nodes:**
- Each node in the simulation has properties such as:
  - **ID:** A unique identifier.
  - **State:** Active, failed, or recovering.
  - **Cluster Membership:** Which cluster it belongs to.
  - **Probability of Events:**
    - **Failure Probability (Pf):** Probability of a node failing.
    - **Recovery Probability (Pr):** Probability of a failed node recovering.
    - **Message Generation Probability (Pm):** Probability of initiating a message exchange.

##### **Clusters:**
- **Dynamic Clusters:** Clusters form dynamically based on proximity or configuration.
- **Cluster Heads (CH):** Nodes with high centrality, elected dynamically.
- **Inter-Cluster Connections:** Secure tunnels between cluster heads (as explained in **salah.md**).

##### **Events:**
- **Node Failures:** Simulated with a probability \( P_f \).
- **Node Recovery:** Simulated with a probability \( P_r \).
- **Message Generation:** Nodes generate messages to other nodes or clusters with \( P_m \).
- **Cluster Restructuring:** Dynamic reorganization of clusters due to node failures/recovery.

---

#### **2. Simulation Process**

1. **Initialization:**
   - Specify the number of nodes \( N \) and clusters \( C \).
   - Define probabilities \( P_f, P_r, P_m \) for all nodes.
   - Randomly initialize node positions and assign them to clusters.

2. **Simulation Steps (Repeated \( T \) Times):**
   - **Step 1: Node Events**
     - For each node:
       - Determine if it fails using \( P_f \).
       - If failed, determine recovery using \( P_r \).
       - If active, determine if it generates a message using \( P_m \).

   - **Step 2: Cluster Events**
     - For each cluster:
       - Check cluster head availability (elect new CH if failed).
       - Handle cluster mergers or splits based on node activity.

   - **Step 3: Inter-Cluster Communication**
     - Simulate secure handshakes between cluster heads when inter-cluster messages are initiated.
     - Verify the security protocols (TLS, AES-256) and measure handshake latency.

   - **Step 4: Metrics Collection**
     - Track metrics such as:
       - **Cluster Count:** Number of active clusters.
       - **Node Connectivity:** Percentage of connected nodes.
       - **Message Delivery Rate:** Ratio of successful to total messages.
       - **Security Handshake Success Rate:** Ratio of successful to total handshake attempts.
       - **Latency:** Average time for message delivery and handshake completion.

3. **Parameter Variation:**
   - Vary \( N, P_f, P_r, P_m \), and network structure parameters to test different scenarios.
   - Simulate small networks (10 nodes) to large networks (1000+ nodes).

---

#### **3. Secure Handshake Testing**
**Scenario:** Secure connection between Cluster A and Cluster B.

- **Setup:**
  - Cluster A has nodes \( A_1, A_2, \dots \) with \( CH_A \) as the cluster head.
  - Cluster B has nodes \( B_1, B_2, \dots \) with \( CH_B \) as the cluster head.
  - Secure tunnel is established using a pre-shared key or public/private key exchange (as per **salah.md**).

- **Events:**
  1. Node \( A_1 \) in Cluster A sends a message to \( B_2 \) in Cluster B.
  2. \( CH_A \) initiates a secure handshake with \( CH_B \).
     - Handshake involves:
       - Exchanging certificates (TLS 1.3).
       - Establishing a secure channel (AES-256 encryption).
  3. Message is routed through the secure tunnel to \( CH_B \), which forwards it to \( B_2 \).

- **Metrics:**
  - **Handshake Latency:** Time taken to establish the secure tunnel.
  - **Tunnel Reliability:** Number of failed handshakes out of total attempts.
  - **Message Integrity:** Ensure message arrives unaltered.

---

#### **4. Metrics and Outputs**
The simulation logs and outputs will include:
- **Network Metrics:**
  - Total nodes, active nodes, failed nodes.
  - Number of clusters and average cluster size.
  - Number of inter-cluster connections.
- **Performance Metrics:**
  - Average latency for intra-cluster and inter-cluster messages.
  - Message delivery success rate.
- **Security Metrics:**
  - Success rate of secure handshakes.
  - Average handshake latency.
  - Message integrity validation results.

---

#### **5. Implementation Plan**

1. **Simulator Design:**
   - Create a `Simulator` class to manage the nodes, clusters, and events.
   - Implement random probability-based event triggers.
   - Use Monte Carlo iterations for robustness.

2. **Cluster Dynamics:**
   - Extend the `Cluster` class to handle dynamic node addition/removal and re-election of cluster heads.

3. **Security Protocol Testing:**
   - Simulate TLS and AES-256 handshake workflows.
   - Log handshake failures and debug issues.

4. **Visualization:**
   - Plot cluster counts, message delivery rates, and handshake latencies against iterations.
