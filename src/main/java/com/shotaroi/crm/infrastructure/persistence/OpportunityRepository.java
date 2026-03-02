package com.shotaroi.crm.infrastructure.persistence;

import com.shotaroi.crm.domain.entity.Opportunity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OpportunityRepository extends JpaRepository<Opportunity, UUID> {

    List<Opportunity> findByTenantIdOrderByCreatedAtDesc(UUID tenantId);

    Optional<Opportunity> findByIdAndTenantId(UUID id, UUID tenantId);
}
