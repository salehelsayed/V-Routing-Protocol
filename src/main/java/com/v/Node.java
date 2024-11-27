package com.v;

import java.io.*;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.Queue;
import java.util.UUID;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;

import com.v.Protocols.DSR;
import com.v.Protocols.Protocol;
import com.v.connections.Connection;
import com.v.connections.ConnectionFactory;

import com.v.connections.P2P.P2PConnection;
import com.v.connections.packets.Packet;
import com.v.connections.packets.P2PPacket;
import com.v.connections.packets.MultiHopPacket;
import com.v.connections.adhoc.MultiHopConnection;

import java.util.HashMap;

public class Node {

    private String nodeId;
    private String ip;
    private int port;

    private List<Node> neighbors;
    private Map<String, Connection> connections;
    private int portStart;
    private int portEnd;

    // New fields
    private Queue<Packet> processingQueue; // To register blocked requests
    private int totalDemands; // Total demands made by this node
    private int connectedDemands; // Total successful connections made by this node

    public Node( String ip, int portStart, int portEnd) throws IOException {
        this.nodeId = generateNodeId();
        this.ip = ip;

        this.neighbors = new ArrayList<>();
        this.portStart = portStart;
        this.portEnd = portEnd;

        // Initialize new fields
        this.processingQueue = new LinkedList<>();
        this.totalDemands = 0;
        this.connectedDemands = 0;

        // Use the parent ConnectionFactory
        ConnectionFactory factory = ConnectionFactory.getInstance(ip, this.port, this.portEnd);
        this.connections.putAll(
                factory.createInboundConnections(this.nodeId) // Updated parameters
        );

    }

    private String generateNodeId() {
        return UUID.randomUUID().toString();
    }

    public String getIp() {
        return ip;
    }

    public int getPort() {
        return port;
    }

    public Map<String, Connection> getConnections() {
        return this.connections;
    }

    public void establishP2PConnection(Node neighbor) throws IOException {
        String key = neighbor.getIp();
        if (!connections.containsKey(key)) {
            ConnectionFactory factory = ConnectionFactory.getInstance(this.nodeId, this.portStart, this.portEnd);
            Connection connection = null;
            try {
                connection = factory.createOutboundConnection(this.nodeId,
                        this.ip, this.port, neighbor.getIp());
                if (connection == null) {
                    // No available port, fallback to multihop
                    System.out.println("Falling back to multihop connection");
                    // Implement multihop connection logic here
                } else {
                    key = neighbor.getIp() + ":" + connection.getDestinationPort();
                    connections.put(key, connection);
                }
            } catch (IOException e) {
                System.out.println("Failed to establish P2P connection: " + e.getMessage());
                // Fallback to multihop
                System.out.println("Falling back to multihop connection");
                // Implement multihop connection logic here
            }
        }
    }

    public Connection getConnection(String destinationIP) {
        for (String key : connections.keySet()) {
            if (key.startsWith(destinationIP)) {
                return connections.get(key);
            }
        }
        return null;
    }

    @Override
    public String toString() {
        StringBuilder connectionsString = new StringBuilder();
        connectionsString.append("Connections: [");

        // Retrieve all connections from the factory
        Map<String, Connection> allConnections = ConnectionFactory.getInstance(this.nodeId, this.portStart, this.portEnd)
                .getConnections();

        // Append each connection to the string
        for (Connection connection : allConnections.values()) {
            connectionsString.append(connection.getSourceIP()).append(" -> ").append(connection.getDestinationIP())
                    .append(", ");
        }

        // // Remove the last comma and space if there are any connections
        // if (connectionsString.length() > 13) { // 13 is the length of "Connections:
        // ["
        // connectionsString.setLength(connectionsString.length() - 2); // Remove last
        // comma and space
        // }
        connectionsString.append("]");

        return "Node{" +
                "nodeId=" + nodeId +
                ", ip='" + ip + '\'' +
                ", port=" + port +
                ", " + connectionsString +
                ", totalDemands=" + totalDemands +
                ", connectedDemands=" + connectedDemands +
                ", blockedRequests=" + processingQueue.size() +
                '}';
    }

