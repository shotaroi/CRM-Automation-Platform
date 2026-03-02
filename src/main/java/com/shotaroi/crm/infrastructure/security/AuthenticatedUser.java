package com.shotaroi.crm.infrastructure.security;

import com.shotaroi.crm.domain.UserRole;

import java.util.UUID;

public record AuthenticatedUser(UUID userId, UUID tenantId, UserRole role) {
}
