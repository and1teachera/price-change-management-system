package com.retail.messaging.model;

/**
 * @author Angel Zlatenov
 */

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

public class PriceAdjustmentMessage {
    @NotNull
    private String eventId;

    @NotNull
    @Size(min = 1, max = 50)
    private String skuId;

    private String nodeKey;

    @NotNull
    private AdjustmentType adjustmentType;

    private Double adjustmentAmount;

    private Double adjustmentPercentage;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime effectiveDate;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime sourceDate;

    private MessageMetadata metadata;

    public enum AdjustmentType {
        PRICE_ADJ,
        PRICE_ADJ_CANCEL,
        PRICE_RESTORE,
        PRICE_RESTORE_CANCEL
    }

    // Getters and setters
    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getSkuId() {
        return skuId;
    }

    public void setSkuId(String skuId) {
        this.skuId = skuId;
    }

    public String getNodeKey() {
        return nodeKey;
    }

    public void setNodeKey(String nodeKey) {
        this.nodeKey = nodeKey;
    }

    public AdjustmentType getAdjustmentType() {
        return adjustmentType;
    }

    public void setAdjustmentType(AdjustmentType adjustmentType) {
        this.adjustmentType = adjustmentType;
    }

    public Double getAdjustmentAmount() {
        return adjustmentAmount;
    }

    public void setAdjustmentAmount(Double adjustmentAmount) {
        this.adjustmentAmount = adjustmentAmount;
    }

    public Double getAdjustmentPercentage() {
        return adjustmentPercentage;
    }

    public void setAdjustmentPercentage(Double adjustmentPercentage) {
        this.adjustmentPercentage = adjustmentPercentage;
    }

    public LocalDateTime getEffectiveDate() {
        return effectiveDate;
    }

    public void setEffectiveDate(LocalDateTime effectiveDate) {
        this.effectiveDate = effectiveDate;
    }

    public LocalDateTime getSourceDate() {
        return sourceDate;
    }

    public void setSourceDate(LocalDateTime sourceDate) {
        this.sourceDate = sourceDate;
    }

    public MessageMetadata getMetadata() {
        return metadata;
    }

    public void setMetadata(MessageMetadata metadata) {
        this.metadata = metadata;
    }
}
