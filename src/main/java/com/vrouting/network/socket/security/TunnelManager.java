package com.vrouting.network.socket.security;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.crypto.SecretKey;

/**
 * Manages secure tunnels between nodes for encrypted communication.
 */
public class TunnelManager {
    private final SecurityManager securityManager;
    private final Map<String, TunnelState> tunnels;
    
    private static class TunnelState {
        final SecretKey key;
        final long establishedTime;
        volatile long lastUsed;
        
        TunnelState(SecretKey key) {
            this.key = key;
            this.establishedTime = System.currentTimeMillis();
            this.lastUsed = this.establishedTime;
        }
        
        void markUsed() {
            this.lastUsed = System.currentTimeMillis();
        }
        
        boolean isExpired() {
            return System.currentTimeMillis() - lastUsed > 3600000; // 1 hour
        }
    }
    
    public TunnelManager(SecurityManager securityManager) {
        this.securityManager = securityManager;
        this.tunnels = new ConcurrentHashMap<>();
    }
    
    public byte[] sendThroughTunnel(String peerId, byte[] data) {
        // TODO: Implement full tunnel protocol
        // For now, just use basic encryption
        return securityManager.encryptMessage(peerId, data);
    }
    
    public byte[] receiveFromTunnel(String peerId, byte[] encryptedData) {
        // TODO: Implement full tunnel protocol
        // For now, just use basic decryption
        return securityManager.decryptMessage(peerId, encryptedData);
    }
    
    public boolean establishTunnel(String peerId) {
        try {
            // TODO: Implement proper tunnel establishment protocol
            // For now, just create a placeholder tunnel state
            tunnels.put(peerId, new TunnelState(null));
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    public void closeTunnel(String peerId) {
        tunnels.remove(peerId);
        securityManager.removePeer(peerId);
    }
    
    public boolean hasTunnel(String peerId) {
        TunnelState state = tunnels.get(peerId);
        if (state != null && state.isExpired()) {
            closeTunnel(peerId);
            return false;
        }
        return state != null;
    }
    
    public void cleanup() {
        tunnels.entrySet().removeIf(entry -> entry.getValue().isExpired());
    }
}
