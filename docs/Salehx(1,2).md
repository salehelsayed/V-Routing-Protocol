# V-Routing Protocol Simulation Fix - Approach 3

## Current Issue
After reviewing the codebase and previous fix attempts, the main issue appears to be that the simulation is not properly stepping through and updating its state. When clicking "Start Simulation", nothing happens because:

1. The simulation engine is created but not actively running steps
2. The frontend is not properly polling for updates
3. The metrics collection may not be working correctly

## Proposed Solution

### 1. Backend Changes

#### SimulationEngine.java
```java
public class SimulationEngine implements Serializable {
    // Add new fields to track simulation state
    private boolean isRunning = false;
    private Thread simulationThread;
    
    // Modify startSimulation to run in a separate thread
    public void startSimulation() {
        if (isRunning) return;
        isRunning = true;
        simulationThread = new Thread(() -> {
            while (isRunning && currentStep < simulationSteps && !simulationComplete) {
                simulateStep();
                try {
                    Thread.sleep(1000); // 1 second delay between steps
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
            simulationComplete = true;
            isRunning = false;
        });
        simulationThread.start();
    }
    
    // Add method to stop simulation
    public void stopSimulation() {
        isRunning = false;
        if (simulationThread != null) {
            simulationThread.interrupt();
        }
    }
    
    // Enhance simulateStep to collect more detailed metrics
    public void simulateStep() {
        if (simulationComplete) return;
        
        int activeNodes = 0;
        int messagesThisStep = 0;
        int deliveredThisStep = 0;
        
        // Simulate node failures and recoveries
        for (Node node : nodes) {
            if (node.isActive()) {
                if (random.nextDouble() < failureProbability) {
                    node.setActive(false);
                } else {
                    activeNodes++;
                }
            } else {
                if (random.nextDouble() < recoveryProbability) {
                    node.setActive(true);
                    activeNodes++;
                }
            }
        }
        
        // Simulate message generation and routing
        for (Node node : nodes) {
            if (node.isActive() && random.nextDouble() < messageGenerationProbability) {
                Node destination = nodes.get(random.nextInt(nodes.size()));
                if (destination != node) {
                    messagesThisStep++;
                    if (simulateMessageDelivery(node, destination)) {
                        deliveredThisStep++;
                    }
                }
            }
        }
        
        // Update metrics
        messagesSent += messagesThisStep;
        messagesDelivered += deliveredThisStep;
        metrics.add(new SimulationMetrics(
            currentStep,
            activeNodes,
            nodes.size(),
            messagesThisStep,
            deliveredThisStep,
            messagesSent,
            messagesDelivered
        ));
        
        currentStep++;
    }
}
```

#### SimulationService.java
```java
@Service
public class SimulationService {
    // Add method to check if simulation is active
    public boolean isSimulationActive(String simulationId) {
        SimulationEngine engine = simulationEngines.get(simulationId);
        return engine != null && !engine.isSimulationComplete();
    }
    
    // Enhance getStatus to include more details
    public Map<String, Object> getStatus(String simulationId) {
        SimulationEngine engine = simulationEngines.get(simulationId);
        Map<String, Object> status = new HashMap<>();
        
        if (engine == null) {
            status.put("error", "Simulation not found");
            status.put("initialized", false);
            return status;
        }
        
        status.put("initialized", true);
        status.put("simulationComplete", engine.isSimulationComplete());
        status.put("stepsCompleted", engine.getCurrentStep());
        status.put("totalSteps", engine.getSimulationSteps());
        status.put("activeNodes", engine.getActiveNodeCount());
        status.put("totalNodes", engine.getTotalNodeCount());
        status.put("messagesSent", engine.getMessagesSent());
        status.put("messagesDelivered", engine.getMessagesDelivered());
        
        return status;
    }
}
```

### 2. Frontend Changes

