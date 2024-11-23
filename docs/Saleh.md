I can see the web application, but when I press on "Start Simulation" I don't see any results

# V-Routing Protocol Documentation

## Method Naming Convention

### Peer Update Methods
In our codebase, we have standardized the method naming for peer updates:

1. In `PeerDirectory.java`:
   - Method: `updatePeer(String nodeId, Message message)`
   - Purpose: Updates peer information including last seen timestamp, message, and route history
   - Also updates adjacency matrix and triggers centrality recalculation if needed

2. In `Node.java`:
   - Method: `updatePeerInfo(String nodeId, Message message)`
   - Purpose: Acts as a wrapper around PeerDirectory's updatePeer method
   - Called by HeartbeatManager during heartbeat handling

3. In `HeartbeatManager.java`:
   - Calls: `node.updatePeerInfo(message.getSourceNodeId(), message)`
   - Used in both handleHeartbeat and handleHeartbeatResponse methods

### Implementation Details
The peer update process involves:
1. Updating the peer's last seen timestamp
2. Storing the latest message from the peer
3. Updating route history
4. Updating the network adjacency matrix
5. Potentially triggering centrality recalculation

### Design Decisions
- We maintain `updatePeerInfo` in Node.java as it better describes the high-level operation from the Node's perspective
- The underlying `updatePeer` in PeerDirectory remains focused on the actual peer data update operation
- This separation provides good abstraction while maintaining clear method naming that reflects each layer's responsibility

## Network Structure
- Nodes maintain peer relationships through the PeerDirectory
- HeartbeatManager handles periodic peer updates
- CentralityCalculator uses peer information to determine network structure and node importance

# Simulation Issue
**Problem**: Web application is visible but pressing "Start Simulation" shows no results.

## Root Cause
The simulation endpoint is missing in the TestController. Currently, the controller only has a basic `/test` endpoint.

## Solution
1. Add Simulation Endpoint
   ```java
   @PostMapping("/simulation/start")
   public ResponseEntity<SimulationResponse> startSimulation(@RequestBody SimulationRequest request) {
       logger.info("Starting simulation with request: {}", request);
       // Add simulation logic here
       return ResponseEntity.ok(new SimulationResponse());
   }
   ```

2. Required Changes:
   - Create SimulationRequest and SimulationResponse classes
   - Add simulation service to handle the business logic
   - Update frontend to call the correct endpoint
   - Add proper error handling and response status codes

3. Verification:
   - Test the endpoint using curl or Postman before using the UI
   - Check logs to ensure the endpoint is being called
   - Verify the response format matches what the frontend expects

## Temporary Workaround
Until the endpoint is implemented:
1. Use the existing `/test` endpoint to verify the API is working
2. Check browser console for specific error messages
3. Ensure all services are running properly

## Troubleshooting Steps
1. Check Browser Console
   - Open browser developer tools (F12)
   - Look for any JavaScript errors in the console
   - Verify that the simulation request is being sent

2. Check Network Tab
   - In browser developer tools, go to Network tab
   - Click "Start Simulation" and watch for API calls
   - Verify the response from the server

3. Backend Verification
   - Check application logs for any errors
   - Verify that TestController is receiving the simulation request
   - Ensure all required services are running

4. Common Solutions
   - Clear browser cache and reload the page
   - Ensure you're running the latest version of the application
   - Check if backend services are properly initialized

If the issue persists:
1. Enable debug logging in the application
2. Check if the simulation data is being properly generated
3. Verify the WebSocket connection if real-time updates are used


====================================

# Rabbit Hole 1

## Changes Made
1. Created New Files:
   - `src/main/java/com/vrouting/vrouting/model/SimulationRequest.java`
     ```java
     public class SimulationRequest {
         private int nodeCount;
         private int duration;
         private boolean enableClustering;
         // Getters and setters
     }
     ```

   - `src/main/java/com/vrouting/vrouting/model/SimulationResponse.java`
     ```java
     public class SimulationResponse {
         private String simulationId;
         private List<String> nodes;
         private Map<String, List<String>> connections;
         private boolean success;
         private String message;
         // Getters and setters
     }
     ```

   - `src/main/java/com/vrouting/vrouting/service/SimulationService.java`
     ```java
     @Service
     public class SimulationService {
         private final Map<String, List<Node>> activeSimulations;
         private final NetworkConfig networkConfig;
         // Methods for starting/stopping simulations
     }
     ```

   - `src/main/java/com/vrouting/network/socket/core/SimulationNode.java`
     ```java
     public class SimulationNode extends Node {
         public SimulationNode(String nodeId, NetworkConfig config) {
             super(nodeId, config);
         }
         // Override abstract methods
     }
     ```

