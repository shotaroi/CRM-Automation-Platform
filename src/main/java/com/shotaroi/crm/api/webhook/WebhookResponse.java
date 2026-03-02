package com.shotaroi.crm.api.webhook;

import java.time.Instant;
import java.util.UUID;

public record WebhookResponse(
        UUID id,
        UUID tenantId,
        String url,
        boolean active,
        Instant createdAt
) {}
