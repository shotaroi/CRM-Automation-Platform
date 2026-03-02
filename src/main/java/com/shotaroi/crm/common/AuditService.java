package com.shotaroi.crm.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shotaroi.crm.domain.entity.AuditLog;
import com.shotaroi.crm.infrastructure.persistence.AuditLogRepository;
import com.shotaroi.crm.infrastructure.security.AuthenticatedUser;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

@Service
public class AuditService {

    private final AuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;

    public AuditService(AuditLogRepository auditLogRepository, ObjectMapper objectMapper) {
        this.auditLogRepository = auditLogRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(String entityType, UUID entityId, String action, Object before, Object after) {
        AuditLog log = new AuditLog();
        log.setTenantId(TenantContext.getTenantId());
        log.setActorUserId(getCurrentUserId());
        log.setEntityType(entityType);
        log.setEntityId(entityId);
        log.setAction(action);
        log.setBeforeJson(toMap(before));
        log.setAfterJson(toMap(after));
        auditLogRepository.save(log);
    }

    private UUID getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof AuthenticatedUser user) {
            return user.userId();
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> toMap(Object obj) {
        if (obj == null) return null;
        if (obj instanceof Map) return (Map<String, Object>) obj;
        try {
            return objectMapper.convertValue(obj, Map.class);
        } catch (Exception e) {
            return Map.of("_error", "Could not serialize");
        }
    }
}
