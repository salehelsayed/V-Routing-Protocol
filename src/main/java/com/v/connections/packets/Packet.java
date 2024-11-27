package com.v.connections.packets;

import java.io.Serializable;

public abstract class Packet implements Serializable {
    private static final long serialVersionUID = 1L;
    protected byte[] data;
    protected MessageType type;
    protected String sourceIP;
    protected int sourcePort;
    protected String destinationIP;
    protected int destinationPort;

    public String getSourceIp() {
        return sourceIP;
    }

    public void setSourceIp(String sourceIP) {
        this.sourceIP = sourceIP;
    }

    public int getSourcePort() {
        return sourcePort;
    }

    public void setSourcePort(int sourcePort) {
        this.sourcePort = sourcePort;
    }

    public String getDestinationIp() {
        return destinationIP;
    }

    public void setDestinationIp(String destinationIP) {
        this.destinationIP = destinationIP;
    }

    public int getDestinationPort() {
        return destinationPort;
    }

    public void setDestinationPort(int destinationPort) {
        this.destinationPort = destinationPort;
    }

    public enum MessageType {
        TEXT,
        IMAGE,
        VIDEO,
        AUDIO,
        TEST_MESSAGE,
        HEARTBEAT_MESSAGE, BROADCAST, RECOVERY, CONNECTION_ESTABLISHED;

        public static boolean isAdhoc(MessageType type) {
            if (type == TEST_MESSAGE || type == HEARTBEAT_MESSAGE) {
                return true;
            }
            return false;

        }
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public MessageType getType() {
        return this.type;
    }

    public String toString() {
        // Start of Selection
        return "IPacket{" +
                "type='" + getType() + '\'' +
                ", data=" + java.util.Arrays.toString(getData()) +
                '}';
    }

}