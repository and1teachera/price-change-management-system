package com.retail.messaging.consumer;

import com.retail.messaging.model.BatchMetrics;

import java.util.concurrent.CountDownLatch;

/**
 * @author Angel Zlatenov
 */

public class BatchContext {
    private final CountDownLatch completionLatch;
    private final BatchMetrics metrics;

    public BatchContext(int batchSize) {
        this.completionLatch = new CountDownLatch(batchSize);
        this.metrics = new BatchMetrics(batchSize);
    }

    public void recordSuccess(long processingTime) {
        metrics.recordSuccess(processingTime);
    }

    public void recordFailure() {
        metrics.recordFailure();
    }

    public void markProcessed() {
        completionLatch.countDown();
    }

    public void awaitCompletion() {
        try {
            completionLatch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Batch processing interrupted", e);
        }
    }

    public BatchMetrics getMetrics() {
        return metrics;
    }
}
