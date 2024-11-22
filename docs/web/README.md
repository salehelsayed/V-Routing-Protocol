# V-Routing Protocol Web Visualization

This web application provides a real-time visualization of the V-Routing Protocol network simulation. It allows users to configure simulation parameters, observe network behavior, and monitor performance metrics.

## Features

- Interactive network visualization using D3.js
- Real-time updates via WebSocket connection
- Configurable simulation parameters
- Live performance metrics display
- Drag-and-drop node positioning
- Cluster visualization with color coding

## Setup Instructions

### Backend (Spring Boot)

1. Build the project:
   ```bash
   ./gradlew build
   ```

2. Run the Spring Boot application:
   ```bash
   ./gradlew bootRun
   ```

The backend server will start on `http://localhost:8080`.

### Frontend (React)

1. Navigate to the frontend directory:
   ```bash
   cd frontend
   ```

2. Install dependencies:
   ```bash
   npm install
   ```

3. Start the development server:
   ```bash
   npm start
   ```

The frontend application will be available at `http://localhost:3000`.

## Usage

1. Open the web application in your browser at `http://localhost:3000`
2. Configure simulation parameters:
   - Number of nodes
   - Failure probability
   - Recovery probability
3. Click "Start Simulation" to begin
4. Observe the network visualization and metrics
5. Use drag-and-drop to reposition nodes
6. Click "Stop Simulation" to end the simulation

## Network Visualization

- Blue nodes: Regular nodes
- Red nodes: Cluster heads
- Gray nodes: Inactive nodes
- Lines: Connections between nodes in the same cluster

## Performance Metrics

- Active Nodes: Number of currently active nodes
- Cluster Count: Number of formed clusters
- Average Path Length: Average number of hops between nodes
- Network Stability: Percentage of time nodes maintain their connections
- Message Success Rate: Percentage of successfully delivered messages
