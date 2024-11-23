### **Updated Testing Plan with Simulation Engine**

This updated testing plan integrates the **Simulation Engine** as a core component for the Monte Carlo-based System Testing. Below are the testing classes, tests, and requirements for the simulation engine to achieve the outlined goals.

---

## **Testing Architecture**

### **1. Testing Framework**
- **Framework:** Use **JUnit 5** for unit and integration testing.
- **Mocking Library:** Use **Mockito** for dependency mocking.
- **Monte Carlo Simulation:** Integrate the `SimulationEngine` for event-driven testing.

### **2. Testing Classes**
- `NodeUnitTests`: Tests the `Node` class's behavior.
- `ClusterIntegrationTests`: Validates the interaction between clusters, nodes, and routing components.
- `MonteCarloSimulationTests`: Leverages the `SimulationEngine` to test dynamic network scenarios and secure communication.

### **3. Supporting Components**
- **Simulation Engine**: Drives dynamic Monte Carlo-based tests.
- **Test Data Management**: Provides configurable inputs for probabilities and network size.
- **Visualization Tools**: Graphically represent metrics like cluster density, latency, and message delivery success rates.

---

## **Simulation Engine Requirements**
The `SimulationEngine` is the backbone of the Monte Carlo testing framework. It handles:
1. **Network Creation and Management**
   - Initialize nodes and clusters dynamically.
   - Assign nodes to clusters based on proximity and predefined logic.
   - Reorganize clusters when nodes fail, recover, or move.

2. **Probabilistic Event Management**
   - Simulate node-level events (failure, recovery, message generation) using:
     - Failure probability \( P_f \).
     - Recovery probability \( P_r \).
     - Message generation probability \( P_m \).
   - Trigger inter-cluster communication events, including secure handshakes.

3. **Metrics Collection**
   - Track and store:
     - Cluster count and density.
     - Latency and throughput.
     - Message delivery rate and handshake success rate.

4. **Simulation Control**
   - Allow parameter tuning for \( P_f, P_r, P_m, N \) (node count), and simulation duration.
   - Generate reproducible simulations using a random seed.

---

### **Simulation Engine Design**

#### **Class:** `SimulationEngine`
The central class orchestrating the simulation.

#### **Key Responsibilities:**
- **Network Initialization**
  - Create a network of nodes with random initial properties.
  - Form clusters and designate cluster heads.

- **Event Simulation**
  - Process probabilistic events (node failures, recoveries, and message generation).
  - Simulate cluster splitting, merging, and secure handshakes between clusters.

- **Metrics Collection**
  - Record key metrics (latency, throughput, cluster density).

- **Visualization**
  - Output results for visualization after each simulation step.

---

#### **SimulationEngine Implementation**

##### **Attributes**
```java
public class SimulationEngine {
    private List<Node> nodes;
    private List<Cluster> clusters;
    private Map<Node, Cluster> nodeClusterMap;
    private Random random;
    private double failureProbability;
    private double recoveryProbability;
    private double messageGenerationProbability;
    private int simulationSteps;
    private List<SimulationMetrics> metrics;
}
```

##### **Methods**
```java
public class SimulationEngine {
    public SimulationEngine(int nodeCount, double failureProbability, double recoveryProbability, double messageGenerationProbability, int simulationSteps) {
        this.random = new Random();
        this.nodes = new ArrayList<>();
        this.clusters = new ArrayList<>();
        this.nodeClusterMap = new HashMap<>();
        this.failureProbability = failureProbability;
        this.recoveryProbability = recoveryProbability;
        this.messageGenerationProbability = messageGenerationProbability;
        this.simulationSteps = simulationSteps;
        this.metrics = new ArrayList<>();
        initializeNetwork(nodeCount);
    }

    // Initialize network with nodes and clusters
    private void initializeNetwork(int nodeCount) {
        for (int i = 0; i < nodeCount; i++) {
            Node node = new Node("Node" + i, "Address" + i);
            nodes.add(node);
            assignToCluster(node);
        }
    }

    // Assign nodes to clusters
    private void assignToCluster(Node node) {
        if (clusters.isEmpty() || random.nextDouble() < 0.3) { // Randomly create new cluster
            Cluster cluster = new Cluster(new ClusterHead(node));
            clusters.add(cluster);
            nodeClusterMap.put(node, cluster);
        } else {
            Cluster cluster = clusters.get(random.nextInt(clusters.size()));
            cluster.addMember(node);
            nodeClusterMap.put(node, cluster);
        }
    }

    // Run simulation steps
    public void runSimulation() {
        for (int step = 0; step < simulationSteps; step++) {
            simulateEvents();
            collectMetrics(step);
        }
        outputResults();
    }

    // Simulate events like failures, recoveries, and message generation
    private void simulateEvents() {
        for (Node node : nodes) {
            if (node.getState() == NodeState.ACTIVE && random.nextDouble() < failureProbability) {
                node.setState(NodeState.FAILED);
                handleNodeFailure(node);
            } else if (node.getState() == NodeState.FAILED && random.nextDouble() < recoveryProbability) {
                node.setState(NodeState.ACTIVE);
                handleNodeRecovery(node);
            } else if (node.getState() == NodeState.ACTIVE && random.nextDouble() < messageGenerationProbability) {
                generateMessage(node);
            }
        }
    }

    // Handle node failure
    private void handleNodeFailure(Node node) {
        Cluster cluster = nodeClusterMap.get(node);
        cluster.removeMember(node);
        if (cluster.getClusterHead().equals(node)) {
            cluster.electClusterHead();
        }
    }

    // Handle node recovery
    private void handleNodeRecovery(Node node) {
        assignToCluster(node);
    }

    // Generate a message and route it
    private void generateMessage(Node node) {
        Node targetNode = nodes.get(random.nextInt(nodes.size()));
        if (!node.equals(targetNode)) {
            Message message = new Message(node.getNodeId(), targetNode.getNodeId(), "TestPayload".getBytes(), MessageType.UNICAST);
            node.sendMessage(message);
        }
    }

    // Collect simulation metrics
    private void collectMetrics(int step) {
        int activeNodes = (int) nodes.stream().filter(node -> node.getState() == NodeState.ACTIVE).count();
        int totalClusters = clusters.size();
        metrics.add(new SimulationMetrics(step, activeNodes, totalClusters));
    }

    // Output simulation results
    private void outputResults() {
        for (SimulationMetrics metric : metrics) {
            System.out.println(metric);
        }
    }
}
```

---

### **Monte Carlo Test Cases Using Simulation Engine**
1. **Dynamic Cluster Formation**
   - Run simulation with varying node counts (\( N = 50, 100, 500 \)).
   - Observe cluster count and cluster head elections.

2. **Secure Handshake Testing**
   - Simulate inter-cluster communication between two clusters.
   - Measure handshake latency and success rate.

3. **Performance Under Load**
   - Increase message generation probability (\( P_m \)) and observe latency and delivery rates.

