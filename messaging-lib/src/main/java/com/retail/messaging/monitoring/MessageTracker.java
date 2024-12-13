package com.retail.messaging.monitoring;

import com.retail.messaging.model.PriceAdjustmentMessage;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * @author Angel Zlatenov
 */

@Component
public class MessageTracker {
    private static final Logger logger = LoggerFactory.getLogger(MessageTracker.class);
    private final MeterRegistry meterRegistry;
    private final ConcurrentHashMap<String, MessageStats> messageStats;

    public MessageTracker(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        this.messageStats = new ConcurrentHashMap<>();
        setupMetrics();
    }

    public void trackMessage(PriceAdjustmentMessage message, String stage) {
        String messageId = message.getMetadata().getMessageId();
        MessageStats stats = messageStats.computeIfAbsent(messageId,
                id -> new MessageStats(message.getMetadata().getSourceRegion()));

        switch (stage) {
            case "RECEIVED":
                stats.setReceiveTime(System.nanoTime());
                recordMessageReceived(message);
                break;
            case "PROCESSED":
                recordProcessingTime(messageId, stats);
                break;
            case "FAILED":
                recordFailure(message);
                break;
            case "COMPLETED":
                recordCompletion(messageId, stats);
                messageStats.remove(messageId);
                break;
        }
    }

    private void recordMessageReceived(PriceAdjustmentMessage message) {
        meterRegistry.counter("messages.received",
                Arrays.asList(
                        Tag.of("region", message.getMetadata().getSourceRegion()),
                        Tag.of("type", message.getAdjustmentType().toString())
                )).increment();
    }

    private void recordProcessingTime(String messageId, MessageStats stats) {
        long processingTime = System.nanoTime() - stats.getReceiveTime();
        meterRegistry.timer("message.processing.time",
                Arrays.asList(
                        Tag.of("region", stats.getRegion())
                )).record(processingTime, TimeUnit.NANOSECONDS);
    }

    private void recordFailure(PriceAdjustmentMessage message) {
        meterRegistry.counter("messages.failed",
                Arrays.asList(
                        Tag.of("region", message.getMetadata().getSourceRegion()),
                        Tag.of("type", message.getAdjustmentType().toString())
                )).increment();
    }

    private void recordCompletion(String messageId, MessageStats stats) {
        long totalTime = System.nanoTime() - stats.getReceiveTime();
        meterRegistry.timer("message.total.time",
                Arrays.asList(
                        Tag.of("region", stats.getRegion())
                )).record(totalTime, TimeUnit.NANOSECONDS);
    }

    private void setupMetrics() {
        meterRegistry.gauge("messages.in_progress", messageStats, ConcurrentHashMap::size);
    }

    private static class MessageStats {
        private final String region;
        private long receiveTime;

        public MessageStats(String region) {
            this.region = region;
            this.receiveTime = System.nanoTime();
        }

        public String getRegion() {
            return region;
        }

        public long getReceiveTime() {
            return receiveTime;
        }

        public void setReceiveTime(long receiveTime) {
            this.receiveTime = receiveTime;
        }
    }
}