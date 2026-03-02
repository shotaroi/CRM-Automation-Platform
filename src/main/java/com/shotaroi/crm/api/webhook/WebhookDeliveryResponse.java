package com.shotaroi.crm.api.webhook;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record WebhookDeliveryResponse(
        UUID id,
        UUID tenantId,
        UUID endpointId,
        String eventType,
        Map<String, Object> payload,
        String status,
        int attemptCount,
        Instant nextAttemptAt,
        String idempotencyKey,
        Instant createdAt
) {}
