package com.shotaroi.crm.api.account;

import java.time.Instant;
import java.util.UUID;

public record AccountResponse(
        UUID id,
        UUID tenantId,
        String name,
        String industry,
        UUID ownerUserId,
        Instant createdAt,
        Instant updatedAt,
        Long version
) {}
