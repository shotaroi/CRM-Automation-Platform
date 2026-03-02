package com.shotaroi.crm.infrastructure.persistence;

import com.shotaroi.crm.domain.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TaskRepository extends JpaRepository<Task, UUID> {

    List<Task> findByTenantIdAndRelatedTypeAndRelatedId(UUID tenantId, String relatedType, UUID relatedId);
}
