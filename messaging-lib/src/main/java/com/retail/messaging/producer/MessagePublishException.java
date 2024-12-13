package com.retail.messaging.producer;

/**
 * @author Angel Zlatenov
 */

public class MessagePublishException extends RuntimeException {
    private final ErrorCategory category;
    private final String correlationId;

    public enum ErrorCategory {
        CONNECTION_ERROR,
        TIMEOUT_ERROR,
        VALIDATION_ERROR,
        ROUTING_ERROR,
        INTERNAL_ERROR
    }

    public MessagePublishException(String message) {
        this(message, ErrorCategory.INTERNAL_ERROR, null);
    }

    public MessagePublishException(String message, Throwable cause) {
        this(message, ErrorCategory.INTERNAL_ERROR, null, cause);
    }

    public MessagePublishException(String message, ErrorCategory category, String correlationId) {
        super(message);
        this.category = category;
        this.correlationId = correlationId;
    }

    public MessagePublishException(String message, ErrorCategory category,
                                   String correlationId, Throwable cause) {
        super(message, cause);
        this.category = category;
        this.correlationId = correlationId;
    }

    public ErrorCategory getCategory() {
        return category;
    }

    public String getCorrelationId() {
        return correlationId;
    }
}