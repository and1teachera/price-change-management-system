package com.retail.messaging.model;

/**
 * @author Angel Zlatenov
 */


public class BatchMetrics {
    private final int batchSize;
    private int successCount;
    private int failureCount;
    private long totalProcessingTime;

    public BatchMetrics(int batchSize) {
        this.batchSize = batchSize;
        this.successCount = 0;
        this.failureCount = 0;
        this.totalProcessingTime = 0;
    }

    public synchronized void recordSuccess(long processingTime) {
        successCount++;
        totalProcessingTime += processingTime;
    }

    public synchronized void recordFailure() {
        failureCount++;
    }

    public int getBatchSize() {
        return batchSize;
    }

    public int getSuccessCount() {
        return successCount;
    }

    public int getFailureCount() {
        return failureCount;
    }

    public long getTotalProcessingTime() {
        return totalProcessingTime;
    }

    public double getSuccessRate() {
        return (double) successCount / batchSize;
    }
}