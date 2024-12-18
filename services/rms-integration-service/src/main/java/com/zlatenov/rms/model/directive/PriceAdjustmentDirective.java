package com.zlatenov.rms.model.directive;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.retail.messaging.model.MessageMetadata;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class PriceAdjustmentDirective {

    @NotNull
    private String eventId;

    @NotNull
    @Size(min = 1, max = 50)
    private String skuId;

    @NotNull
    private String locationId;

    @NotNull
    private AdjustmentType adjustmentType;

    @DecimalMin("0.0")
    private Double adjustmentAmount;

    @DecimalMin("0.0")
    @DecimalMax("100.0")
    private Double adjustmentPercentage;

    @NotNull
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime effectiveDate;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime sourceDate;

    @NotNull
    private ActionCode actionCode;

    private MessageMetadata metadata;

    public enum AdjustmentType {
        PRICE_ADJ,
        PRICE_ADJ_CANCEL,
        PRICE_RESTORE,
        PRICE_RESTORE_CANCEL
    }


}