package com.shotaroi.crm.infrastructure.persistence;

import com.shotaroi.crm.domain.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AccountRepository extends JpaRepository<Account, UUID> {

    List<Account> findByTenantIdOrderByCreatedAtDesc(UUID tenantId);

    Optional<Account> findByIdAndTenantId(UUID id, UUID tenantId);
}
