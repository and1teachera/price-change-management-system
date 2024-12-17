package com.retail.messaging.consumer;

import com.retail.messaging.config.ConsumerConfig;
import com.retail.messaging.error.ErrorHandler;
import com.retail.messaging.model.BatchMetrics;
import com.retail.messaging.model.PriceAdjustmentMessage;
import com.retail.messaging.monitoring.MetricsCollector;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import lombok.Data;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

/**
 * @author Angel Zlatenov
 */

@Component
@Data
public class BatchMessageConsumer {
    private final MessageProcessor messageProcessor;
    private final ErrorHandler errorHandler;
    private final ExecutorService processingExecutor;
    private final Semaphore concurrencyLimiter;
    private final MetricsCollector metricsCollector;
    private final ConsumerConfig config;

    public BatchMessageConsumer(
            MessageProcessor messageProcessor,
            ErrorHandler errorHandler,
            MetricsCollector metricsCollector,
            ConsumerConfig config) {
        this.messageProcessor = messageProcessor;
        this.errorHandler = errorHandler;
        this.metricsCollector = metricsCollector;
        this.config = config;
        this.processingExecutor = Executors.newFixedThreadPool(config.getConcurrentProcessors());
        this.concurrencyLimiter = new Semaphore(config.getConcurrentProcessors());
    }

    @RabbitListener(
            queues = "${rabbitmq.queue.pas}",
            containerFactory = "batchRabbitListenerContainerFactory",
            batch = "true"
    )
    public void consumePriceAdjustmentScheduleBatch(List<PriceAdjustmentMessage> messages) {
        try {
            processBatch(messages, ProcessingType.SCHEDULE);
        } catch (Exception e) {
            errorHandler.handleBatchError(messages, e);
        }
    }

    @RabbitListener(queues = "${rabbitmq.queue.pad}", containerFactory = "batchRabbitListenerContainerFactory")
    public void consumePriceAdjustmentDirectiveBatch(List<PriceAdjustmentMessage> messages) {
        try {
            processBatch(messages, ProcessingType.DIRECTIVE);
        } catch (Exception e) {
            errorHandler.handleBatchError(messages, e);
        }
    }

    private void processBatch(List<PriceAdjustmentMessage> messages, ProcessingType type) {
        BatchContext context = new BatchContext(messages.size());

        for (PriceAdjustmentMessage message : messages) {
            processingExecutor.submit(() -> processMessageWithContext(message, context, type));
        }

        context.awaitCompletion();
        metricsCollector.recordBatchProcessing(type, context.getMetrics());
    }

    private void processMessageWithContext(PriceAdjustmentMessage message,
                                           BatchContext context,
                                           ProcessingType type) {
        try {
            concurrencyLimiter.acquire();
            long startTime = System.nanoTime();

            messageProcessor.processMessage(message);

            long processingTime = System.nanoTime() - startTime;
            context.recordSuccess(processingTime);
            metricsCollector.recordMessageProcessing(type, processingTime);
        } catch (Exception e) {
            context.recordFailure();
            errorHandler.handleError(message, e);
        } finally {
            concurrencyLimiter.release();
            context.markProcessed();
        }
    }

    public enum ProcessingType {
        SCHEDULE,
        DIRECTIVE
    }
}
