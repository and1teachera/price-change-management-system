package com.retail.messaging.producer;

import com.retail.messaging.model.PriceAdjustmentMessage;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;

/**
 * @author Angel Zlatenov
 */



public interface MessagePublisher {

    /**
     * Publishes a price adjustment message to the specified exchange
     *
     * @param message The price adjustment message to publish
     * @param exchange The target exchange
     * @param routingKey The routing key for message delivery
     * @throws MessagePublishException if publishing fails after retries
     */
    @Retryable(
            value = {MessagePublishException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    void publish(PriceAdjustmentMessage message, String exchange, String routingKey) throws MessagePublishException;

    /**
     * Publishes a price adjustment message with high priority
     *
     * @param message The price adjustment message to publish
     * @param exchange The target exchange
     * @param routingKey The routing key for message delivery
     * @param priority Message priority (1-10)
     * @throws MessagePublishException if publishing fails after retries
     */
    @Retryable(
            value = {MessagePublishException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    void publishWithPriority(PriceAdjustmentMessage message, String exchange, String routingKey, int priority)
            throws MessagePublishException;
}