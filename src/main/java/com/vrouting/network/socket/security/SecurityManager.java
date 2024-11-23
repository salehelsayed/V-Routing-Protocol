package com.vrouting.network.socket.security;

import java.security.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

/**
 * Manages security operations including key management and message encryption.
 */
public class SecurityManager {
    private final KeyPair nodeKeyPair;
    private final Map<String, PublicKey> peerKeys;
    private final Map<String, SecretKey> sessionKeys;
    
    public SecurityManager() throws NoSuchAlgorithmException {
        this.peerKeys = new ConcurrentHashMap<>();
        this.sessionKeys = new ConcurrentHashMap<>();
        this.nodeKeyPair = generateKeyPair();
    }
    
    private KeyPair generateKeyPair() throws NoSuchAlgorithmException {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        return generator.generateKeyPair();
    }
    
    public void registerPeerKey(String peerId, PublicKey publicKey) {
        peerKeys.put(peerId, publicKey);
    }
    
    public byte[] encryptMessage(String peerId, byte[] message) {
        try {
            SecretKey sessionKey = getOrCreateSessionKey(peerId);
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, sessionKey);
            return cipher.doFinal(message);
        } catch (Exception e) {
            // TODO: Implement proper error handling
            // For now, return the original message
            return message;
        }
    }
    
    public byte[] decryptMessage(String peerId, byte[] encryptedMessage) {
        try {
            SecretKey sessionKey = sessionKeys.get(peerId);
            if (sessionKey == null) {
                // TODO: Implement session key negotiation
                return encryptedMessage;
            }
            
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, sessionKey);
            return cipher.doFinal(encryptedMessage);
        } catch (Exception e) {
            // TODO: Implement proper error handling
            // For now, return the original message
            return encryptedMessage;
        }
    }
    
    private SecretKey getOrCreateSessionKey(String peerId) throws NoSuchAlgorithmException {
        return sessionKeys.computeIfAbsent(peerId, k -> {
            try {
                KeyGenerator keyGen = KeyGenerator.getInstance("AES");
                keyGen.init(256);
                return keyGen.generateKey();
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException("Failed to generate session key", e);
            }
        });
    }
    
    public PublicKey getPublicKey() {
        return nodeKeyPair.getPublic();
    }
    
    public boolean verifySignature(String peerId, byte[] data, byte[] signature) {
        try {
            PublicKey peerKey = peerKeys.get(peerId);
            if (peerKey == null) {
                return false;
            }
            
            Signature sig = Signature.getInstance("SHA256withRSA");
            sig.initVerify(peerKey);
            sig.update(data);
            return sig.verify(signature);
        } catch (Exception e) {
            return false;
        }
    }
    
    public byte[] signData(byte[] data) {
        try {
            Signature sig = Signature.getInstance("SHA256withRSA");
            sig.initSign(nodeKeyPair.getPrivate());
            sig.update(data);
            return sig.sign();
        } catch (Exception e) {
            // TODO: Implement proper error handling
            return new byte[0];
        }
    }
    
    public void removePeer(String peerId) {
        peerKeys.remove(peerId);
        sessionKeys.remove(peerId);
    }
}
