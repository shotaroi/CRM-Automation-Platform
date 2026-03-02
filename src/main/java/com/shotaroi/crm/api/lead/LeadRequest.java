package com.shotaroi.crm.api.lead;

import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

public record LeadRequest(
        @NotBlank String firstName,
        @NotBlank String lastName,
        String email,
        String phone,
        String status,
        Integer score,
        String source,
        UUID ownerUserId
) {}
