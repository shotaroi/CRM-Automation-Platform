package com.shotaroi.crm.infrastructure.persistence;

import com.shotaroi.crm.domain.entity.WebhookDelivery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WebhookDeliveryRepository extends JpaRepository<WebhookDelivery, UUID> {

    Page<WebhookDelivery> findByTenantIdAndStatusOrderByCreatedAtDesc(UUID tenantId, String status, Pageable pageable);

    Page<WebhookDelivery> findByTenantIdOrderByCreatedAtDesc(UUID tenantId, Pageable pageable);

    @Query("SELECT wd FROM WebhookDelivery wd WHERE wd.status = 'PENDING' AND (wd.nextAttemptAt IS NULL OR wd.nextAttemptAt <= :now)")
    List<WebhookDelivery> findPendingDeliveries(Instant now);

    Optional<WebhookDelivery> findByEndpointIdAndIdempotencyKey(UUID endpointId, String idempotencyKey);
}
