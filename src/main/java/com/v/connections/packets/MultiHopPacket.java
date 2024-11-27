package com.v.connections.packets;

import java.util.Stack;

public class MultiHopPacket extends Packet {
    
    private boolean forward;
    private Stack<String> pathStack;

    private Stack<String> originalPath;

    private String sourceNodeId;
    private String destinationNodeId;

    public MultiHopPacket(byte[] data, MessageType type, String sourceNodeId, String sourceIP, int sourcePort,
                          String destinationNodeId, String destinationIP, int destinationPort) {
        this.data = data;
        this.type = type;
        this.sourceNodeId = sourceNodeId;
        this.sourceIP = sourceIP;
        this.sourcePort = sourcePort;
        this.destinationNodeId = destinationNodeId;
        this.destinationIP = destinationIP;
        this.destinationPort = destinationPort;
        this.forward = true;
        this.pathStack = new Stack<>();
    }

    public boolean getForward() {
        return forward;
    }

    public void setForward(boolean forward) {
        this.forward = forward;
    }

    public Stack<String> getPathStack() {
        return pathStack;
    }

    public void setPathStack(Stack<String> pathStack) {
        this.pathStack = pathStack;
    }

    public void pushToStack(String hop) {
        pathStack.push(hop);
    }

    public String popFromStack() {
        return pathStack.pop();
    }

    @SuppressWarnings("unchecked")
    public void copyPathBeforeForwardChange() {
        this.originalPath = (Stack<String>) this.pathStack.clone();
    }

    public Stack<String> getOriginalPath() {
        return originalPath;
    }

    public String getDestinationIP() {
        return destinationIP;
    }

    public int getDestinationPort() {
        return destinationPort;
    }

    public String getSourceNodeId() {
        return sourceNodeId;
    }

    public String getDestinationNodeId() {
        return destinationNodeId;
    }

} 