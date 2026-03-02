package com.shotaroi.crm.api.contact;

import java.time.Instant;
import java.util.UUID;

public record ContactResponse(
        UUID id,
        UUID tenantId,
        UUID accountId,
        String firstName,
        String lastName,
        String email,
        String phone,
        String personalNumber,  // masked for SALES role
        Instant createdAt,
        Instant updatedAt,
        Long version
) {}
