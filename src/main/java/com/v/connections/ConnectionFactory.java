package com.v.connections;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import com.v.connections.P2P.InBoundP2PConnectionFactory;
import com.v.connections.P2P.InboundP2PConnection;
import com.v.connections.P2P.OutBoundP2PConnectionFactory;
import com.v.connections.P2P.OutboundP2PConnection;
import com.v.connections.adhoc.MultiHopConnection;
import com.v.Protocols.Protocol;
import com.v.Protocols.DSR;



public class ConnectionFactory {

    private static int portEnd;
    private static int portStart;

    protected Map<String, Connection> connections = new ConcurrentHashMap<>();

    private static String sourceIP;
    private static int sourcePort;
    private static String destinationIP;    

    private Map<String, InBoundP2PConnectionFactory> inboundFactories = new ConcurrentHashMap<>();
    private Map<String, OutBoundP2PConnectionFactory> outboundFactories = new ConcurrentHashMap<>();

    // Map to store ConnectionFactory instances by node ID
    private static Map<String, ConnectionFactory> instances = new ConcurrentHashMap<>();

    protected ConnectionFactory(int portStart, int portEnd) {
        // Initialization if necessary
    }

    public static synchronized ConnectionFactory getInstance(String nodeId, int portStart, int portEnd) {
        if (!instances.containsKey(nodeId)) {
            instances.put(nodeId, new ConnectionFactory(portStart, portEnd));
        }
        return instances.get(nodeId);
    }

    // Method to create the key
    protected String generateConnectionKey() {
        return UUID.randomUUID().toString();
    }

    // Common method to get a connection
    public Connection getConnection(String srcIP, String destinationIP) {
        String key = generateConnectionKey();
        return connections.get(key);
    }

    // Common method to remove a connection
    public void removeConnection(String srcIP, String destinationIP) {
        String key = generateConnectionKey();
        connections.remove(key);
    }

    // Getter for all connections
    public List<Connection> getAllConnections() {
        return new ArrayList<>(connections.values());
    }

    // Setter for all connections - use with caution
    public void setAllConnections(Map<String, Connection> newConnections) {
        connections.clear();
        connections.putAll(newConnections);
    }



    // Method to create an inbound P2P connection with existence check
    public Map<String, Connection> createInboundConnections(String nodeId) throws IOException {
        Map<String, Connection> inboundConnectionsMap = new HashMap<>();
        InBoundP2PConnectionFactory factory = getInboundFactory(nodeId);
        InboundP2PConnection[] newConnections = factory.createInBoundP2PConnections(sourceIP, this.getPortStart(), this.getPortEnd());
        
        for (InboundP2PConnection connection : newConnections) {
            String key = connection.getDestinationIP() + ":" + connection.getDestinationPort();
            inboundConnectionsMap.put(key, connection);
        }
        
        return inboundConnectionsMap;
    }

    // Method to create an outbound P2P connection with existence check
    public Connection createOutboundConnection(String nodeId, String sourceIP, int sourcePort, String destinationIP) throws IOException {
        String key = generateConnectionKey();
        // Check if the connection already exists
        Connection existingConnection = connections.get(key);
        if (existingConnection != null) {
            return existingConnection;
        }
        try {
            // Try to create a new Outbound P2P Connection
            OutBoundP2PConnectionFactory factory = getOutboundFactory(nodeId);
            OutboundP2PConnection connection = factory.createOutBoundP2PConnection(
                sourceIP, sourcePort, destinationIP, this.getPortStart(), this.getPortEnd()
            ); 
            connections.put(key, connection);
            return connection;
        } catch (IOException e) {
            // If P2P Connection fails, create a MultiHopConnection
            System.out.println("Outbound P2P Connection failed, initiating MultiHopConnection.");
            Protocol protocol = new DSR((nodeId)); // Assuming DSR protocol
            MultiHopConnection connection = new MultiHopConnection(sourceIP, sourcePort, destinationIP, -1, protocol);
            connections.put(key, connection);
            return connection;
        }
    }

    public Map<String, Connection> getConnections() {
        return connections;
    }   


    public int getPortStart() {
        return portStart;
    }

    public int getPortEnd() {
        return portEnd;
    }

    // Method to get or create an inbound factory for a given nodeId
    private InBoundP2PConnectionFactory getInboundFactory(String nodeId) {
        return inboundFactories.computeIfAbsent(nodeId, id -> InBoundP2PConnectionFactory.getInstance(portStart, portEnd));
    }

    // Method to get or create an outbound factory for a given nodeId
    private OutBoundP2PConnectionFactory getOutboundFactory(String nodeId) {
        return outboundFactories.computeIfAbsent(nodeId, id -> OutBoundP2PConnectionFactory.getInstance(sourceIP, sourcePort, destinationIP, portStart, portEnd));
    }
} 
