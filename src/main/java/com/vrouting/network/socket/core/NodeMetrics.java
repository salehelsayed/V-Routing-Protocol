package com.vrouting.network.socket.core;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class NodeMetrics {
    private double batteryLevel;
    private double processingCapacity;
    private double networkStrength;
    private double reliability;
    
    public NodeMetrics() {
        this.batteryLevel = 1.0;
        this.processingCapacity = 1.0;
        this.networkStrength = 1.0;
        this.reliability = 1.0;
    }
    
    public byte[] serialize() {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             DataOutputStream dos = new DataOutputStream(baos)) {
            dos.writeDouble(batteryLevel);
            dos.writeDouble(processingCapacity);
            dos.writeDouble(networkStrength);
            dos.writeDouble(reliability);
            return baos.toByteArray();
        } catch (IOException e) {
            return new byte[8];
        }
    }
    
    public static NodeMetrics deserialize(byte[] data) {
        NodeMetrics metrics = new NodeMetrics();
        try (ByteArrayInputStream bais = new ByteArrayInputStream(data);
             DataInputStream dis = new DataInputStream(bais)) {
            metrics.batteryLevel = dis.readDouble();
            metrics.processingCapacity = dis.readDouble();
            metrics.networkStrength = dis.readDouble();
            metrics.reliability = dis.readDouble();
        } catch (IOException e) {
            // Return default metrics on error
        }
        return metrics;
    }
    
    // Getters and setters
    public double getBatteryLevel() {
        return batteryLevel;
    }
    
    public void setBatteryLevel(double batteryLevel) {
        this.batteryLevel = Math.max(0.0, Math.min(1.0, batteryLevel));
    }
    
    public double getProcessingCapacity() {
        return processingCapacity;
    }
    
    public void setProcessingCapacity(double processingCapacity) {
        this.processingCapacity = Math.max(0.0, Math.min(1.0, processingCapacity));
    }
    
    public double getNetworkStrength() {
        return networkStrength;
    }
    
    public void setNetworkStrength(double networkStrength) {
        this.networkStrength = Math.max(0.0, Math.min(1.0, networkStrength));
    }
    
    public double getReliability() {
        return reliability;
    }
    
    public void setReliability(double reliability) {
        this.reliability = Math.max(0.0, Math.min(1.0, reliability));
    }
    
    public double getOverallScore() {
        return (batteryLevel + processingCapacity + networkStrength + reliability) / 4.0;
    }
}
