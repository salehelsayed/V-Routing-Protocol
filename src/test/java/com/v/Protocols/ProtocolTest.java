package com.v.Protocols;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.v.Protocols.DSR;
import com.v.Protocols.Protocol;
import com.v.connections.packets.Packet;

import java.io.IOException;
import java.net.Socket;

import static org.mockito.Mockito.*;

public class ProtocolTest {
    private Protocol mockProtocol;
    private Socket mockSocket;

    // @BeforeEach
    // public void setUp() throws IOException {
    //     mockSocket = Mockito.mock(Socket.class);
    //     mockProtocol = new DSR();
    //     mockProtocol.initializeConnection("127.0.0.1", 8080, "192.168.1.1", 9090);
    //     // Assume DSR has a method to set the socket for testing purposes
       
    // }

    @Test
    public void testSendMessage() throws IOException {
        String message = "Test Message";
        Packet mockPacket = mock(Packet.class);
        mockProtocol.sendMessage(mockPacket);
        // Verify that the mock socket's output stream was used
        verify(mockSocket.getOutputStream(), times(1)).write(any(byte[].class));
    }

    @Test
    public void testReceiveMessage() throws IOException {
        mockProtocol.receiveMessage(mock(Packet.class));
    }

    // ... additional tests ...
} 