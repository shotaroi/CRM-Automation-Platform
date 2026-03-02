package com.shotaroi.crm.api.auth;

import com.shotaroi.crm.domain.UserRole;

import java.util.UUID;

public record AuthResponse(
        String token,
        UUID userId,
        UUID tenantId,
        UserRole role
) {}
