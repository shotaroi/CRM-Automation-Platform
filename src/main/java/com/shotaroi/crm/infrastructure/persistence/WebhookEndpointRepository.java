package com.shotaroi.crm.infrastructure.persistence;

import com.shotaroi.crm.domain.entity.WebhookEndpoint;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WebhookEndpointRepository extends JpaRepository<WebhookEndpoint, UUID> {

    List<WebhookEndpoint> findByTenantId(UUID tenantId);

    List<WebhookEndpoint> findByTenantIdAndActiveTrue(UUID tenantId);

    Optional<WebhookEndpoint> findByIdAndTenantId(UUID id, UUID tenantId);
}