    public void startListeners() throws IOException {
        // Listening is already started in the P2PConnection constructor
    }

    public void stopListeners() throws IOException {
        for (Connection connection : connections.values()) {
            connection.close();
        }
    }

    public void addNeighbor(Node neighbor) {
        this.neighbors.add(neighbor);
    }

    public boolean sendMessageTo(Node destination, String message) {
        Connection connection = getConnection(destination.getIp());
        if (connection != null) {
            try {
                Packet p2pPacket = new P2PPacket(
                        message.getBytes(),
                        Packet.MessageType.TEXT,
                        this.ip,
                        this.port,
                        destination.getIp(),
                        destination.getPort());
                connection.send(p2pPacket);
                return true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("No P2P connection exists between " + this.ip +
                    " and " + destination.getIp());
        }
        return false;
    }

    public boolean sendMessageViaMultiHop(Node destination, String message) {
        try {
            // Create a new MultiHopConnection using the Protocol
            Protocol protocol = new DSR(this.nodeId);
            protocol.initializeConnection(this.ip, this.port, destination.getIp(), destination.getPort());
            MultiHopConnection multiHopConnection = new MultiHopConnection(
                    this.ip, this.port,
                    destination.getIp(), destination.getPort(),
                    protocol);

            // Register the MultiHopConnection in the connections map
            String key = destination.getIp() + ":" + destination.getPort(); // Unique key for the connection
            addConnection(key, multiHopConnection);
            // (byte[] data, MessageType type, int sourceNodeId, String sourceIP, int sourcePort,
            // int destinationNodeId, String destinationIP, int destinationPort)
            // Create MultiHopPacket and send using the MultiHopConnection
            MultiHopPacket packet = new MultiHopPacket(
                    message.getBytes(),
                    Packet.MessageType.TEXT,
                    this.nodeId,
                    this.ip,
                    this.port,
                    destination.getNodeId(),
                    destination.getIp(),
                    destination.getPort());
            multiHopConnection.send(packet);

            System.out.println("Multi-hop connection established and message sent between Node " + this.nodeId
                    + " and Node " + destination.getNodeId());
            this.connections.put(key, multiHopConnection);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void close() throws IOException {
        // Close the P2P connection's server sockets
        for (Connection connection : connections.values()) {
            connection.close();
        
        }
    }

    public void addConnection(String key, Connection connection) {
        connections.put(key, connection);
    }

    public String getNodeId() {
        return nodeId;
    }

    // Getter methods for new fields
    public Queue<Packet> getProcessingQueue() {
        return processingQueue;
    }

    public int getTotalDemands() {
        return totalDemands;
    }

    public int getConnectedDemands() {
        return connectedDemands;
    }

    // Method to process demands
    public void processDemand(Node destination, String message) {
        totalDemands++;
        boolean success = sendMessageTo(destination, message);
        if (success) {
            connectedDemands++;
            System.out.println(
                    "P2P connection successful between Node " + nodeId + " and Node " + destination.getNodeId());
        } else {
            System.out.println("P2P connection failed. Trying multi-hop connection.");
            boolean multiHopSuccess = sendMessageViaMultiHop(destination, message);
            if (multiHopSuccess) {
                connectedDemands++;
                System.out.println("Multi-hop connection successful between Node " + nodeId + " and Node "
                        + destination.getNodeId());
            } else {
                System.out.println(
                        "Multi-hop connection failed between Node " + nodeId + " and Node " + destination.getNodeId());
                // Add to processing queue
                Packet blockedPacket = new MultiHopPacket(
                        message.getBytes(),
                        Packet.MessageType.TEXT,
                        this.nodeId,
                        this.ip,
                        this.port,
                        destination.getNodeId(),
                        destination.getIp(),
                        destination.getPort());
                processingQueue.add(blockedPacket);
            }
        }
    }

    public void removeConnection(String key) {
        connections.remove(key);
    }
}
