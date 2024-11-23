package com.vrouting.network.socket.core;

import com.vrouting.network.socket.config.NetworkConfig;
import com.vrouting.network.socket.message.Message;
import com.vrouting.network.socket.message.MessageHandler;
import com.vrouting.network.socket.message.MessageType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimulationNode extends Node {
    private static final Logger logger = LoggerFactory.getLogger(SimulationNode.class);
    
    public SimulationNode(String nodeId, NetworkConfig config) {
        super(nodeId, config);
    }

    @Override
    protected MessageHandler createMessageHandler() {
        return new MessageHandler() {
            @Override
            public Message handleMessage(Message message) {
                logger.debug("Simulation node {} handling message: {}", getNodeId(), message);
                // Echo back the message as acknowledgment
                return new Message(getNodeId(), message.getSourceNodeId(), MessageType.HEARTBEAT_ACK);
            }

            @Override
            public void start() {
                logger.debug("Message handler starting for node {}", getNodeId());
            }

            @Override
            public void stop() {
                logger.debug("Message handler stopping for node {}", getNodeId());
            }
        };
    }

    @Override
    protected void onStart() {
        logger.info("Simulation node {} starting", getNodeId());
    }

    @Override
    protected void onStop() {
        logger.info("Simulation node {} stopping", getNodeId());
    }
}