2. Modified Files:
   - `src/main/java/com/vrouting/vrouting/controller/TestController.java`
     - Added simulation endpoints:
       - POST `/api/simulation/start`
       - POST `/api/simulation/{simulationId}/stop`
     - Added SimulationService dependency

## How to Revert
To revert these changes:

1. Delete New Files:
   ```bash
   rm src/main/java/com/vrouting/vrouting/model/SimulationRequest.java
   rm src/main/java/com/vrouting/vrouting/model/SimulationResponse.java
   rm src/main/java/com/vrouting/vrouting/service/SimulationService.java
   rm src/main/java/com/vrouting/network/socket/core/SimulationNode.java
   ```

2. Restore TestController.java to original state:
   ```java
   @RestController
   public class TestController {
       private static final Logger logger = LoggerFactory.getLogger(TestController.class);

       public TestController() {
           logger.info("TestController initialized");
       }

       @GetMapping("/test")
       public String test() {
           logger.info("Test endpoint called");
           return "API is working";
       }
   }
   ```

## Testing the Changes
To test if changes are working:
1. Start the application
2. Send POST request to `/api/simulation/start`:
   ```json
   {
       "nodeCount": 5,
       "duration": 60,
       "enableClustering": true
   }
   ```
3. Check response for simulationId and node information
4. Use `/api/simulation/{simulationId}/stop` to stop the simulation

## Known Issues
1. Node creation might fail if NetworkConfig is not properly initialized
2. SimulationNode implementation is minimal and might need more specific behavior
3. No WebSocket implementation yet for real-time updates

## Endpoint Conflict Resolution and Simulation Management Improvements

### Issue Description
There was a conflict between two controllers (`TestController` and `SimulationController`) that were both trying to handle the same endpoint `/api/simulation/start`. This caused the application to fail during startup with an ambiguous mapping error.

### Changes Made

#### 1. Controller Reorganization

##### TestController Changes
- Removed simulation-related endpoints from `TestController`
- Kept only the basic API test endpoint `/api/test`
- Removed unused imports and `SimulationService` dependency

##### SimulationController Updates
- Added proper base path mapping `@RequestMapping("/api/simulation")`
- Added `SimulationService` integration for managing simulations
- Added logging support
- Consolidated all simulation-related endpoints:
  - POST `/api/simulation/start` - Start a new simulation
  - POST `/api/simulation/{simulationId}/stop` - Stop a running simulation
  - POST `/api/simulation/{simulationId}/step` - Step through simulation
  - GET `/api/simulation/{simulationId}/metrics` - Get simulation metrics
  - GET `/api/simulation/{simulationId}/status` - Get simulation status

#### 2. SimulationService Improvements

##### Added New Features
- Added `SimulationEngine` management with a dedicated map to track engines per simulation
- Added methods to support simulation lifecycle:
  - `stepSimulation(String simulationId)`
  - `getMetrics(String simulationId)`
  - `getStatus(String simulationId)`
- Improved simulation status reporting with additional metrics:
  - Initialization status
  - Simulation completion status
  - Steps completed
  - Total steps
  - Number of active simulations

##### Configuration Management
- Moved network configuration to per-node basis
- Added default simulation parameters:
  - Failure probability: 0.1
  - Recovery probability: 0.5
  - Message generation probability: 0.3
  - Default simulation steps: 100

### API Endpoints Documentation

#### Start Simulation
```http
POST /api/simulation/start
Content-Type: application/json

{
  "nodeCount": 5,
  "duration": 60,
  "enableClustering": true
}
```

#### Stop Simulation
```http
POST /api/simulation/{simulationId}/stop
```

#### Step Simulation
```http
POST /api/simulation/{simulationId}/step
```

#### Get Metrics
```http
GET /api/simulation/{simulationId}/metrics
```

#### Get Status
```http
GET /api/simulation/{simulationId}/status
```

### Implementation Notes

1. **Thread Safety**: Using `HashMap` for storing active simulations and engines. If concurrent access becomes a requirement, consider switching to `ConcurrentHashMap`.

2. **Resource Management**: Each simulation is properly cleaned up when stopped:
   - Nodes are stopped
   - Engine is removed from tracking
   - Resources are released

3. **Error Handling**: Added proper error handling and logging throughout the simulation lifecycle.

4. **Configuration**: Network configuration is now more flexible with per-node settings.

### Future Improvements

1. **Clustering Support**
   - Implement cluster formation logic
   - Add cluster-specific metrics
   - Support cluster head election

2. **Enhanced Metrics**
   - Add network topology visualization
   - Track message delivery success rates
   - Monitor network partitioning

3. **Simulation Control**
   - Add pause/resume functionality
   - Support simulation speed control
   - Add simulation replay capabilities

4. **Persistence**
   - Add simulation state persistence
   - Support for saving/loading simulation configurations
   - Export simulation results