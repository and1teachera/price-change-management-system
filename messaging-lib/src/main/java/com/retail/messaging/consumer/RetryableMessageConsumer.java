package com.retail.messaging.consumer;

import com.retail.messaging.model.PriceAdjustmentMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RetryableMessageConsumer {
    private static final Logger logger = LoggerFactory.getLogger(RetryableMessageConsumer.class);
    private static final int MAX_RETRY_ATTEMPTS = 3;

    private final RabbitTemplate rabbitTemplate;
    private final MessageConsumer messageProcessor;

    @Autowired
    public RetryableMessageConsumer(RabbitTemplate rabbitTemplate, MessageConsumer messageProcessor) {
        this.rabbitTemplate = rabbitTemplate;
        this.messageProcessor = messageProcessor;
    }

    @RabbitListener(queues = "${rabbitmq.queue.pas}")
    public void consumePriceAdjustmentSchedule(Message message) {
        try {
            processWithRetry(message, 0);
        } catch (Exception e) {
            logger.error("Failed to process message after {} attempts", MAX_RETRY_ATTEMPTS, e);
            // Message will be sent to DLQ by RabbitMQ's dead letter configuration
        }
    }

    @RabbitListener(queues = "${rabbitmq.queue.pad}")
    public void consumePriceAdjustmentDirective(Message message) {
        try {
            processWithRetry(message, 0);
        } catch (Exception e) {
            logger.error("Failed to process message after {} attempts", MAX_RETRY_ATTEMPTS, e);
            // Message will be sent to DLQ by RabbitMQ's dead letter configuration
        }
    }

    private void processWithRetry(Message message, int retryCount) throws MessageProcessingException {
        try {
            PriceAdjustmentMessage adjustmentMessage = (PriceAdjustmentMessage)
                    rabbitTemplate.getMessageConverter().fromMessage(message);

            messageProcessor.processMessage(adjustmentMessage);

        } catch (Exception e) {
            if (retryCount < MAX_RETRY_ATTEMPTS &&
                    messageProcessor.handleFailure(
                            (PriceAdjustmentMessage) rabbitTemplate.getMessageConverter().fromMessage(message),
                            e,
                            retryCount
                    )) {
                // Exponential backoff
                try {
                    Thread.sleep((long) Math.pow(2, retryCount) * 1000);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new MessageProcessingException("Processing interrupted during retry delay", ie);
                }
                processWithRetry(message, retryCount + 1);
            } else {
                throw new MessageProcessingException("Message processing failed", e);
            }
        }
    }
}