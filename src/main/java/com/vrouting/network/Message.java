package com.vrouting.network;

import java.util.UUID;

public class Message {
    private final String id;
    private final MessageType type;
    private final String content;
    private final long timestamp;

    public Message(MessageType type, String content) {
        this.id = UUID.randomUUID().toString();
        this.type = type;
        this.content = content;
        this.timestamp = System.currentTimeMillis();
    }

    public String getId() {
        return id;
    }

    public MessageType getType() {
        return type;
    }

    public String getContent() {
        return content;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
