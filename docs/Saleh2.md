# V-Routing Protocol Simulation Fix Plan

Context: 
The "Start Simulation" button in the V-Routing Protocol Simulation Dashboard is not working as intended.
The simulation status and metrics are not being displayed correctly.
The following are the steps to fix the issue:

1- Impelemnt the following steps
2- create a new .md file called tracker and write the things you did so I can see the changes and in case things didn't work we know what was changed

## Root Cause Analysis

After investigating the codebase, I've identified several critical issues preventing the simulation metrics and steps from being displayed:

1. **Simulation State Management**:
   - The `SimulationService` creates a new `SimulationEngine` but doesn't store the simulation ID in the engine
   - The frontend makes requests using `/api/simulation/status` and `/api/simulation/metrics` without a simulation ID
   - The status endpoint returns incomplete data when simulation is not found

2. **Metrics Collection**:
   - `SimulationEngine.simulateStep()` collects metrics but doesn't expose step count
   - No proper initialization of metrics list in `SimulationEngine`
   - Missing proper error handling for metrics retrieval

3. **Frontend-Backend Communication**:
   - Frontend doesn't store or use the simulation ID returned from `/api/simulation/start`
   - Status polling doesn't include proper error handling
   - Metrics polling doesn't handle empty or error responses

## Fix Plan

### 1. Backend Fixes

#### SimulationController.java
```java
@GetMapping("/status")
public ResponseEntity<Map<String, Object>> getCurrentStatus() {
    // Get the most recent simulation status
    String latestSimulationId = simulationService.getLatestSimulationId();
    if (latestSimulationId == null) {
        return ResponseEntity.ok(Map.of(
            "initialized", false,
            "simulationComplete", false,
            "stepsCompleted", 0,
            "totalSteps", 0,
            "error", "No active simulation"
        ));
    }
    return ResponseEntity.ok(simulationService.getStatus(latestSimulationId));
}

@GetMapping("/metrics")
public ResponseEntity<List<SimulationMetrics>> getCurrentMetrics() {
    String latestSimulationId = simulationService.getLatestSimulationId();
    if (latestSimulationId == null) {
        return ResponseEntity.ok(Collections.emptyList());
    }
    return ResponseEntity.ok(simulationService.getMetrics(latestSimulationId));
}
```

#### SimulationService.java
```java
private String latestSimulationId;

public String getLatestSimulationId() {
    return latestSimulationId;
}

public SimulationResponse startSimulation(SimulationRequest request) {
    // ... existing code ...
    
    try {
        // ... existing code ...
        
        latestSimulationId = simulationId;
        SimulationEngine engine = new SimulationEngine(
            request.getNodeCount(),
            request.getFailureProbability(),
            request.getRecoveryProbability(),
            request.getMessageGenerationProbability(),
            request.getSimulationSteps()
        );
        simulationEngines.put(simulationId, engine);
        
        // ... rest of existing code ...
    }
    catch (Exception e) {
        // ... existing error handling ...
    }
}

public Map<String, Object> getStatus(String simulationId) {
    SimulationEngine engine = simulationEngines.get(simulationId);
    if (engine == null) {
        return Map.of(
            "initialized", false,
            "simulationComplete", false,
            "stepsCompleted", 0,
            "totalSteps", 0,
            "error", "Simulation not found"
        );
    }
    
    return Map.of(
        "initialized", true,
        "simulationComplete", engine.isSimulationComplete(),
        "stepsCompleted", engine.getCurrentStep(),
        "totalSteps", engine.getSimulationSteps(),
        "activeNodes", engine.getActiveNodeCount(),
        "totalNodes", engine.getTotalNodeCount()
    );
}

@Scheduled(fixedRate = 1000)  // Run every second
public void executeSimulationSteps() {
    if (latestSimulationId != null) {
        SimulationEngine engine = simulationEngines.get(latestSimulationId);
        if (engine != null && !engine.isSimulationComplete()) {
            stepSimulation(latestSimulationId);
        }
    }
}
```

#### SimulationEngine.java
```java
public int getCurrentStep() {
    return currentStep;
}

public int getSimulationSteps() {
    return simulationSteps;
}

public boolean isSimulationComplete() {
    return simulationComplete || currentStep >= simulationSteps;
}

public int getActiveNodeCount() {
    return (int) nodes.stream().filter(Node::isActive).count();
}

public int getTotalNodeCount() {
    return nodes.size();
}

public void simulateStep() {
    if (isSimulationComplete()) {
        return;
    }
    
    // ... existing simulation logic ...
    
    currentStep++;
    simulationComplete = currentStep >= simulationSteps;
    collectMetrics();
}

private int messagesSent = 0;
private int messagesDelivered = 0;
private double totalLatency = 0;

private void generateMessageFromNode(Node node) {
    List<Node> activeNodes = getActiveNodes();
    if (activeNodes.size() > 1) {
        Node targetNode = selectRandomTarget(node, activeNodes);
        messagesSent++;
        
        if (canDeliverMessage(node, targetNode)) {
            messagesDelivered++;
            totalLatency += calculateMessageLatency(node, targetNode);
        }
    }
}

private double calculateMessageDeliveryRate() {
    return messagesSent > 0 ? (double) messagesDelivered / messagesSent : 0.0;
}

private double calculateAverageLatency() {
    return messagesDelivered > 0 ? totalLatency / messagesDelivered : 0.0;
}

private int getTotalMessagesSent() {
    return messagesSent;
}

private int getTotalMessagesDelivered() {
    return messagesDelivered;
}
```

