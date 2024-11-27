package com.v.connections.packets;

import java.util.Stack;

public class RecoveryPacket extends Packet {
    private Stack<String> pathHistory;
    private MultiHopPacket failedPacket;

    public RecoveryPacket(byte[] data, MessageType type, String sourceIP, int sourcePort,
                          String destinationIP, int destinationPort, Stack<String> pathHistory,
                          MultiHopPacket failedPacket) {
        this.data = data;
        this.type = type;
        this.sourceIP = sourceIP;
        this.sourcePort = sourcePort;
        this.destinationIP = destinationIP;
        this.destinationPort = destinationPort;
        this.pathHistory = pathHistory;
        this.failedPacket = failedPacket;
    }

    public Stack<String> getPathHistory() {
        return pathHistory;
    }

    public void setPathHistory(Stack<String> pathHistory) {
        this.pathHistory = pathHistory;
    }

    public MultiHopPacket getFailedPacket() {
        return failedPacket;
    }

    public void setFailedPacket(MultiHopPacket failedPacket) {
        this.failedPacket = failedPacket;
    }
} 