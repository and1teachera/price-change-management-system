package com.retail.messaging.producer;

import com.retail.messaging.model.PriceAdjustmentMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeoutException;

@Component
public class RetryableMessagePublisher implements MessagePublisher {
    private static final Logger logger = LoggerFactory.getLogger(RetryableMessagePublisher.class);
    private static final int DEFAULT_TIMEOUT_MS = 5000;

    private final RabbitTemplate rabbitTemplate;
    private final ConcurrentHashMap<String, CorrelationMetadata> pendingConfirms;
    private final Jackson2JsonMessageConverter messageConverter;

    @Autowired
    public RetryableMessagePublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
        this.pendingConfirms = new ConcurrentHashMap<>();
        this.messageConverter = new Jackson2JsonMessageConverter();

        setupCallbacks();
    }

    private void setupCallbacks() {
        rabbitTemplate.setConfirmCallback((correlation, ack, reason) -> {
            CorrelationMetadata metadata = pendingConfirms.remove(correlation);
            if (metadata != null) {
                if (!ack) {
                    logger.error("Message {} was not confirmed. Reason: {}", correlation, reason);
                    metadata.setFailure(new MessagePublishException(
                            String.format("Message publish not confirmed: %s", reason)
                    ));
                } else {
                    metadata.setSuccess();
                }
            }
        });

        rabbitTemplate.setReturnsCallback(returned -> {
            logger.error("Message returned: {} - {}",
                    returned.getReplyText(),
                    returned.getMessage().getMessageProperties().getMessageId());

            String correlationId = returned.getMessage().getMessageProperties().getCorrelationId();
            CorrelationMetadata metadata = pendingConfirms.remove(correlationId);
            if (metadata != null) {
                metadata.setFailure(new MessagePublishException(
                        String.format("Message returned: %s", returned.getReplyText())
                ));
            }
        });
    }

    @Override
    public void publish(PriceAdjustmentMessage message, String exchange, String routingKey)
            throws MessagePublishException {
        publishInternal(message, exchange, routingKey, 0);
    }

    @Override
    public void publishWithPriority(PriceAdjustmentMessage message, String exchange,
                                    String routingKey, int priority) throws MessagePublishException {
        if (priority < 1 || priority > 10) {
            throw new IllegalArgumentException("Priority must be between 1 and 10");
        }
        publishInternal(message, exchange, routingKey, priority);
    }

    private void publishInternal(PriceAdjustmentMessage message, String exchange,
                                 String routingKey, int priority) throws MessagePublishException {
        String correlationId = UUID.randomUUID().toString();
        CorrelationMetadata metadata = new CorrelationMetadata();
        pendingConfirms.put(correlationId, metadata);

        MessageProperties properties = new MessageProperties();
        properties.setCorrelationId(correlationId);
        if (priority > 0) {
            properties.setPriority(priority);
        }

        Message amqpMessage = messageConverter.toMessage(message, properties);

        try {
            rabbitTemplate.send(exchange, routingKey, amqpMessage);
            metadata.await(DEFAULT_TIMEOUT_MS);

            if (metadata.getException() != null) {
                throw metadata.getException();
            }
        } catch (TimeoutException e) {
            pendingConfirms.remove(correlationId);
            throw new MessagePublishException("Message publish confirmation timed out", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            pendingConfirms.remove(correlationId);
            throw new MessagePublishException("Message publish interrupted", e);
        }
    }

    private static class CorrelationMetadata {
        private boolean completed = false;
        private MessagePublishException exception;

        synchronized void setSuccess() {
            completed = true;
            notifyAll();
        }

        synchronized void setFailure(MessagePublishException exception) {
            this.exception = exception;
            completed = true;
            notifyAll();
        }

        synchronized void await(long timeoutMs) throws TimeoutException, InterruptedException {
            long startTime = System.currentTimeMillis();
            while (!completed) {
                long timeLeft = timeoutMs - (System.currentTimeMillis() - startTime);
                if (timeLeft <= 0) {
                    throw new TimeoutException("Publish confirmation timed out");
                }
                wait(timeLeft);
            }
        }

        MessagePublishException getException() {
            return exception;
        }
    }
}