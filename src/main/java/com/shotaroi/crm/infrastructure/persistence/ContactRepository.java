package com.shotaroi.crm.infrastructure.persistence;

import com.shotaroi.crm.domain.entity.Contact;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ContactRepository extends JpaRepository<Contact, UUID> {

    List<Contact> findByTenantIdOrderByCreatedAtDesc(UUID tenantId);

    List<Contact> findByTenantIdAndAccountId(UUID tenantId, UUID accountId);

    Optional<Contact> findByIdAndTenantId(UUID id, UUID tenantId);
}
