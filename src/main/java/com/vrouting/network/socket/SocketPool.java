package com.vrouting.network.socket;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Manages a pool of network sockets for efficient connection reuse and monitoring.
 */
public class SocketPool implements AutoCloseable {
    private final Map<String, NetworkSocket> activeSockets;
    private final ScheduledExecutorService healthMonitor;
    private static final long HEALTH_CHECK_INTERVAL = 5000; // 5 seconds

    public SocketPool() {
        this.activeSockets = new ConcurrentHashMap<>();
        this.healthMonitor = Executors.newSingleThreadScheduledExecutor();
        startHealthMonitoring();
    }

    /**
     * Gets or creates a socket connection to the specified address and port.
     *
     * @param address The remote address
     * @param port The remote port
     * @return A NetworkSocket instance
     * @throws IOException If connection fails
     */
    public NetworkSocket getSocket(InetAddress address, int port) throws IOException {
        String key = createKey(address, port);
        
        return activeSockets.computeIfAbsent(key, k -> {
            try {
                NetworkSocket socket = new TCPNetworkSocket();
                socket.connect(address, port);
                return socket;
            } catch (IOException e) {
                throw new RuntimeException("Failed to create socket connection", e);
            }
        });
    }

    /**
     * Releases a socket connection.
     *
     * @param address The remote address
     * @param port The remote port
     */
    public void releaseSocket(InetAddress address, int port) {
        String key = createKey(address, port);
        NetworkSocket socket = activeSockets.remove(key);
        
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException e) {
                // Log error but continue
                System.err.println("Error closing socket: " + e.getMessage());
            }
        }
    }

    /**
     * Gets the current number of active connections.
     *
     * @return The number of active connections
     */
    public int getActiveConnectionCount() {
        return activeSockets.size();
    }

    @Override
    public void close() {
        healthMonitor.shutdown();
        activeSockets.values().forEach(socket -> {
            try {
                socket.close();
            } catch (IOException e) {
                // Log error but continue
                System.err.println("Error closing socket: " + e.getMessage());
            }
        });
        activeSockets.clear();
    }

    private void startHealthMonitoring() {
        healthMonitor.scheduleAtFixedRate(() -> {
            activeSockets.forEach((key, socket) -> {
                if (!socket.isConnected() || !socket.getMetrics().isHealthy()) {
                    // Remove and close unhealthy connections
                    releaseSocket(socket.getRemoteAddress(), socket.getRemotePort());
                }
            });
        }, HEALTH_CHECK_INTERVAL, HEALTH_CHECK_INTERVAL, TimeUnit.MILLISECONDS);
    }

    private String createKey(InetAddress address, int port) {
        return address.getHostAddress() + ":" + port;
    }
}
