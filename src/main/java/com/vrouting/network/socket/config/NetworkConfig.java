package com.vrouting.network.socket.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Configuration settings for network operations.
 */
public class NetworkConfig {
    private static final Logger logger = LoggerFactory.getLogger(NetworkConfig.class);

    private final int port;
    private final int maxConnections;
    private final int bufferSize;
    private final long connectionTimeout;
    private final long heartbeatInterval;
    private final int maxRetries;
    private final boolean clusterHeadEligible;
    private final double clusterHeadThreshold;
    private final int maxNetworkDepth;
    private final int maxClusterSize;
    
    
    private NetworkConfig(Builder builder) {
        this.port = builder.port;
        this.maxConnections = builder.maxConnections;
        this.bufferSize = builder.bufferSize;
        this.connectionTimeout = builder.connectionTimeout;
        this.heartbeatInterval = builder.heartbeatInterval;
        this.maxRetries = builder.maxRetries;
        this.clusterHeadEligible = builder.clusterHeadEligible;
        this.clusterHeadThreshold = builder.clusterHeadThreshold;
        this.maxNetworkDepth = builder.maxNetworkDepth;
        this.maxClusterSize = builder.maxClusterSize;
        logger.info("NetworkConfig initialized");
    }
    
    public static class Builder {
        private int port = 8080;
        private int maxConnections = 100;
        private int bufferSize = 8192;
        private long connectionTimeout = 30000;
        private long heartbeatInterval = 5000;
        private int maxRetries = 3;
        private boolean clusterHeadEligible = false;
        private double clusterHeadThreshold = 0.7;
        private int maxNetworkDepth = 10;
        private int maxClusterSize = 50;
        
        public Builder port(int port) {
            this.port = port;
            return this;
        }
        
        public Builder maxConnections(int maxConnections) {
            this.maxConnections = maxConnections;
            return this;
        }
        
        public Builder bufferSize(int bufferSize) {
            this.bufferSize = bufferSize;
            return this;
        }
        
        public Builder connectionTimeout(long connectionTimeout) {
            this.connectionTimeout = connectionTimeout;
            return this;
        }
        
        public Builder heartbeatInterval(long heartbeatInterval) {
            this.heartbeatInterval = heartbeatInterval;
            return this;
        }
        
        public Builder maxRetries(int maxRetries) {
            this.maxRetries = maxRetries;
            return this;
        }
        
        public Builder clusterHeadEligible(boolean eligible) {
            this.clusterHeadEligible = eligible;
            return this;
        }
        
        public Builder clusterHeadThreshold(double threshold) {
            this.clusterHeadThreshold = threshold;
            return this;
        }
        
        public Builder maxNetworkDepth(int depth) {
            this.maxNetworkDepth = depth;
            return this;
        }
        
        public Builder maxClusterSize(int size) {
            this.maxClusterSize = size;
            return this;
        }
        
        public NetworkConfig build() {
            return new NetworkConfig(this);
        }
    }
    
    // Getters
    public int getPort() {
        return port;
    }
    
    public int getMaxConnections() {
        return maxConnections;
    }
    
    public int getBufferSize() {
        return bufferSize;
    }
    
    public long getConnectionTimeout() {
        return connectionTimeout;
    }
    
    public long getHeartbeatInterval() {
        return heartbeatInterval;
    }
    
    public int getMaxRetries() {
        return maxRetries;
    }
    
    public boolean isClusterHeadEligible() {
        return clusterHeadEligible;
    }
    
    public double getClusterHeadThreshold() {
        return clusterHeadThreshold;
    }
    
    public int getMaxNetworkDepth() {
        return maxNetworkDepth;
    }
    
    public int getMaxClusterSize() {
        return maxClusterSize;
    }
    
    public static NetworkConfig getDefault() {
        return new Builder().build();
    }

    public void configure() {
        logger.debug("Configuring network settings");
        // ... existing code ...
        logger.info("Network configuration completed");
    }
}
