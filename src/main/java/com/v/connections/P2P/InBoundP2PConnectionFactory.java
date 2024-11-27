package com.v.connections.P2P;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;



public class InBoundP2PConnectionFactory extends P2PConnectionFactory {
    private static InBoundP2PConnectionFactory instance;
    private int portStart;
    private int portEnd;

    private InBoundP2PConnectionFactory(int portStart, int portEnd) {
        super(portStart, portEnd);
        this.portStart = portStart;
        this.portEnd = portEnd;
    }
    public int getPortStart() {
        return portStart;
    }
    public int getPortEnd() {
        return portEnd;
    }   
    private void setPortStart(int portStart) {
        this.portStart = portStart;
    }
    private void setPortEnd(int portEnd) {
        this.portEnd = portEnd;
    }
    public static InBoundP2PConnectionFactory getInstance(int portStart, int portEnd) {
        if (instance == null) {
            instance = new InBoundP2PConnectionFactory(portStart, portEnd);
        }
        return instance;
    }

    public InboundP2PConnection[] createInBoundP2PConnections(String sourceIP, int portStart, int portEnd) throws IOException {
        setPortStart(portStart);
        setPortEnd(portEnd);
        String key = generateConnectionKey();

        List<InboundP2PConnection> inboundConnections = new ArrayList<>();

        for (int port = portStart; port <= portEnd; port++) {
            if (isPortAvailable(port)) {
                try {
                    InboundP2PConnection connection = new InboundP2PConnection(sourceIP, port);
                    connections.put(key + ":" + port, connection);
                    inboundConnections.add(connection);
                    System.out.println("Inbound connection initialized on port " + port);
                } catch (IOException e) {
                    System.err.println("Failed to initialize inbound connection on port " + port + ": " + e.getMessage());
                }
            } else {
                System.out.println("Port " + port + " is already in use.");
            }
        }

        if (inboundConnections.isEmpty()) {
            throw new IOException("No available ports found in the range " + portStart + "-" + portEnd);
        }

        return inboundConnections.toArray(new InboundP2PConnection[0]);
    }

    // Helper method to check if a port is available
    private boolean isPortAvailable(int port) {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            serverSocket.setReuseAddress(true);
            return true;
        } catch (IOException e) {
            return false;
        }
    }
}
