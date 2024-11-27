package com.v.connections.P2P;

import java.io.IOException;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import com.v.connections.ConnectionFactory;

public class P2PConnectionFactory extends ConnectionFactory {
    private static P2PConnectionFactory instance;
    private int portStart;
    private int portEnd;
    // Map to store connections with key as "srcIP:destinationIP"
    protected Map<String, P2PConnection> connections = new ConcurrentHashMap<>();

    // Method to create the key
    protected String generateConnectionKey() {
        return UUID.randomUUID().toString();
    }

    // Common method to get a connection
    public P2PConnection getConnection(String srcIP, String destinationIP) {
        String key = generateConnectionKey();
        return connections.get(key);
    }

    // Common method to remove a connection
    public void removeConnection(String key) {
        connections.remove(key);
    }

    
    protected P2PConnectionFactory(int portStart, int portEnd) {
        super(portStart, portEnd);
        this.portStart = portStart;
        this.portEnd = portEnd;
    }

    public P2PConnectionFactory(String sourceIP, int sourcePort, String destinationIP, int portStart2, int portEnd2) {
        super(portStart2, portEnd2);
    }

    public static synchronized P2PConnectionFactory getInstance(int portStart, int portEnd) {
        if (instance == null) {
            instance = new P2PConnectionFactory(portStart, portEnd);
        }
        return instance;
    }

    // Method to create inbound P2P connection
    public InboundP2PConnection[] createInboundP2PConnections(String sourceIP) throws IOException {
        return InBoundP2PConnectionFactory.getInstance(portStart, portEnd)
                .createInBoundP2PConnections(sourceIP, portStart, portEnd);
    }

    // Method to create outbound P2P connection
    public OutboundP2PConnection createOutboundP2PConnection(String sourceIP, int sourcePort, String destinationIP)
            throws IOException {
        return OutBoundP2PConnectionFactory.getInstance(sourceIP, sourcePort, destinationIP, portStart, portEnd)
                .createOutBoundP2PConnection(sourceIP, sourcePort, destinationIP, portStart, portEnd);
    }
} 