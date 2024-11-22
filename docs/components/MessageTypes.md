# Message Types in V-Routing Protocol

## Overview
The V-Routing Protocol uses various message types to facilitate network discovery, maintenance, and data transfer. This document describes each message type and its role in the protocol.

## Message Categories

### Discovery and Heartbeat Messages
- `HEARTBEAT`: Standard heartbeat message for maintaining network presence
- `HEARTBEAT_DISCOVERY`: Used during initial network discovery phase
- `HEARTBEAT_STABLE`: Heartbeat message used in stable network state
- `HEARTBEAT_UPDATE`: Message containing updated node information
- `HEARTBEAT_RESPONSE`: Response to heartbeat messages with network topology info
- `HEARTBEAT_ACK`: Acknowledgment of received heartbeat messages

### Cluster Management Messages
- `JOIN`: Request to join a cluster
- `LEAVE`: Notification of leaving a cluster
- `UPDATE`: General cluster state update message
- `ACK`: General acknowledgment message

### Routing Messages
- `ROUTE_REQUEST`: Request to discover route to destination
- `ROUTE_REPLY`: Response containing route information
- `ROUTE_REPLY_ACK`: Acknowledgment of route reply receipt
- `ROUTE_RESPONSE`: Detailed response with route metrics

### Data Messages
- `DATA`: Regular data transfer message
- `DATA_ACK`: Acknowledgment of data receipt

## Message Flow Examples

### Network Discovery Process
1. New node broadcasts `HEARTBEAT_DISCOVERY`
2. Neighboring nodes respond with `HEARTBEAT_RESPONSE`
3. Node transitions to stable state using `HEARTBEAT_STABLE`

### Route Discovery Process
1. Node sends `ROUTE_REQUEST`
2. Intermediate nodes forward request
3. Destination responds with `ROUTE_REPLY`
4. Source acknowledges with `ROUTE_REPLY_ACK`

### Data Transfer Process
1. Node sends `DATA` message
2. Recipient responds with `DATA_ACK`

## Implementation Notes

### Heartbeat Mechanism
- Initial discovery phase: High-frequency heartbeats
- Stable phase: Reduced frequency heartbeats
- Response handling includes network topology updates

### Routing Strategy
- Dynamic route discovery
- Route maintenance through periodic updates
- Support for multi-hop routing

### Cluster Management
- Cluster formation through JOIN/LEAVE messages
- State synchronization using UPDATE messages
- Acknowledgment-based reliability
