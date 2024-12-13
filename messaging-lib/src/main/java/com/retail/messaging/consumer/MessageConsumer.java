package com.retail.messaging.consumer;

import com.retail.messaging.model.PriceAdjustmentMessage;

import com.retail.messaging.model.PriceAdjustmentMessage;
/**
 * @author Angel Zlatenov
 */


public interface MessageConsumer {
    /**
     * Processes a received price adjustment message
     *
     * @param message The received price adjustment message
     * @throws MessageProcessingException if processing fails
     */
    void processMessage(PriceAdjustmentMessage message) throws MessageProcessingException;

    /**
     * Handles processing failures for messages
     *
     * @param message The failed message
     * @param exception The exception that occurred during processing
     * @param retryCount Current retry attempt number
     * @return true if the message should be retried, false if it should be sent to DLQ
     */
    boolean handleFailure(PriceAdjustmentMessage message, Exception exception, int retryCount);
}