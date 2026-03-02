package com.shotaroi.crm.infrastructure.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "crm.jwt")
public record JwtProperties(
        String secret,
        long expirationMs
) {
    public JwtProperties {
        if (secret == null || secret.isBlank()) {
            throw new IllegalArgumentException("crm.jwt.secret is required");
        }
    }
}