#### index.html
```javascript
let simulationId = null;
let statusCheckInterval = null;
let metricsCheckInterval = null;

async function startSimulation() {
    const nodeCount = parseInt(document.getElementById('nodeCount').value);
    
    try {
        // Start simulation
        const response = await fetch('/api/simulation/start', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                nodeCount: nodeCount
            })
        });
        
        const data = await response.json();
        simulationId = data.simulationId;
        
        // Start polling for updates
        statusCheckInterval = setInterval(checkSimulationStatus, 1000);
        metricsCheckInterval = setInterval(updateMetrics, 1000);
        
        // Update UI
        document.getElementById('startButton').disabled = true;
        document.getElementById('stopButton').disabled = false;
        document.getElementById('simulationStatus').textContent = 'Simulation running...';
    } catch (error) {
        console.error('Error starting simulation:', error);
        document.getElementById('simulationStatus').textContent = 'Error starting simulation';
    }
}

async function checkSimulationStatus() {
    try {
        const response = await fetch('/api/simulation/status');
        const status = await response.json();
        
        if (status.error) {
            document.getElementById('simulationStatus').textContent = status.error;
            return;
        }
        
        // Update progress
        const progress = (status.stepsCompleted / status.totalSteps) * 100;
        document.getElementById('simulationProgress').style.width = `${progress}%`;
        
        // Update status text
        document.getElementById('simulationStatus').textContent = 
            `Step ${status.stepsCompleted}/${status.totalSteps} - ` +
            `Active Nodes: ${status.activeNodes}/${status.totalNodes} - ` +
            `Messages: ${status.messagesDelivered}/${status.messagesSent}`;
        
        // Check if simulation is complete
        if (status.simulationComplete) {
            clearInterval(statusCheckInterval);
            clearInterval(metricsCheckInterval);
            document.getElementById('startButton').disabled = false;
            document.getElementById('stopButton').disabled = true;
            document.getElementById('simulationStatus').textContent = 'Simulation complete';
        }
    } catch (error) {
        console.error('Error checking simulation status:', error);
    }
}

async function updateMetrics() {
    try {
        const response = await fetch('/api/simulation/metrics');
        const metrics = await response.json();
        
        if (metrics.length === 0) return;
        
        // Update charts with new metrics data
        updateCharts(metrics);
    } catch (error) {
        console.error('Error updating metrics:', error);
    }
}
```

## Implementation Steps

1. First, implement the SimulationEngine changes:
   - Add new fields for tracking simulation state
   - Modify startSimulation to run in a separate thread
   - Enhance simulateStep with better metrics collection

2. Update SimulationService:
   - Add isSimulationActive method
   - Enhance getStatus with more detailed information
   - Improve error handling

3. Update the frontend:
   - Add proper polling intervals
   - Improve progress and status display
   - Add better error handling
   - Update charts with new metrics

4. Testing:
   - Test simulation start/stop
   - Verify metrics collection
   - Check UI updates
   - Test error scenarios

## Verification Steps

1. Start the application and open the web interface
2. Enter a node count (e.g., 10) and click "Start Simulation"
3. Verify that:
   - Progress bar updates
   - Status text shows current step and metrics
   - Charts update with new data
   - Simulation completes successfully

## Expected Results

- The simulation should start running immediately after clicking "Start Simulation"
- Progress bar should update every second
- Status should show current step, active nodes, and message statistics
- Charts should update with new metrics data
- Simulation should complete after reaching the specified number of steps

## Error Handling

The solution includes improved error handling:
- Backend returns proper error messages
- Frontend displays error messages to user
- Polling intervals are cleared on errors
- UI is updated appropriately in error cases

## Notes

This approach differs from previous attempts by:
1. Running simulation in a separate thread
2. Adding more detailed metrics collection
3. Improving frontend polling and UI updates
4. Adding better error handling
5. Including progress tracking

If this approach doesn't work, we should:
1. Add debug logging throughout the simulation process
2. Check for thread safety issues
3. Verify network connectivity
4. Test with smaller node counts first
