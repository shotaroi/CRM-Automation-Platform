package com.shotaroi.crm.api.flow;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record FlowResponse(
        UUID id,
        UUID tenantId,
        String eventType,
        String name,
        boolean active,
        Map<String, Object> jsonDefinition,
        Instant createdAt,
        Instant updatedAt
) {}
