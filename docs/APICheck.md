# V-Routing Protocol API Endpoints

## Base URL
All endpoints are prefixed with `/api` unless otherwise specified.

## Test Endpoint
- **GET** `/test`
  - Description: Simple test endpoint to verify API is working
  - Response: String
  - Example Response: `"API is working"`

## Simulation Endpoints

### Start Simulation
- **POST** `/simulation/start`
  - Description: Start a new simulation
  - Request Body:
    ```json
    {
        "nodeCount": 10
    }
    ```
  - Response: SimulationResponse object containing simulationId

### Stop Simulation
- **POST** `/simulation/{simulationId}/stop`
  - Description: Stop a running simulation
  - Parameters:
    - `simulationId`: ID of the simulation to stop
  - Response: Empty response with 200 OK status

### Step Simulation
- **POST** `/simulation/{simulationId}/step`
  - Description: Execute one step of the simulation
  - Parameters:
    - `simulationId`: ID of the simulation to step
  - Response: String status message

### Get Current Status
- **GET** `/simulation/status`
  - Description: Get status of the most recent simulation
  - Response:
    ```json
    {
        "initialized": true,
        "simulationComplete": false,
        "stepsCompleted": 5,
        "totalSteps": 100,
        "error": null
    }
    ```

### Get Specific Simulation Status
- **GET** `/simulation/{simulationId}/status`
  - Description: Get status of a specific simulation
  - Parameters:
    - `simulationId`: ID of the simulation to check
  - Response: Same as current status endpoint

### Get Current Metrics
- **GET** `/simulation/metrics`
  - Description: Get metrics of the most recent simulation
  - Response: List of SimulationMetrics objects

### Get Specific Simulation Metrics
- **GET** `/simulation/{simulationId}/metrics`
  - Description: Get metrics of a specific simulation
  - Parameters:
    - `simulationId`: ID of the simulation to get metrics for
  - Response: List of SimulationMetrics objects

## Updated API Test Results (Using api_test_v2.bat)

### All Endpoints Working:

1. **GET** `/test`
   - Status: 
   - Response: `API is working`

2. **POST** `/simulation/start`
   - Status: 
   - Response: Successfully creates simulation and returns simulation ID and nodes
   - Sample ID: `f4af1177-7eb5-47e0-bb97-b3852f1b9152`

3. **GET** `/simulation/status`
   - Status: 
   - Response:
   ```json
   {
       "stepsCompleted": 0,
       "initialized": true,
       "totalSteps": 100,
       "simulationComplete": false,
       "activeSimulations": 3
   }
   ```

4. **GET** `/simulation/{simulationId}/status`
   - Status: 
   - Response: Same format as general status endpoint

5. **GET** `/simulation/metrics` and `/simulation/{simulationId}/metrics`
   - Status: 
   - Response: `[]` (empty array for new simulation)

6. **POST** `/simulation/{simulationId}/step`
   - Status: 
   - Response: `Step completed`
   - Effect: Verified by checking status after step (stepsCompleted increased from 0 to 1)

7. **POST** `/simulation/{simulationId}/stop`
   - Status: 
   - Response: Empty response with 200 OK status

### Improvements Made:

1. Fixed simulation ID handling in the test script:
   - Properly extracts simulation ID from JSON response
   - Removes quotes and special characters
   - Uses clean ID in subsequent API calls

2. All endpoints now working as expected with proper simulation ID handling

### Verification:

1. Simulation Flow:
   - Start simulation ➡️ Get ID ➡️ Check status ➡️ Step simulation ➡️ Verify step count increased ➡️ Stop simulation
   - All steps completed successfully

2. Data Consistency:
   - Status updates reflect simulation progress
   - Step count increments correctly
   - Metrics tracking is initialized

### Next Steps:

1. Consider adding more detailed responses for:
   - Simulation stop confirmation
   - Metrics data structure when simulation progresses
   - Error cases and edge conditions

2. Add tests for:
   - Invalid simulation IDs
   - Multiple concurrent simulations
   - Edge cases in step counts
   - Error handling scenarios
