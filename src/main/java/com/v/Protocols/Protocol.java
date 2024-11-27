package com.v.Protocols;

import java.io.IOException;

import java.util.Map;
import com.v.connections.P2P.P2PConnection;
import com.v.connections.packets.Packet;

public abstract class Protocol {
    
    private  String ip = "127.0.0.1";
    private  int port = 8080;
    private String nodeId;
    private  Map<String, P2PConnection> neighbors;


    public abstract String getVersion();

    public abstract void sendMessage(Packet message) throws IOException;

    public abstract Packet receiveMessage(Packet expectedPacket) throws IOException;

    public abstract void closeConnection() throws IOException;

    public abstract void initializeConnection(String sourceIP, int sourcePort, String destinationIP, int destinationPort)
            throws IOException;

    public String getIp() {
        return ip;
    }

    public int getPort() {
        return port;
    }

    public Map<String, P2PConnection> getNeighbors() {
        return neighbors;
    }

    public void setNeighbors(Map<String, P2PConnection> neighbors) {
        this.neighbors = neighbors;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId2) {
        this.nodeId = nodeId2;
    }

    public void addNeighbor(String ip, P2PConnection connection) {
        this.neighbors.put(ip, connection);
    }

    public void removeNeighbor(String ip) {
        this.neighbors.remove(ip);
    }
   
}
