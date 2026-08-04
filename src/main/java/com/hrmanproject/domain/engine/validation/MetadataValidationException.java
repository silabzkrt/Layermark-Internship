package com.hrmanproject.domain.engine.validation;

import java.util.Map;

public class MetadataValidationException extends RuntimeException {

    private final Map<String, String> validationErrors;

    public MetadataValidationException(String message, Map<String, String> validationErrors) {
        super(message);
        this.validationErrors = validationErrors;
    }

    public Map<String, String> getValidationErrors() {
        return validationErrors;
    }
}
