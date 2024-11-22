# V-Routing Protocol Architecture

## Overview
The V-Routing Protocol is designed to enable ad-hoc networking on top of Wi-Fi Direct, focusing on multi-hop capabilities and secure communication. This document outlines the core architecture and components.

## Core Components

### Node Management
- **Base Node**: Abstract class defining core node functionality
- **Regular Node**: Standard network participant
- **Cluster Head Node**: Node with additional routing and management capabilities
- **Node States**: Discovery → Regular → Cluster Head (if eligible)

### Network Discovery
#### Heartbeat System
- **Phase 1 (Discovery)**: High-frequency heartbeats (1s interval)
  - Limited to immediate neighbors
  - Basic network topology discovery
- **Phase 2 (Formation)**: Medium-frequency heartbeats (4s interval)
  - Extended network discovery
  - Route information collection
- **Phase 3 (Stable)**: Low-frequency heartbeats (18s interval)
  - Network maintenance
  - Route updates
  - Cluster head announcements

### Routing System
#### Route Management
- **Route Discovery**: Dynamic path finding
- **Route Selection**: Multi-criteria optimization
  - Path length
  - Link quality
  - Node stability
  - Battery level
- **Route Table**: Dynamic routing information storage
  - Path options
  - Metrics
  - Last update time

### Cluster Management
#### Cluster Head Selection
- Based on:
  - Eigenvector centrality
  - Node capabilities
  - Configuration flags
- No explicit election process
- Automatic promotion based on metrics

### Security
#### Two-Layer Security
1. **SSH Key-Based Tunnels**
   - Pre-configured SSH keys
   - Secure tunnel establishment
   - Inter-cluster communication
2. **Ad-hoc Network Security**
   - End-to-end encryption
   - Multi-hop secure routing

## Message Processing

### Message Flow
1. Message Reception
2. Type Identification
3. Processing based on:
   - Message type
   - Current node state
   - Network conditions
4. Response/Forward decision
5. Message dispatch

### Message History
- Tracks message paths
- Prevents routing loops
- Contributes to centrality calculations

## Network Health

### Monitoring
- Node availability
- Route quality
- Cluster stability
- Security status

### Maintenance
- Automatic route updates
- Dynamic cluster head adjustments
- Security tunnel management

## Implementation Considerations

### Extensibility
- Modular component design
- Interface-based interactions
- Pluggable routing metrics

### Performance
- Efficient message handling
- Optimized route calculations
- Minimal network overhead

### Reliability
- Acknowledgment-based messaging
- Redundant paths
- Automatic recovery mechanisms
