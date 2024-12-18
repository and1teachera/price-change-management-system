package com.zlatenov.rms.validation;

import com.zlatenov.rms.model.schedule.AdjustmentDate;
import com.zlatenov.rms.model.schedule.PriceAdjustmentSchedule;
import com.zlatenov.rms.model.validation.ValidationResult;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class PriceAdjustmentScheduleValidator {

    public ValidationResult validate(PriceAdjustmentSchedule schedule) {
        ValidationResult result = new ValidationResult();

        if (schedule == null) {
            result.addError("Schedule cannot be null");
            return result;
        }

        // Validate event ID format
        if (!schedule.getEventId().matches("\\d+")) {
            result.addError("Event ID must be numeric");
        }

        // Validate fiscal year
        if (!isValidFiscalYear(schedule.getFiscalYear())) {
            result.addError("Invalid fiscal year format");
        }

        // Validate adjustment dates
        validateAdjustmentDates(schedule.getAdjustmentDates(), result);

        // Validate event type specific rules
        validateEventTypeRules(schedule, result);

        return result;
    }

    private boolean isValidFiscalYear(String fiscalYear) {
        try {
            int year = Integer.parseInt(fiscalYear);
            int currentYear = LocalDateTime.now().getYear();
            return year >= currentYear - 1 && year <= currentYear + 1;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private void validateAdjustmentDates(List<AdjustmentDate> dates, ValidationResult result) {
        if (dates == null || dates.isEmpty()) {
            result.addError("At least one adjustment date is required");
            return;
        }

        LocalDateTime previousDate = null;
        for (AdjustmentDate date : dates) {
            if (previousDate != null && !date.getEffectiveDate().isAfter(previousDate)) {
                result.addError("Adjustment dates must be in chronological order");
                break;
            }
            previousDate = date.getEffectiveDate();
        }
    }

    private void validateEventTypeRules(PriceAdjustmentSchedule schedule, ValidationResult result) {
        switch (schedule.getEventType()) {
            case CMD:
                validateCMDRules(schedule, result);
                break;
            case CTA:
                validateCTARules(schedule, result);
                break;
            // Add other event type validations
        }
    }

    private void validateCMDRules(PriceAdjustmentSchedule schedule, ValidationResult result) {
        // CMD specific validations
        if (schedule.getOutOfSaleDate() == null) {
            result.addError("CMD events require an out of sale date");
        }
    }

    private void validateCTARules(PriceAdjustmentSchedule schedule, ValidationResult result) {
        // CTA specific validations
        if (schedule.getAdjustmentDates().size() > 1) {
            result.addError("CTA events can only have one adjustment date");
        }
    }
}