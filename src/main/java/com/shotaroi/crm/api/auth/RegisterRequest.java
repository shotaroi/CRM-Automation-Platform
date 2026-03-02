package com.shotaroi.crm.api.auth;

import com.shotaroi.crm.domain.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record RegisterRequest(
        @NotNull UUID tenantId,
        @NotBlank @Email String email,
        @NotBlank String password,
        @NotNull UserRole role
) {}
