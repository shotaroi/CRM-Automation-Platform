package com.shotaroi.crm.api.opportunity;

import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record OpportunityRequest(
        UUID accountId,
        @NotBlank String name,
        BigDecimal amount,
        String stage,
        LocalDate closeDate,
        UUID ownerUserId
) {}
