package com.vrouting.network.socket.routing;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Represents a routing path in the network.
 * Contains path information and associated metrics.
 */
public class RoutingPath {
    private final List<String> path;
    private final Map<String, Double> metrics;
    private final long createdTime;
    private volatile long lastUpdated;
    private volatile int useCount;
    private volatile double score;
    
    /**
     * Creates a new routing path with the specified route and metrics.
     */
    public RoutingPath(List<String> path, Map<String, Double> metrics) {
        this.path = new ArrayList<>(path);
        this.metrics = new ConcurrentHashMap<>(metrics);
        this.createdTime = System.currentTimeMillis();
        this.lastUpdated = this.createdTime;
        this.useCount = 0;
        this.score = calculateBaseScore();
    }
    
    /**
     * Updates path metrics with new values.
     */
    public void updateMetrics(Map<String, Double> newMetrics) {
        metrics.putAll(newMetrics);
        lastUpdated = System.currentTimeMillis();
        score = calculateBaseScore();
    }
    
    /**
     * Increments the use count for this path.
     */
    public void incrementUseCount() {
        useCount++;
        lastUpdated = System.currentTimeMillis();
    }
    
    /**
     * Calculates the weighted score for this path based on metrics and weights.
     */
    public double calculateScore(Map<String, Double> weights) {
        return metrics.entrySet().stream()
            .mapToDouble(e -> e.getValue() * weights.getOrDefault(e.getKey(), 0.0))
            .sum();
    }
    
    /**
     * Calculates the base score for this path using default weights.
     */
    private double calculateBaseScore() {
        double latencyScore = 1.0 / (1.0 + metrics.getOrDefault("latency", 0.0));
        double reliabilityScore = metrics.getOrDefault("reliability", 0.5);
        double bandwidthScore = metrics.getOrDefault("bandwidth", 0.0);
        double hopPenalty = 1.0 / (1.0 + getHopCount());
        
        return (0.4 * latencyScore + 0.3 * reliabilityScore + 0.2 * bandwidthScore + 0.1 * hopPenalty);
    }
    
    /**
     * Gets the current score for this path.
     */
    public double getScore() {
        return score;
    }
    
    /**
     * Checks if this path is still valid based on age and metrics.
     */
    public boolean isValid() {
        long age = System.currentTimeMillis() - lastUpdated;
        return age < 300000; // 5 minutes
    }
    
    // Getters
    public List<String> getPath() {
        return Collections.unmodifiableList(path);
    }
    
    public Map<String, Double> getMetrics() {
        return Collections.unmodifiableMap(metrics);
    }
    
    public long getCreatedTime() {
        return createdTime;
    }
    
    public long getLastUpdated() {
        return lastUpdated;
    }
    
    public int getUseCount() {
        return useCount;
    }
    
    public int getHopCount() {
        return path.size() - 1;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RoutingPath that = (RoutingPath) o;
        return path.equals(that.path);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(path);
    }
    
    @Override
    public String toString() {
        return String.format("RoutingPath{hops=%d, nodes=%s}", getHopCount(), path);
    }
}
