package com.shotaroi.crm.common;

import java.util.UUID;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String entityType, UUID id) {
        super(entityType + " not found: " + id);
    }
}
