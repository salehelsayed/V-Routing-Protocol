package com.vrouting.network.socket;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicLong;

/**
 * TCP implementation of the NetworkSocket interface.
 */
public class TCPNetworkSocket implements NetworkSocket {
    private Socket socket;
    private final ConnectionMetrics metrics;
    private final AtomicLong lastSentTime;
    private final AtomicLong lastReceivedTime;
    private InputStream inputStream;
    private OutputStream outputStream;

    public TCPNetworkSocket() {
        this.metrics = new ConnectionMetrics();
        this.lastSentTime = new AtomicLong(0);
        this.lastReceivedTime = new AtomicLong(0);
    }

    @Override
    public void connect(InetAddress address, int port) throws IOException {
        if (socket != null && !socket.isClosed()) {
            throw new IOException("Socket is already connected");
        }

        socket = new Socket(address, port);
        socket.setTcpNoDelay(true); // Disable Nagle's algorithm for better responsiveness
        socket.setKeepAlive(true);
        
        inputStream = socket.getInputStream();
        outputStream = socket.getOutputStream();
        
        // Initialize metrics
        updateMetrics(0, 0.0);
    }

    @Override
    public void send(byte[] data) throws IOException {
        if (!isConnected()) {
            throw new IOException("Socket is not connected");
        }

        long startTime = System.nanoTime();
        outputStream.write(data);
        outputStream.flush();
        
        long endTime = System.nanoTime();
        lastSentTime.set(System.currentTimeMillis());
        
        // Update metrics with send latency
        updateMetrics((endTime - startTime) / 1_000_000, 0.0); // Convert to milliseconds
    }

    @Override
    public byte[] receive(int maxLength) throws IOException {
        if (!isConnected()) {
            throw new IOException("Socket is not connected");
        }

        byte[] buffer = new byte[maxLength];
        long startTime = System.nanoTime();
        
        int bytesRead = inputStream.read(buffer);
        if (bytesRead == -1) {
            throw new IOException("Connection closed by remote host");
        }
        
        long endTime = System.nanoTime();
        lastReceivedTime.set(System.currentTimeMillis());

        // Update metrics with receive latency
        long receiveLatency = (endTime - startTime) / 1_000_000; // Convert to milliseconds
        updateMetrics(receiveLatency, calculateJitter(receiveLatency));

        // Trim buffer to actual size
        if (bytesRead < maxLength) {
            byte[] trimmedBuffer = new byte[bytesRead];
            System.arraycopy(buffer, 0, trimmedBuffer, 0, bytesRead);
            return trimmedBuffer;
        }
        
        return buffer;
    }

    @Override
    public boolean isConnected() {
        return socket != null && socket.isConnected() && !socket.isClosed();
    }

    @Override
    public ConnectionMetrics getMetrics() {
        return metrics;
    }

    @Override
    public int getLocalPort() {
        return socket != null ? socket.getLocalPort() : -1;
    }

    @Override
    public int getRemotePort() {
        return socket != null ? socket.getPort() : -1;
    }

    @Override
    public InetAddress getRemoteAddress() {
        return socket != null ? socket.getInetAddress() : null;
    }

    @Override
    public void close() throws IOException {
        if (socket != null && !socket.isClosed()) {
            socket.close();
        }
    }

    private void updateMetrics(long latency, double jitter) {
        metrics.updateLatency(latency);
        metrics.updateJitter(jitter);
        
        // Calculate packet loss based on TCP retransmission (if available)
        // This is a simplified version, in practice you might want to use JMX or native socket statistics
        if (socket != null && socket.getInputStream() != null) {
            try {
                int availableBytes = socket.getInputStream().available();
                // Simple heuristic: if there are too many bytes waiting, we might be experiencing packet loss
                double estimatedLoss = availableBytes > 8192 ? 5.0 : 0.0;
                metrics.updatePacketLossRate(estimatedLoss);
            } catch (IOException e) {
                metrics.updatePacketLossRate(100.0); // Assume worst case on error
            }
        }
    }

    private double calculateJitter(long currentLatency) {
        // Simplified jitter calculation based on latency variation
        // In practice, you might want to use a moving average or more sophisticated calculation
        return Math.abs(currentLatency - metrics.getLatency()) * 0.5;
    }
}
