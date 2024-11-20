package com.vrouting.network.socket;

import java.io.IOException;
import java.net.InetAddress;

/**
 * Interface defining the base operations for network sockets in the ad-hoc routing protocol.
 */
public interface NetworkSocket extends AutoCloseable {
    /**
     * Connects to a remote host.
     *
     * @param address The remote host address
     * @param port The remote port
     * @throws IOException If connection fails
     */
    void connect(InetAddress address, int port) throws IOException;

    /**
     * Sends data to the connected host.
     *
     * @param data The data to send
     * @throws IOException If sending fails
     */
    void send(byte[] data) throws IOException;

    /**
     * Receives data from the connected host.
     *
     * @param maxLength Maximum length of data to receive
     * @return The received data
     * @throws IOException If receiving fails
     */
    byte[] receive(int maxLength) throws IOException;

    /**
     * Checks if the socket is currently connected.
     *
     * @return true if connected, false otherwise
     */
    boolean isConnected();

    /**
     * Gets the current connection metrics.
     *
     * @return The connection metrics
     */
    ConnectionMetrics getMetrics();

    /**
     * Gets the local port number.
     *
     * @return The local port
     */
    int getLocalPort();

    /**
     * Gets the remote port number.
     *
     * @return The remote port
     */
    int getRemotePort();

    /**
     * Gets the remote address.
     *
     * @return The remote address
     */
    InetAddress getRemoteAddress();
}
