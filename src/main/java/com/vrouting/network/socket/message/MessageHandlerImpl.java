package com.vrouting.network.socket.message;

import com.vrouting.network.socket.core.Node;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

/**
 * Implementation of MessageHandler interface.
 */
public class MessageHandlerImpl implements MessageHandler {
    private static final Logger logger = Logger.getLogger(MessageHandlerImpl.class.getName());
    
    private final Node node;
    private final BlockingQueue<Message> messageQueue;
    private final ExecutorService executor;
    private final AtomicBoolean isRunning;
    
    public MessageHandlerImpl(Node node) {
        this.node = node;
        this.messageQueue = new LinkedBlockingQueue<>();
        this.executor = Executors.newSingleThreadExecutor();
        this.isRunning = new AtomicBoolean(false);
    }
    
    @Override
    public void start() {
        if (isRunning.compareAndSet(false, true)) {
            executor.submit(this::processMessages);
            logger.info("Message handler started for node " + node.getNodeId());
        }
    }
    
    @Override
    public void stop() {
        if (isRunning.compareAndSet(true, false)) {
            executor.shutdown();
            try {
                if (!executor.awaitTermination(1, TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
                Thread.currentThread().interrupt();
            }
            logger.info("Message handler stopped for node " + node.getNodeId());
        }
    }
    
    @Override
    public Message handleMessage(Message message) {
        if (isRunning.get()) {
            messageQueue.offer(message);
            return message; // Return the message after offering it to the queue
        }
        return null; // Return null if the handler is not running
    }
    
    private void processMessages() {
        while (isRunning.get()) {
            try {
                Message message = messageQueue.poll(100, TimeUnit.MILLISECONDS);
                if (message != null) {
                    try {
                        node.processMessage(message);
                    } catch (Exception e) {
                        logger.warning("Error processing message: " + e.getMessage());
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
    
    /**
     * Gets the current queue size.
     */
    public int getQueueSize() {
        return messageQueue.size();
    }
    
    /**
     * Checks if the handler is currently running.
     */
    public boolean isRunning() {
        return isRunning.get();
    }
}
