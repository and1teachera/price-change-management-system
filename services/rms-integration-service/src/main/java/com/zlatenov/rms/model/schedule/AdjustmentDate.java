package com.zlatenov.rms.model.schedule;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

/**
 * @author Angel Zlatenov
 */

public class AdjustmentDate {
    @NotNull
    private Integer markNumber;

    @NotNull
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime effectiveDate;

    @AssertTrue(message = "Mark dates must be chronologically ordered")
    private boolean isValidSequence() {
        // Implementation for chronological validation
        return true;
    }

    public @NotNull Integer getMarkNumber() {
        return markNumber;
    }

    public void setMarkNumber(@NotNull final Integer markNumber) {
        this.markNumber = markNumber;
    }

    public @NotNull LocalDateTime getEffectiveDate() {
        return effectiveDate;
    }

    public void setEffectiveDate(@NotNull final LocalDateTime effectiveDate) {
        this.effectiveDate = effectiveDate;
    }
}
