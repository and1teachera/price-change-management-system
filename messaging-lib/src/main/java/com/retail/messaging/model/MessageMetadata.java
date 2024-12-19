package com.retail.messaging.model;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Angel Zlatenov
 */
public class MessageMetadata {
    private String messageId;
    private int retryCount;
    private LocalDateTime timestamp;
    private String sourceRegion;
    private Map<String, String> headers;
    private ProcessingStatus status;

    public MessageMetadata() {
        this.timestamp = LocalDateTime.now();
        this.headers = new HashMap<>();
        this.retryCount = 0;
        this.status = ProcessingStatus.NEW;
    }

    public enum ProcessingStatus {
        NEW,
        PROCESSING,
        COMPLETED,
        FAILED,
        DEAD_LETTERED
    }

    // Getters and setters
    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(final int retryCount) {
        this.retryCount = retryCount;
    }

    public void incrementRetryCount() {
        this.retryCount++;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getSourceRegion() {
        return sourceRegion;
    }

    public void setSourceRegion(String sourceRegion) {
        this.sourceRegion = sourceRegion;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void addHeader(String key, String value) {
        this.headers.put(key, value);
    }

    public ProcessingStatus getStatus() {
        return status;
    }

    public void setStatus(ProcessingStatus status) {
        this.status = status;
    }
}