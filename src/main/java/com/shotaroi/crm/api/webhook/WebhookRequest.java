package com.shotaroi.crm.api.webhook;

import jakarta.validation.constraints.NotBlank;

public record WebhookRequest(
        @NotBlank String url,
        String secret
) {}
