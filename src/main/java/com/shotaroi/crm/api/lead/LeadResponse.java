package com.shotaroi.crm.api.lead;

import java.time.Instant;
import java.util.UUID;

public record LeadResponse(
        UUID id,
        UUID tenantId,
        String firstName,
        String lastName,
        String email,
        String phone,
        String status,
        Integer score,
        String source,
        UUID ownerUserId,
        Instant createdAt,
        Instant updatedAt,
        Long version
) {}
