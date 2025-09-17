package com.ghostvault.exception;

import java.util.List;
import java.util.ArrayList;

/**
 * Exception for validation errors
 */
public class ValidationException extends GhostVaultException {
    
    public enum ValidationType {
        PASSWORD_STRENGTH("Password strength validation"),
        FILE_FORMAT("File format validation"),
        INPUT_FORMAT("Input format validation"),
        SIZE_LIMIT("Size limit validation"),
        CONTENT_VALIDATION("Content validation"),
        CONFIGURATION("Configuration validation"),
        INTEGRITY_CHECK("Integrity check validation"),
        BUSINESS_RULE("Business rule validation");
        
        private final String description;
        
        ValidationType(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    private final ValidationType validationType;
    private final List<String> validationErrors;
    private final String fieldName;
    private final Object invalidValue;
    
    public ValidationException(String message, ValidationType validationType) {
        super(message, ErrorCategory.VALIDATION, ErrorSeverity.LOW, true);
        this.validationType = validationType;
        this.validationErrors = new ArrayList<>();
        this.fieldName = null;
        this.invalidValue = null;
    }
    
    public ValidationException(String message, ValidationType validationType, String fieldName) {
        super(message, ErrorCategory.VALIDATION, ErrorSeverity.LOW, true);
        this.validationType = validationType;
        this.validationErrors = new ArrayList<>();
        this.fieldName = fieldName;
        this.invalidValue = null;
    }
    
    public ValidationException(String message, ValidationType validationType, String fieldName, Object invalidValue) {
        super(message, ErrorCategory.VALIDATION, ErrorSeverity.LOW, true);
        this.validationType = validationType;
        this.validationErrors = new ArrayList<>();
        this.fieldName = fieldName;
        this.invalidValue = invalidValue;
    }
    
    public ValidationException(List<String> validationErrors, ValidationType validationType) {
        super("Multiple validation errors occurred", ErrorCategory.VALIDATION, ErrorSeverity.LOW, true);
        this.validationType = validationType;
        this.validationErrors = new ArrayList<>(validationErrors);
        this.fieldName = null;
        this.invalidValue = null;
    }
    
    private static String generateUserMessage(ValidationType validationType, String fieldName) {
        String field = fieldName != null ? fieldName : "input";
        
        switch (validationType) {
            case PASSWORD_STRENGTH:
                return "Password does not meet security requirements.";
            case FILE_FORMAT:
                return "Invalid file format. Please select a supported file type.";
            case INPUT_FORMAT:
                return "Invalid " + field + " format. Please check your input.";
            case SIZE_LIMIT:
                return "File size exceeds the maximum allowed limit.";
            case CONTENT_VALIDATION:
                return "Invalid content detected in " + field + ".";
            case CONFIGURATION:
                return "Invalid configuration setting for " + field + ".";
            case INTEGRITY_CHECK:
                return "Data integrity validation failed.";
            case BUSINESS_RULE:
                return "Operation violates business rules.";
            default:
                return "Validation failed for " + field + ".";
        }
    }
    
    public ValidationType getValidationType() {
        return validationType;
    }
    
    public List<String> getValidationErrors() {
        return new ArrayList<>(validationErrors);
    }
    
    public String getFieldName() {
        return fieldName;
    }
    
    public Object getInvalidValue() {
        return invalidValue;
    }
    
    public void addValidationError(String error) {
        validationErrors.add(error);
    }
    
    public boolean hasMultipleErrors() {
        return validationErrors.size() > 1;
    }
    
    @Override
    public String getUserMessage() {
        if (!validationErrors.isEmpty()) {
            if (validationErrors.size() == 1) {
                return validationErrors.get(0);
            } else {
                return "Multiple validation errors occurred. Please check your input.";
            }
        }
        return generateUserMessage(validationType, fieldName);
    }
    
    @Override
    public String getTechnicalDetails() {
        StringBuilder details = new StringBuilder();
        details.append("Validation Type: ").append(validationType.name());
        
        if (fieldName != null) {
            details.append("; Field: ").append(fieldName);
        }
        
        if (invalidValue != null) {
            String valueStr = invalidValue.toString();
            if (valueStr.length() > 50) {
                valueStr = valueStr.substring(0, 47) + "...";
            }
            details.append("; Value: ").append(valueStr);
        }
        
        if (!validationErrors.isEmpty()) {
            details.append("; Errors: ").append(validationErrors.size());
        }
        
        return details.toString();
    }
    
    public String getDetailedErrorMessage() {
        if (validationErrors.isEmpty()) {
            return getUserMessage();
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append("Validation failed");
        if (fieldName != null) {
            sb.append(" for ").append(fieldName);
        }
        sb.append(":\n");
        
        for (int i = 0; i < validationErrors.size(); i++) {
            sb.append("  ").append(i + 1).append(". ").append(validationErrors.get(i)).append("\n");
        }
        
        return sb.toString().trim();
    }
}