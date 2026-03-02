package com.shotaroi.crm.api.flow;

import jakarta.validation.constraints.NotBlank;

import java.util.Map;

public record FlowRequest(
        @NotBlank String eventType,
        @NotBlank String name,
        Map<String, Object> jsonDefinition
) {}
