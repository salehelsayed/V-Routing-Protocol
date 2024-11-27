package com.v.connections.packets;

public class P2PPacket extends Packet {

    public P2PPacket(byte[] data, MessageType type, String sourceIP, int sourcePort, String destinationIP, int destinationPort) {
        this.sourceIP = sourceIP;
        this.sourcePort = sourcePort;
        this.destinationIP = destinationIP;
        this.destinationPort = destinationPort;
        this.data = data;
        this.type = type;
    }


    @Override
    public MessageType getType() {
        return MessageType.valueOf(this.type.toString());
    }
    @Override
    public byte[] getData() {
        return data;
    }


} 