package com.zlatenov.rms.model.validation;

import com.retail.messaging.model.MessageMetadata;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Angel Zlatenov
 */

@Getter
@Setter
public class ValidationResult {
    private boolean valid;
    private List<String> errors;
    private MessageMetadata metadata;

    public ValidationResult() {
        this.valid = true;
        this.errors = new ArrayList<>();
    }

    public void addError(String error) {
        this.valid = false;
        this.errors.add(error);
    }

}