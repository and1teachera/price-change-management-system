package com.retail.messaging.consumer;

import com.retail.messaging.model.PriceAdjustmentMessage;
import com.retail.messaging.model.MessageMetadata;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author Angel Zlatenov
 */
@Component
public class DeadLetterQueueProcessor {
    private static final Logger logger = LoggerFactory.getLogger(DeadLetterQueueProcessor.class);

    private final RabbitTemplate rabbitTemplate;
    private final MeterRegistry meterRegistry;
    private final ScheduledExecutorService scheduledExecutor;
    private final Map<String, FailureRecord> failureRecords;

    @Value("${rabbitmq.queue.pas.dlq}")
    private String pasDlqQueue;

    @Value("${rabbitmq.queue.pad.dlq}")
    private String padDlqQueue;

    @Value("${rabbitmq.exchange.pas}")
    private String pasExchange;

    @Value("${rabbitmq.exchange.pad}")
    private String padExchange;

    @Autowired
    public DeadLetterQueueProcessor(RabbitTemplate rabbitTemplate, MeterRegistry meterRegistry) {
        this.rabbitTemplate = rabbitTemplate;
        this.meterRegistry = meterRegistry;
        this.scheduledExecutor = Executors.newScheduledThreadPool(1);
        this.failureRecords = new ConcurrentHashMap<>();

        setupPeriodicProcessing();
        setupMetrics();
    }

    @RabbitListener(queues = "${rabbitmq.queue.pas.dlq}")
    public void processPasDLQ(Message message) {
        processDeadLetter(message, "PAS", pasExchange);
    }

    @RabbitListener(queues = "${rabbitmq.queue.pad.dlq}")
    public void processPadDLQ(Message message) {
        processDeadLetter(message, "PAD", padExchange);
    }

    private void processDeadLetter(Message message, String type, String exchange) {
        String messageId = message.getMessageProperties().getMessageId();
        logger.info("Processing dead letter message: {} of type: {}", messageId, type);

        meterRegistry.counter("rabbitmq.dlq.messages.received", "type", type).increment();

        try {
            PriceAdjustmentMessage adjustmentMessage = (PriceAdjustmentMessage)
                    rabbitTemplate.getMessageConverter().fromMessage(message);

            // Record failure
            FailureRecord record = failureRecords.computeIfAbsent(messageId,
                    k -> new FailureRecord(adjustmentMessage));
            record.incrementAttempts();

            // Check if message can be reprocessed
            if (shouldAttemptReprocessing(record)) {
                reprocessMessage(adjustmentMessage, exchange, type);
            } else {
                handlePermanentFailure(record);
            }

        } catch (Exception e) {
            logger.error("Error processing dead letter message: {}", messageId, e);
            meterRegistry.counter("rabbitmq.dlq.processing.errors", "type", type).increment();
        }
    }

    private boolean shouldAttemptReprocessing(FailureRecord record) {
        // Check failure count and time since last attempt
        return record.getAttempts() < 3 &&
                record.getTimeSinceLastAttempt().toHours() >= 1;
    }

    private void reprocessMessage(PriceAdjustmentMessage message, String exchange, String type) {
        try {
            // Reset retry count and update metadata
            MessageMetadata metadata = message.getMetadata();
            metadata.setRetryCount(0);
            metadata.setStatus(MessageMetadata.ProcessingStatus.PROCESSING);

            // Republish to original exchange
            rabbitTemplate.convertAndSend(exchange, getRoutingKey(type), message);

            meterRegistry.counter("rabbitmq.dlq.messages.reprocessed", "type", type).increment();
            logger.info("Successfully requeued message: {}", metadata.getMessageId());

        } catch (Exception e) {
            logger.error("Failed to reprocess message: {}", message.getMetadata().getMessageId(), e);
            meterRegistry.counter("rabbitmq.dlq.reprocessing.errors", "type", type).increment();
        }
    }

    private void handlePermanentFailure(FailureRecord record) {
        String messageId = record.getMessage().getMetadata().getMessageId();
        logger.error("Message {} has failed permanent processing after {} attempts",
                messageId, record.getAttempts());

        // Update metadata to reflect permanent failure
        record.getMessage().getMetadata().setStatus(MessageMetadata.ProcessingStatus.DEAD_LETTERED);

        // Record metrics
        meterRegistry.counter("rabbitmq.dlq.messages.permanent_failure").increment();

        // Archive message for analysis
        archiveFailedMessage(record);
    }

    private void setupPeriodicProcessing() {
        scheduledExecutor.scheduleAtFixedRate(() -> {
            try {
                cleanupOldRecords();
            } catch (Exception e) {
                logger.error("Error during periodic DLQ processing", e);
            }
        }, 1, 1, TimeUnit.HOURS);
    }

    private void cleanupOldRecords() {
        Instant threshold = Instant.now().minus(java.time.Duration.ofDays(7));
        failureRecords.entrySet().removeIf(entry ->
                entry.getValue().getLastAttempt().isBefore(threshold));
    }

    private void setupMetrics() {
        meterRegistry.gauge("rabbitmq.dlq.tracked_messages", failureRecords, Map::size);
    }

    private String getRoutingKey(String type) {
        return type.toLowerCase() + ".key";
    }

    private void archiveFailedMessage(FailureRecord record) {
        // Implementation for archiving permanently failed messages
        // This could write to a database or file system
    }

    private static class FailureRecord {
        private final PriceAdjustmentMessage message;
        private int attempts;
        private Instant lastAttempt;

        public FailureRecord(PriceAdjustmentMessage message) {
            this.message = message;
            this.attempts = 0;
            this.lastAttempt = Instant.now();
        }

        public void incrementAttempts() {
            attempts++;
            lastAttempt = Instant.now();
        }

        public int getAttempts() {
            return attempts;
        }

        public Instant getLastAttempt() {
            return lastAttempt;
        }

        public java.time.Duration getTimeSinceLastAttempt() {
            return java.time.Duration.between(lastAttempt, Instant.now());
        }

        public PriceAdjustmentMessage getMessage() {
            return message;
        }
    }
}