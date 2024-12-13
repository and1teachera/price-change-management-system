package com.retail.messaging.error;

import com.retail.messaging.model.MessageMetadata;
import com.retail.messaging.model.PriceAdjustmentMessage;
import com.retail.messaging.producer.MessagePublishException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author Angel Zlatenov
 */



@Component
public class ErrorHandler {
    private static final Logger logger = LoggerFactory.getLogger(ErrorHandler.class);
    private final ConcurrentHashMap<String, ErrorContext> errorContexts;
    private final ScheduledExecutorService cleanupExecutor;

    public ErrorHandler() {
        this.errorContexts = new ConcurrentHashMap<>();
        this.cleanupExecutor = Executors.newSingleThreadScheduledExecutor();
        scheduleCleanup();
    }

    public void handleBatchError(List<PriceAdjustmentMessage> messages, Exception e) {
        logger.error("Batch processing failed for {} messages", messages.size(), e);
        messages.forEach(message -> handleError(message, e));
    }

    public void handleError(PriceAdjustmentMessage message, Exception exception) {
        MessageMetadata metadata = message.getMetadata();
        ErrorContext context = new ErrorContext(exception, LocalDateTime.now(), metadata.getRetryCount());
        String correlationId = message.getMetadata().getMessageId();
        errorContexts.put(correlationId, context);

        logger.error("Error processing message [{}]: {}", correlationId, exception.getMessage(), exception);

        if (shouldRetry(metadata.getRetryCount(), exception)) {
            metadata.incrementRetryCount();
            metadata.setStatus(MessageMetadata.ProcessingStatus.PROCESSING);
        } else {
            metadata.setStatus(MessageMetadata.ProcessingStatus.DEAD_LETTERED);
            logger.error("Message [{}] exceeded retry attempts. Moving to DLQ", correlationId);
        }
    }

    private boolean shouldRetry(int retryCount, Exception exception) {
        if (retryCount >= 3) {
            return false;
        }

        if (exception instanceof MessagePublishException) {
            MessagePublishException publishException = (MessagePublishException) exception;
            return publishException.getCategory() != MessagePublishException.ErrorCategory.VALIDATION_ERROR;
        }

        return true;
    }

    private void scheduleCleanup() {
        cleanupExecutor.scheduleAtFixedRate(() -> {
            LocalDateTime threshold = LocalDateTime.now().minusHours(24);
            errorContexts.entrySet().removeIf(entry ->
                    entry.getValue().timestamp().isBefore(threshold));
        }, 1, 1, TimeUnit.HOURS);
    }

    private record ErrorContext(Exception exception, LocalDateTime timestamp, int retryCount) {
    }
}
