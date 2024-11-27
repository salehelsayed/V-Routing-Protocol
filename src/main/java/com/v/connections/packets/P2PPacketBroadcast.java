package com.v.connections.packets;

import java.util.Map;

import com.v.connections.P2P.P2PConnection;


public class P2PPacketBroadcast extends Packet {

    public P2PPacketBroadcast(byte[] data,  String sourceIP, int sourcePort) {
        this.sourceIP = sourceIP;
        this.sourcePort = sourcePort;
        this.data = data;
        this.type = MessageType.BROADCAST;
    }
    public void broadcast(Map<String, P2PConnection> neighbors) {

    }

} 