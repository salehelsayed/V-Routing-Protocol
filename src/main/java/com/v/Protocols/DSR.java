package com.v.Protocols;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Map;

import com.v.connections.P2P.P2PConnection;
import com.v.connections.packets.Packet;
import com.v.connections.packets.Packet.MessageType;
import com.v.connections.packets.MultiHopPacket;
import com.v.connections.packets.P2PPacket;
import com.v.connections.packets.P2PPacketBroadcast;

public class DSR extends Protocol {

    public DSR(String nodeId) {
        this.setNodeId(nodeId);
    }

    @Override
    public void initializeConnection(String sourceIP, int sourcePort, String destinationIP, int destinationPort)
            throws IOException {
        this.setIp(sourceIP);
        this.setPort(sourcePort);
        // Additional initialization if necessary
    }

    @Override
    public void sendMessage(Packet packet) throws IOException {
        if (packet instanceof MultiHopPacket && MessageType.isAdhoc(packet.getType())) {
            MultiHopPacket multiHopPacket = (MultiHopPacket) packet;

            if (multiHopPacket.getForward()) {
                System.out.println("Node " + getNodeId() + ": Sending Route Request for Node " + multiHopPacket.getDestinationNodeId());

                // Route Discovery Phase
                multiHopPacket.pushToStack(this.getIp());

                // Serialize the packet and use it as payload
                byte[] serializedPacket = serializePacket(multiHopPacket);
                Packet p2pPacket = new P2PPacketBroadcast(
                        serializedPacket,
                        this.getIp(),
                        this.getPort()
                );
                broadcast(p2pPacket, this.getNeighbors());

            } else {
                System.out.println("Node " + getNodeId() + ": Sending Route Reply to Node " + multiHopPacket.getDestinationNodeId());

                // Route Reply Phase
                if (!multiHopPacket.getPathStack().isEmpty()) {
                    String nextHopNodeIp = multiHopPacket.popFromStack();

                    // Find neighbor's connection using nodeId
                    P2PConnection nextHopConnection = findConnectionByNodeIp(nextHopNodeIp);

                    if (nextHopConnection != null) {
                        // Serialize the packet and send it
                        byte[] serializedPacket = serializePacket(multiHopPacket);
                        Packet p2pPacket = new P2PPacket(
                                serializedPacket,
                                multiHopPacket.getType(),
                                this.getIp(),
                                this.getPort(),
                                nextHopConnection.getDestinationIP(),
                                nextHopConnection.getDestinationPort()
                        );
                        nextHopConnection.send(p2pPacket);
                    } else {
                        System.out.println("Node " + getNodeId() + ": Next hop node " + nextHopNodeIp + " not found among neighbors.");
                    }
                } else {
                    System.out.println("Node " + getNodeId() + ": No more hops; packet cannot be forwarded.");
                }
            }
        }
    }

    @Override
    public Packet receiveMessage(Packet receivedPacket) throws IOException {
        if (receivedPacket instanceof P2PPacket) {
            P2PPacket p2pPacket = (P2PPacket) receivedPacket;
            // Deserialize the payload to get the MultiHopPacket
            MultiHopPacket multiHopPacket = null;
            try {
                multiHopPacket = (MultiHopPacket) deserializePacket(p2pPacket.getData());
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                return null;
            }

            if (multiHopPacket.getDestinationNodeId() == this.getNodeId()) {
                System.out.println("Node " + getNodeId() + ": Received MultiHopPacket destined for this node.");
                // Packet reached its destination
                // Stop forwarding, change forward flag to false
                multiHopPacket.setForward(false);

                // Register the route
                registerRoute(multiHopPacket);

                // Optionally process the packet or generate a response
                // Serialize the packet and send it back along the path
                sendMessage(multiHopPacket);
            } else {
                System.out.println("Node " + getNodeId() + ": Forwarding MultiHopPacket towards destination.");
                sendMessage(multiHopPacket);
            }
        } else if (receivedPacket instanceof P2PPacketBroadcast) {
            // Handle broadcast packets similarly
            P2PPacketBroadcast broadcastPacket = (P2PPacketBroadcast) receivedPacket;
            MultiHopPacket multiHopPacket = null;
            try {
                multiHopPacket = (MultiHopPacket) deserializePacket(broadcastPacket.getData());
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                return null;
            }

            if (!multiHopPacket.getPathStack().contains(this.getIp())) {
                // Avoid loops by checking if the node is already in the path
                sendMessage(multiHopPacket);
            }
        }
        return null;
    }

    // Implement the method to register the route or connection
    private void registerRoute(MultiHopPacket packet) {
        // Logic to register the route
        // For example, store the path stack or add connections to the neighbors map

        // Assuming the path is stored in packet.getOriginalPath()
        // You can extract the route and update the node's routing table or connections

        System.out.println("Node " + getNodeId() + ": Registering route from Node " + packet.getSourceNodeId());

        // Update the neighbors map or routing table according to your implementation
    }

    // Helper method to find connection by nodeId
    private P2PConnection findConnectionByNodeIp(String nodeId) {
        for (P2PConnection connection : getNeighbors().values()) {
            if (connection.getDestinationIP() == nodeId) {
                return connection;
            }
        }
        return null;
    }

    // Utility methods for serialization and deserialization
    private byte[] serializePacket(Packet packet) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(bos);
        out.writeObject(packet);
        out.flush();
        return bos.toByteArray();
    }

    private Packet deserializePacket(byte[] data) throws IOException, ClassNotFoundException {
        ByteArrayInputStream bis = new ByteArrayInputStream(data);
        ObjectInputStream in = new ObjectInputStream(bis);
        return (Packet) in.readObject();
    }

    private void broadcast(Packet packet, Map<String, P2PConnection> neighbors) {
        for (P2PConnection neighbor : neighbors.values()) {
            try {
                neighbor.send(packet);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void closeConnection() throws IOException {
        // Implement DSR-specific close logic here
    }

    @Override
    public String getVersion() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getVersion'");
    }
}