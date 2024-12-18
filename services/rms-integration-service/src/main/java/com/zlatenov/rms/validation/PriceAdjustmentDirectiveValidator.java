package com.zlatenov.rms.validation;

import com.zlatenov.rms.model.directive.ActionCode;
import com.zlatenov.rms.model.directive.PriceAdjustmentDirective;
import com.zlatenov.rms.model.validation.ValidationResult;
import org.springframework.stereotype.Component;

@Component
public class PriceAdjustmentDirectiveValidator {

    public ValidationResult validate(PriceAdjustmentDirective directive) {
        ValidationResult result = new ValidationResult();

        if (directive == null) {
            result.addError("Directive cannot be null");
            return result;
        }

        // Validate price adjustment rules
        validatePriceAdjustment(directive, result);

        // Validate location rules
        validateLocation(directive, result);

        // Validate action code rules
        validateActionCode(directive, result);

        return result;
    }

    private void validatePriceAdjustment(PriceAdjustmentDirective directive, ValidationResult result) {
        if (directive.getAdjustmentAmount() != null && directive.getAdjustmentPercentage() != null) {
            result.addError("Cannot specify both adjustment amount and percentage");
        }

        if (directive.getAdjustmentAmount() == null && directive.getAdjustmentPercentage() == null) {
            result.addError("Must specify either adjustment amount or percentage");
        }
    }

    private void validateLocation(PriceAdjustmentDirective directive, ValidationResult result) {
        // Location validation logic based on type
        if (directive.getAdjustmentType() == PriceAdjustmentDirective.AdjustmentType.PRICE_RESTORE) {
            validateRestoreLocation(directive, result);
        }
    }

    private void validateRestoreLocation(PriceAdjustmentDirective directive, ValidationResult result) {
        // Special validation for restore actions
        if ("1".equals(directive.getLocationId())) {
            // This indicates a store-wide restore action
            if (directive.getActionCode() != ActionCode.ADD) {
                result.addError("Store-wide restore actions must use ADD action code");
            }
        }
    }

    private void validateActionCode(PriceAdjustmentDirective directive, ValidationResult result) {
        if (directive.getActionCode() == null) {
            result.addError("Action code is required");
            return;
        }

        // Validate action code combinations
        switch (directive.getAdjustmentType()) {
            case PRICE_ADJ:
            case PRICE_RESTORE:
                if (directive.getActionCode() == ActionCode.DEL) {
                    result.addError("Invalid action code for adjustment type");
                }
                break;
            case PRICE_ADJ_CANCEL:
            case PRICE_RESTORE_CANCEL:
                if (directive.getActionCode() == ActionCode.ADD) {
                    result.addError("Invalid action code for cancellation");
                }
                break;
        }
    }
}