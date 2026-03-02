package com.shotaroi.crm.api.contact;

import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

public record ContactRequest(
        UUID accountId,
        @NotBlank String firstName,
        @NotBlank String lastName,
        String email,
        String phone,
        String personalNumber
) {}
