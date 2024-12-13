package com.retail.messaging.error;

import com.retail.messaging.model.MessageMetadata;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
public class RetryStrategy {
    private static final Logger logger = LoggerFactory.getLogger(RetryStrategy.class);
    private final MeterRegistry meterRegistry;
    private final ConcurrentHashMap<String, RetryContext> retryContexts;
    private final ScheduledExecutorService retryExecutor;

    private static final int MAX_RETRY_ATTEMPTS = 3;
    private static final int INITIAL_RETRY_DELAY_MS = 1000;
    private static final double BACKOFF_MULTIPLIER = 2.0;

    @Autowired
    public RetryStrategy(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        this.retryContexts = new ConcurrentHashMap<>();
        this.retryExecutor = Executors.newScheduledThreadPool(2);
        setupMetrics();
        startRetryTracking();
    }

    public boolean shouldRetry(String messageId, Exception exception, MessageMetadata metadata) {
        RetryContext context = retryContexts.computeIfAbsent(messageId,
                id -> new RetryContext(metadata.getRetryCount()));

        if (context.getRetryCount() >= MAX_RETRY_ATTEMPTS) {
            logger.warn("Max retry attempts reached for message: {}", messageId);
            meterRegistry.counter("rabbitmq.retries.exhausted").increment();
            return false;
        }

        if (isRetryableException(exception)) {
            long delay = calculateBackoffDelay(context.getRetryCount());
            context.incrementRetryCount();
            context.setNextRetryTime(Instant.now().plusMillis(delay));

            meterRegistry.counter("rabbitmq.retries.scheduled").increment();
            logger.info("Scheduled retry {} for message {} with delay {}ms",
                    context.getRetryCount(), messageId, delay);

            return true;
        }

        return false;
    }

    private boolean isRetryableException(Exception exception) {
        return !(exception instanceof IllegalArgumentException) &&
                !(exception instanceof IllegalStateException);
    }

    private long calculateBackoffDelay(int retryCount) {
        return (long) (INITIAL_RETRY_DELAY_MS * Math.pow(BACKOFF_MULTIPLIER, retryCount));
    }

    private void setupMetrics() {
        meterRegistry.gauge("rabbitmq.retries.pending", retryContexts, ConcurrentHashMap::size);
    }

    private void startRetryTracking() {
        retryExecutor.scheduleAtFixedRate(() -> {
            Instant now = Instant.now();
            retryContexts.forEach((messageId, context) -> {
                if (context.getNextRetryTime() != null &&
                        context.getNextRetryTime().isBefore(now.plusSeconds(30))) {
                    logger.debug("Retry pending for message: {} at {}",
                            messageId, context.getNextRetryTime());
                }
            });
        }, 0, 1, TimeUnit.MINUTES);

        // Cleanup old retry contexts
        retryExecutor.scheduleAtFixedRate(() -> {
            Instant threshold = Instant.now().minus(Duration.ofHours(24));
            retryContexts.entrySet().removeIf(entry ->
                    entry.getValue().getCreatedAt().isBefore(threshold));
        }, 1, 1, TimeUnit.HOURS);
    }

    private static class RetryContext {
        private final Instant createdAt;
        private int retryCount;
        private Instant nextRetryTime;

        public RetryContext(int initialRetryCount) {
            this.createdAt = Instant.now();
            this.retryCount = initialRetryCount;
        }

        public Instant getCreatedAt() {
            return createdAt;
        }

        public int getRetryCount() {
            return retryCount;
        }

        public void incrementRetryCount() {
            this.retryCount++;
        }

        public Instant getNextRetryTime() {
            return nextRetryTime;
        }

        public void setNextRetryTime(Instant nextRetryTime) {
            this.nextRetryTime = nextRetryTime;
        }
    }
}