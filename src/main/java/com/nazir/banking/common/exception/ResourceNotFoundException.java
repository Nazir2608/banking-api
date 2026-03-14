package com.nazir.banking.common.exception;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }

    public static ResourceNotFoundException of(String entity, String identifier) {
        return new ResourceNotFoundException(entity + " not found: " + identifier);
    }
}
