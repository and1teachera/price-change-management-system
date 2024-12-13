package com.retail.messaging.consumer;

/**
 * @author Angel Zlatenov
 */

public class MessageProcessingException extends Exception {
    public MessageProcessingException(String message) {
        super(message);
    }

    public MessageProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}