Certainly! Here is a reorganized and clarified version of your meeting minutes:

---

## Meeting Minutes

### Date: [Insert Date]

### Participants: [Insert Participants]

### Topics Covered: Updates on Nearby Connections, Multi-hop Messaging, Cluster Management, and Project V Status

---

### **2\. Update 1: P2P Adhoc netowkring system protocol** 

#### 

#### **d. WiFi-Protocol Details**

* **Availability:**
  * Supported on Android 8 and above.
* **Cluster Formation:**
  * **Group Owner (GO):** Acts as the cluster head.
  * **Connection Limits:** Each GO can connect to up to 4 devices.
  * **Hierarchical Structure:** Each connected device (within the GO's cluster) can further connect to 3 additional GOs, forming a multi-tiered network.

#### **e. Multi-hop Messaging Implementation**

* **Current Limitation:**
  * Android documentation does not support multi-hop messaging (GO or cluster heads cannot relay messages).
* **Proposed Solution:**
  * **Implement Multi-hop Layer with the Following Steps:**
    1. **Heartbeat Signal:**
       * Broadcasted on the ad-hoc network to monitor node status.
       * Frequency decays exponentially over time (e.g., from every second to every minute).
    2. **Heartbeat Phases:**
       * **Heartbeat 1:** "I'm here."
         * **Response:** Acknowledgment ("We are aware of your presence. At least one connection will be made.")
       * **Heartbeat 2:** Stability Confirmation
         * Neighboring nodes recognize stability and begin forwarding to second-degree nodes.
       * **Heartbeat 3:** Network Expansion
         * Continues growing until the 4th layer, then transitions to Phase 3.
    3. **Phase 3:**
       * **Cluster Head Election:**
         * Nodes assess their position using "Eigen vector centrality."
         * Top N nodes (based on centrality index) within the subnetwork become cluster heads.
         * **N Calculation:** Based on the subnetwork size (e.g., 2^max_degree where max_degree is 4, allowing up to 1024 users per subnetwork).
       * **Cluster Head Responsibilities:**
         1. **Network Governance:**
            * Acts as a router for up to 1,000 users in the subnetwork.
            * Compensated with tokens.
         2. **Encrypt Tunnel Management:**
            * Manages encrypted tunnels within the subnet.
            * Maintains geographical location data of destinations and itself as a cluster head.
            * Facilitates secure connections between different clusters by leveraging existing encrypted tunnels.

#### **f. Cluster Head (CH) Role and Responsibilities**

* **Vision on the Entire Network:**
  * Oversees the network's structure and connectivity.
* **Calculation of N (Number of Cluster Heads):**
  * Based on the maximum degree and subnetwork size.
  * Example: For a subnetwork size of 1,000 users, there would be approximately 32 cluster heads.
* **Primary Responsibilities:**
  1. **Network Governance:**
     * Routes traffic for the subnetwork.
     * Financial compensation through tokens.
  2. **Encrypted Tunnel Management:**
     * Manages and maintains encrypted connections.
     * Determines the geographical location of destinations and ensures secure communication channels.

#### **g. Routing Process Example (Static Routing Use Case)**

* **Scenario:**
  * **Cluster A:**
    * **CHA:** Cluster Head A
    * **A1:** User has an encrypted connection with B1 in Cluster B.
    * **A2:**  User wants to send a request to B2


  * **Cluster B:**
    * **CHB:** Cluster Head 
    * **B1:** User has an encrypted connection with A1 in Cluster A.
    * **B2:** User expects a message request
* **Connection Request:**
  1. **A2** in **Cluster A** send an unencrypted request to connect to **B2** in **Cluster B**.
  2. **B2** asks **CHB** for any requests to **Cluster A**
  3. **CHB** checks **Cluster B** for existing encrypted connections to **Cluster A**.
  4. **CHB** finds **B1** and asks for a secure connection to **CHA**
  5. CHB informs CHA A2:B2 request and the available tunnel
  6. CHA informs A2 with the available tunnle and the availble secure path and can facilitate the secure connection

#### **h. Connectivity Scenarios**

* **With Internet Connectivity:**
  * Users can request additional encrypted connections to enhance bandwidth or connection quality.
* **Without Internet Connectivity:**
  * The network utilizes existing encrypted connections to maintain communication.

#### **i. Protocol Balancing Mechanism**

* Ensures the network adapts based on:
  1. **User Demand for Better Connections**
  2. **Availability of Users to Support Connections**

---

### **4\. Next Steps**

* **Implementation of Multi-hop Messaging:**
  * Develop the necessary protocols to enable message relaying between cluster heads.
* **Cluster Head Election Mechanism:**
  * Finalize the "Eigen values centrality" algorithm for determining cluster heads.
* **VPN Management Enhancements:**
  * Optimize VPN tunnel creation and maintenance between clusters.
* **Project V Continuation:**
  * Address remaining security aspects and finalize the Security Manager role.

---

### **5\. Action Items**

* **\[Assigned to Name\]:** Develop heartbeat signal protocol.
* **\[Assigned to Name\]:** Implement multi-hop messaging functionality.
* **\[Assigned to Name\]:** Finalize cluster head responsibilities and compensation model.
* **\[Assigned to Name\]:** Complete Project V security integrations.

---