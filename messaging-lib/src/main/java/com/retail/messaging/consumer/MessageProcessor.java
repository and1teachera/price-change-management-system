package com.retail.messaging.consumer;

import com.retail.messaging.model.PriceAdjustmentMessage;

/**
 * @author Angel Zlatenov
 */

public interface MessageProcessor {
    void processMessage(PriceAdjustmentMessage message) throws MessageProcessingException;
}