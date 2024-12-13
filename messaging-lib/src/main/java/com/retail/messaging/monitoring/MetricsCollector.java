package com.retail.messaging.monitoring;

import com.retail.messaging.consumer.BatchMessageConsumer;
import com.retail.messaging.model.BatchMetrics;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class MetricsCollector {
    private final MeterRegistry registry;
    private final Timer processingTimer;
    private final Timer batchProcessingTimer;

    public MetricsCollector(MeterRegistry registry) {
        this.registry = registry;
        this.processingTimer = Timer.builder("message.processing")
                .description("Message processing time")
                .register(registry);
        this.batchProcessingTimer = Timer.builder("batch.processing")
                .description("Batch processing time")
                .register(registry);
    }

    public void recordMessageProcessing(BatchMessageConsumer.ProcessingType type, long processingTimeNanos) {
        processingTimer.record(processingTimeNanos, TimeUnit.NANOSECONDS);
        registry.counter("messages.processed", "type", type.name().toLowerCase()).increment();
    }

    public void recordBatchProcessing(BatchMessageConsumer.ProcessingType type, BatchMetrics metrics) {
        batchProcessingTimer.record(metrics.getTotalProcessingTime(), TimeUnit.MILLISECONDS);
        registry.gauge("batch.size", metrics.getBatchSize());
        registry.gauge("batch.success.rate", metrics.getSuccessRate());
        registry.counter("batches.processed", "type", type.name().toLowerCase()).increment();
    }
}