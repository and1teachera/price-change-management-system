package com.zlatenov.rms.model.schedule;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.retail.messaging.model.MessageMetadata;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author Angel Zlatenov
 */

public class PriceAdjustmentSchedule {

    @Getter
    @Setter
    @NotNull
    private String eventId;

    @Getter
    @Setter
    @NotNull
    @Pattern(regexp = "\\d{4}")
    private String fiscalYear;

    @NotNull
    @Size(min = 1, max = 6)
    @Valid
    private List<AdjustmentDate> adjustmentDates;

    @Getter
    @Setter
    @NotNull
    private EventType eventType;

    @Getter
    @Setter
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime outOfSaleDate;

    @Getter
    @Setter
    private MessageMetadata metadata;

    public enum EventType {
        PROM,    // Promotional
        MKDOWN,  // Markdown
        CMD,     // Clearance Markdown
        CTA      // Clearance to Active
    }

    public @NotNull @Size(min = 1, max = 6) @Valid List<AdjustmentDate> getAdjustmentDates() {
        return adjustmentDates;
    }

    public void setAdjustmentDates(
            @NotNull @Size(min = 1, max = 6) @Valid final List<AdjustmentDate> adjustmentDates) {
        this.adjustmentDates = adjustmentDates;
    }
}