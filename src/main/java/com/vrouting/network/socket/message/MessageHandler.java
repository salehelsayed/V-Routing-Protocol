package com.vrouting.network.socket.message;

import com.vrouting.network.socket.core.Node;

/**
 * Interface for handling message processing and dispatching for a node.
 */
public interface MessageHandler {
    /**
     * Starts the message handler.
     */
    void start();

    /**
     * Stops the message handler.
     */
    void stop();

    /**
     * Handles an incoming message.
     * @param message The message to handle
     * @return The processed message
     */
    Message handleMessage(Message message);
}
