package com.shotaroi.crm.domain.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "audit_log")
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "actor_user_id")
    private UUID actorUserId;

    @Column(name = "entity_type", nullable = false)
    private String entityType;

    @Column(name = "entity_id", nullable = false)
    private UUID entityId;

    @Column(nullable = false)
    private String action;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "before_json")
    private Map<String, Object> beforeJson;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "after_json")
    private Map<String, Object> afterJson;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
    }

    public UUID getId() { return id; }
    public UUID getTenantId() { return tenantId; }
    public void setTenantId(UUID tenantId) { this.tenantId = tenantId; }
    public UUID getActorUserId() { return actorUserId; }
    public void setActorUserId(UUID actorUserId) { this.actorUserId = actorUserId; }
    public String getEntityType() { return entityType; }
    public void setEntityType(String entityType) { this.entityType = entityType; }
    public UUID getEntityId() { return entityId; }
    public void setEntityId(UUID entityId) { this.entityId = entityId; }
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    public Map<String, Object> getBeforeJson() { return beforeJson; }
    public void setBeforeJson(Map<String, Object> beforeJson) { this.beforeJson = beforeJson; }
    public Map<String, Object> getAfterJson() { return afterJson; }
    public void setAfterJson(Map<String, Object> afterJson) { this.afterJson = afterJson; }
    public Instant getCreatedAt() { return createdAt; }
}
