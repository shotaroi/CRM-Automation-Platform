package com.shotaroi.crm.infrastructure.persistence;

import com.shotaroi.crm.domain.entity.IdempotencyKey;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface IdempotencyKeyRepository extends JpaRepository<IdempotencyKey, UUID> {

    Optional<IdempotencyKey> findByTenantIdAndKeyHash(UUID tenantId, String keyHash);
}
