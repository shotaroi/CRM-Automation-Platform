package com.shotaroi.crm.api.opportunity;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record OpportunityResponse(
        UUID id,
        UUID tenantId,
        UUID accountId,
        String name,
        BigDecimal amount,
        String stage,
        LocalDate closeDate,
        UUID ownerUserId,
        Instant createdAt,
        Instant updatedAt,
        Long version
) {}
