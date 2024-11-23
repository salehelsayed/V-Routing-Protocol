package com.vrouting.network.socket.cluster;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Maintains metrics for cluster performance and health monitoring.
 */
public class ClusterMetrics {
    private final AtomicInteger memberCount;
    private final AtomicLong avgResponseTime;
    private final AtomicInteger messageCount;
    private final AtomicLong lastUpdateTime;
    
    public ClusterMetrics() {
        this.memberCount = new AtomicInteger(0);
        this.avgResponseTime = new AtomicLong(0);
        this.messageCount = new AtomicInteger(0);
        this.lastUpdateTime = new AtomicLong(System.currentTimeMillis());
    }
    
    public void updateMemberCount(int count) {
        memberCount.set(count);
        lastUpdateTime.set(System.currentTimeMillis());
    }
    
    public void updateResponseTime(long newResponseTime) {
        // TODO: Implement exponential moving average for response time
        long currentAvg = avgResponseTime.get();
        long newAvg = (currentAvg == 0) ? newResponseTime : (currentAvg + newResponseTime) / 2;
        avgResponseTime.set(newAvg);
        lastUpdateTime.set(System.currentTimeMillis());
    }
    
    public void incrementMessageCount() {
        messageCount.incrementAndGet();
        lastUpdateTime.set(System.currentTimeMillis());
    }
    
    public int getMemberCount() {
        return memberCount.get();
    }
    
    public long getAvgResponseTime() {
        return avgResponseTime.get();
    }
    
    public int getMessageCount() {
        return messageCount.get();
    }
    
    public long getLastUpdateTime() {
        return lastUpdateTime.get();
    }
    
    public void reset() {
        memberCount.set(0);
        avgResponseTime.set(0);
        messageCount.set(0);
        lastUpdateTime.set(System.currentTimeMillis());
    }
}