### 2. Frontend Fixes

#### index.html
```javascript
let currentSimulationId = null;

function startSimulation() {
    const config = {
        nodeCount: parseInt(document.getElementById('nodeCount').value),
        failureProbability: parseFloat(document.getElementById('failureProbability').value),
        recoveryProbability: parseFloat(document.getElementById('recoveryProbability').value),
        messageGenerationProbability: parseFloat(document.getElementById('messageGenerationProbability').value),
        simulationSteps: parseInt(document.getElementById('simulationSteps').value)
    };

    const statusText = document.getElementById('statusText');
    statusText.textContent = 'Starting...';
    statusText.className = 'status-indicator running';

    fetch('/api/simulation/start', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify(config),
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            currentSimulationId = data.simulationId;
            document.getElementById('stepsCompleted').textContent = '0';
            startPolling();
        } else {
            throw new Error(data.message || 'Failed to start simulation');
        }
    })
    .catch((error) => {
        console.error('Error:', error);
        statusText.textContent = 'Error';
        statusText.className = 'status-indicator not-started';
        alert('Failed to start simulation: ' + error.message);
    });
}

function checkStatusAndMetrics() {
    fetch('/api/simulation/status')
        .then(response => response.json())
        .then(status => {
            const statusText = document.getElementById('statusText');
            if (status.error) {
                statusText.textContent = 'Error: ' + status.error;
                statusText.className = 'status-indicator not-started';
                return;
            }
            
            statusText.textContent = status.simulationComplete ? 'Completed' : 'Running';
            statusText.className = `status-indicator ${status.simulationComplete ? 'completed' : 'running'}`;
            
            document.getElementById('stepsCompleted').textContent = status.stepsCompleted;
            
            if (!status.simulationComplete) {
                fetchMetrics();
                setTimeout(checkStatusAndMetrics, 1000);
            }
        })
        .catch(error => {
            console.error('Error:', error);
            const statusText = document.getElementById('statusText');
            statusText.textContent = 'Error';
            statusText.className = 'status-indicator not-started';
        });
}

function updateTable(metrics) {
    const tbody = document.querySelector('#metricsTable tbody');
    tbody.innerHTML = '';
    
    metrics.forEach(metric => {
        const row = tbody.insertRow();
        row.insertCell(0).textContent = metric.step;
        row.insertCell(1).textContent = metric.activeNodes;
        row.insertCell(2).textContent = metric.failedNodes;
        row.insertCell(3).textContent = (metric.messageDeliveryRate * 100).toFixed(1) + '%';
        row.insertCell(4).textContent = metric.averageLatency.toFixed(2) + 'ms';
        row.insertCell(5).textContent = metric.messagesSent;
        row.insertCell(6).textContent = metric.messagesDelivered;
    });
}

function updateChart(metrics) {
    const labels = metrics.map(m => m.step);
    const activeNodes = metrics.map(m => m.activeNodes);
    const deliveryRates = metrics.map(m => m.messageDeliveryRate * 100);
    
    metricsChart.data.labels = labels;
    metricsChart.data.datasets[0].data = activeNodes;
    metricsChart.data.datasets[1].data = deliveryRates;
    metricsChart.update();
}
```

## Implementation Steps

1. Backend Changes:
   - Update `SimulationController.java` to add new endpoints
   - Modify `SimulationService.java` to track latest simulation
   - Enhance `SimulationEngine.java` with new methods

2. Frontend Changes:
   - Update the JavaScript code in `index.html`
   - Add error handling and proper status display
   - Improve metrics visualization

3. Testing:
   - Test simulation start/stop functionality
   - Verify metrics collection and display
   - Test error handling scenarios

## Expected Results

After implementing these fixes:
1. The simulation status will properly update
2. Steps completed will increment as the simulation progresses
3. Metrics will be displayed and updated in real-time
4. Error handling will be more robust
5. The UI will provide better feedback about simulation state

## Additional Notes

- The fixes maintain backward compatibility with existing code
- Error handling has been improved throughout the system
- The solution doesn't require any additional dependencies
- All changes follow the existing code style and patterns
