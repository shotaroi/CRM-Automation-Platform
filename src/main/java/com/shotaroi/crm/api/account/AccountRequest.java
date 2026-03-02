package com.shotaroi.crm.api.account;

import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

public record AccountRequest(
        @NotBlank String name,
        String industry,
        UUID ownerUserId
) {}
