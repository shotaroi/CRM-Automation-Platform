package com.shotaroi.crm.infrastructure.persistence;

import com.shotaroi.crm.domain.entity.Lead;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface LeadRepository extends JpaRepository<Lead, UUID> {

    Optional<Lead> findByIdAndTenantId(UUID id, UUID tenantId);

    @Query("SELECT l FROM Lead l WHERE l.tenantId = :tenantId " +
            "AND (:status IS NULL OR l.status = :status) " +
            "AND (:ownerId IS NULL OR l.ownerUserId = :ownerId) " +
            "AND (:q IS NULL OR LOWER(l.firstName) LIKE LOWER(CONCAT('%', :q, '%')) " +
            "OR LOWER(l.lastName) LIKE LOWER(CONCAT('%', :q, '%')) " +
            "OR LOWER(l.email) LIKE LOWER(CONCAT('%', :q, '%')))")
    Page<Lead> findByTenantWithFilters(@Param("tenantId") UUID tenantId,
                                       @Param("status") String status,
                                       @Param("ownerId") UUID ownerId,
                                       @Param("q") String q,
                                       Pageable pageable);
}
