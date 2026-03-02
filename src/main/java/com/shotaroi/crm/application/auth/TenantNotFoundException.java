package com.shotaroi.crm.application.auth;

public class TenantNotFoundException extends RuntimeException {
    public TenantNotFoundException(java.util.UUID tenantId) {
        super("Tenant not found: " + tenantId);
    }
}
