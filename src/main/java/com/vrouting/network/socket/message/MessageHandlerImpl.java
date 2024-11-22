package com.vrouting.network.socket.message;

import com.vrouting.network.Node;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

/**
 * Implementation of the MessageHandler interface for processing network messages.
 */
public class MessageHandlerImpl implements MessageHandler {
    private static final Logger logger = Logger.getLogger(MessageHandlerImpl.class.getName());
    private final Node node;
    private final ExecutorService messageExecutor;
    private final BlockingQueue<Message> messageQueue;
    private final AtomicBoolean isRunning;

    public MessageHandlerImpl(Node node) {
        this.node = node;
        this.messageExecutor = Executors.newSingleThreadExecutor();
        this.messageQueue = new LinkedBlockingQueue<>();
        this.isRunning = new AtomicBoolean(true);
        startMessageProcessor();
    }

    private void startMessageProcessor() {
        messageExecutor.submit(() -> {
            while (isRunning.get()) {
                try {
                    Message message = messageQueue.poll(100, TimeUnit.MILLISECONDS);
                    if (message != null) {
                        processMessageInternal(message);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    logger.warning("Error processing message: " + e.getMessage());
                }
            }
        });
    }

    @Override
    public void processMessage(Message message) {
        if (message != null && isRunning.get()) {
            messageQueue.offer(message);
        }
    }

    private void processMessageInternal(Message message) {
        try {
            switch (message.getType()) {
                case HEARTBEAT:
                    handleHeartbeat(message);
                    break;
                case ROUTING_UPDATE:
                    handleRoutingUpdate(message);
                    break;
                case CLUSTER_UPDATE:
                    handleClusterUpdate(message);
                    break;
                default:
                    logger.warning("Unknown message type: " + message.getType());
            }
        } catch (Exception e) {
            logger.warning("Error processing message: " + e.getMessage());
        }
    }

    private void handleHeartbeat(Message message) {
        if (node.getPeerDirectory() != null) {
            node.getPeerDirectory().updatePeerLastSeen(message.getSourceId());
        }
    }

    private void handleRoutingUpdate(Message message) {
        if (node.getRoutingTable() != null) {
            node.getRoutingTable().updateEntry(message.getRoutingEntry());
        }
    }

    private void handleClusterUpdate(Message message) {
        if (node.getClusterManager() != null) {
            node.getClusterManager().handleClusterUpdate(message);
        }
    }

    @Override
    public void sendMessage(Message message) {
        if (message != null && isRunning.get()) {
            // Logic to send the message
            logger.info("Sending message: " + message);
        }
    }

    @Override
    public Message handleMessage(Message message) {
        // Logic to handle the message
        logger.info("Handling message: " + message);
        return message; // Return the processed message
    }

    @Override
    public void shutdown() {
        isRunning.set(false);
        messageExecutor.shutdown();
        try {
            if (!messageExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                messageExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            messageExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
