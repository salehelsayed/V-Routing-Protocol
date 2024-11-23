package com.vrouting.network.socket.core;

import com.vrouting.network.socket.config.NetworkConfig;
import com.vrouting.network.socket.message.Message;
import com.vrouting.network.socket.message.MessageHandler;

import java.io.*;
import java.net.*;
import java.util.concurrent.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles network communication and socket operations.
 */
public class NetworkInterface {
    private static final Logger logger = LoggerFactory.getLogger(NetworkInterface.class);

    private final NetworkConfig config;
    private final ExecutorService executor;
    private final MessageHandler messageHandler;
    private ServerSocket serverSocket;
    private volatile boolean running;
    
    public NetworkInterface(NetworkConfig config, MessageHandler messageHandler) {
        this.config = config;
        this.messageHandler = messageHandler;
        this.executor = Executors.newFixedThreadPool(config.getMaxConnections());
        this.running = false;
    }
    
    public void start() throws IOException {
        serverSocket = new ServerSocket(config.getPort());
        running = true;
        
        // Start accepting connections
        executor.submit(this::acceptConnections);
    }
    
    private void acceptConnections() {
        while (running) {
            try {
                Socket clientSocket = serverSocket.accept();
                handleConnection(clientSocket);
            } catch (IOException e) {
                if (running) {
                    // TODO: Implement proper error handling
                    e.printStackTrace();
                }
            }
        }
    }
    
    private void handleConnection(Socket socket) {
        executor.submit(() -> {
            try {
                socket.setSoTimeout((int) config.getConnectionTimeout());
                processClientMessages(socket);
            } catch (IOException e) {
                // TODO: Implement proper error handling
                e.printStackTrace();
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    // Ignore
                }
            }
        });
    }
    
    private void processClientMessages(Socket socket) throws IOException {
        // TODO: Implement proper message serialization/deserialization
        try (ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream())) {
            
            while (running) {
                try {
                    Message message = (Message) in.readObject();
                    Message response = messageHandler.handleMessage(message);
                    if (response != null) {
                        out.writeObject(response);
                        out.flush();
                    }
                } catch (ClassNotFoundException e) {
                    // TODO: Implement proper error handling
                    break;
                }
            }
        }
    }
    
    public void sendMessage(String targetAddress, int targetPort, Message message) {
        executor.submit(() -> {
            try (Socket socket = new Socket(targetAddress, targetPort);
                 ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream())) {
                
                socket.setSoTimeout((int) config.getConnectionTimeout());
                out.writeObject(message);
                out.flush();
                
            } catch (IOException e) {
                // TODO: Implement proper error handling and retry logic
                e.printStackTrace();
            }
        });
    }
    
    public void shutdown() {
        running = false;
        executor.shutdown();
        try {
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (Exception e) {
            executor.shutdownNow();
        }
    }
    
    public boolean isRunning() {
        return running;
    }
    
    public int getPort() {
        return serverSocket != null ? serverSocket.getLocalPort() : -1;
    }
}
