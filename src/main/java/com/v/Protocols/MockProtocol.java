package com.v.Protocols;

import java.io.IOException;
import java.net.Socket;
import org.mockito.Mockito;

import com.v.connections.packets.Packet;


public class MockProtocol extends Protocol {
    @SuppressWarnings("unused")
    private Socket mockSocket;

    public MockProtocol() {
        // Create a mock Socket using Mockito
        this.mockSocket = Mockito.mock(Socket.class);
        // Set up mock behaviors as needed
    }

    @Override
    public void initializeConnection(String sourceIP, int sourcePort, String destinationIP, int destinationPort) throws IOException {
        // Use the mock socket for connection initialization
        // ... existing code ...
    }

    @Override
    public String getVersion() {
        return "MockProtocol 1.0";
    }

    @Override
    public void sendMessage(Packet message) throws IOException {
        // Mock send logic using mockSocket
        // ... existing code ...
    }

    
    @Override
    public void closeConnection() throws IOException {
        // Mock close logic
        // ... existing code ...
    }


    @Override
    public Packet receiveMessage(Packet expectedPacket) throws IOException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'receiveMessage'");
    }
} 