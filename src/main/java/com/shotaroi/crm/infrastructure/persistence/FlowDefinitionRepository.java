package com.shotaroi.crm.infrastructure.persistence;

import com.shotaroi.crm.domain.entity.FlowDefinition;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FlowDefinitionRepository extends JpaRepository<FlowDefinition, UUID> {

    List<FlowDefinition> findByTenantIdAndEventTypeAndActiveTrue(UUID tenantId, String eventType);

    List<FlowDefinition> findByTenantId(UUID tenantId);

    List<FlowDefinition> findByTenantIdAndEventType(UUID tenantId, String eventType);

    Optional<FlowDefinition> findByIdAndTenantId(UUID id, UUID tenantId);
}
