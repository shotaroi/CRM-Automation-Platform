package com.shotaroi.crm.application.lead;

import com.shotaroi.crm.application.webhook.WebhookEventPublisher;
import com.shotaroi.crm.automation.FlowEngine;
import com.shotaroi.crm.automation.FlowEventType;
import com.shotaroi.crm.automation.TriggerContext;
import com.shotaroi.crm.automation.TriggerEventType;
import com.shotaroi.crm.automation.TriggerRegistry;
import com.shotaroi.crm.common.ResourceNotFoundException;
import com.shotaroi.crm.common.TenantContext;
import com.shotaroi.crm.domain.entity.IdempotencyKey;
import com.shotaroi.crm.domain.entity.Lead;
import com.shotaroi.crm.infrastructure.persistence.IdempotencyKeyRepository;
import com.shotaroi.crm.infrastructure.persistence.LeadRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class LeadService {

    private static final String ENTITY_TYPE_LEAD = "Lead";
    private static final int IDEMPOTENCY_TTL_HOURS = 24;

    private final LeadRepository leadRepository;
    private final IdempotencyKeyRepository idempotencyKeyRepository;
    private final TriggerRegistry triggerRegistry;
    private final FlowEngine flowEngine;
    private final WebhookEventPublisher webhookEventPublisher;

    public LeadService(LeadRepository leadRepository,
                       IdempotencyKeyRepository idempotencyKeyRepository,
                       TriggerRegistry triggerRegistry,
                       FlowEngine flowEngine,
                       WebhookEventPublisher webhookEventPublisher) {
        this.leadRepository = leadRepository;
        this.idempotencyKeyRepository = idempotencyKeyRepository;
        this.triggerRegistry = triggerRegistry;
        this.flowEngine = flowEngine;
        this.webhookEventPublisher = webhookEventPublisher;
    }

    @Transactional(readOnly = true)
    public Page<Lead> list(String status, UUID ownerId, String q, Pageable pageable) {
        UUID tenantId = TenantContext.getTenantId();
        return leadRepository.findByTenantWithFilters(tenantId, status, ownerId, q, pageable);
    }

    @Transactional(readOnly = true)
    public Lead getById(UUID id) {
        return leadRepository.findByIdAndTenantId(id, TenantContext.getTenantId())
                .orElseThrow(() -> new ResourceNotFoundException("Lead", id));
    }

    @Transactional
    public Lead create(String firstName, String lastName, String email, String phone,
                       String status, Integer score, String source, UUID ownerUserId,
                       Optional<String> idempotencyKey) {

        UUID tenantId = TenantContext.getTenantId();

        if (idempotencyKey.isPresent() && !idempotencyKey.get().isBlank()) {
            String keyHash = hashKey(idempotencyKey.get());
            var existing = idempotencyKeyRepository.findByTenantIdAndKeyHash(tenantId, keyHash);
            if (existing.isPresent()) {
                IdempotencyKey ik = existing.get();
                if (ik.getExpiresAt().isBefore(Instant.now())) {
                    idempotencyKeyRepository.delete(ik);
                } else {
                    return leadRepository.findByIdAndTenantId(ik.getEntityId(), tenantId)
                            .orElseThrow(() -> new ResourceNotFoundException("Lead", ik.getEntityId()));
                }
            }
        }

        Lead lead = new Lead();
        lead.setTenantId(tenantId);
        lead.setFirstName(firstName);
        lead.setLastName(lastName);
        lead.setEmail(email);
        lead.setPhone(phone);
        lead.setStatus(status != null ? status : "NEW");
        lead.setScore(score != null ? score : 0);
        lead.setSource(source);
        lead.setOwnerUserId(ownerUserId);

        triggerRegistry.fire(TriggerContext.lead(TriggerEventType.BEFORE_CREATE, lead, null));

        lead = leadRepository.save(lead);

        triggerRegistry.fire(TriggerContext.lead(TriggerEventType.AFTER_CREATE, lead, null));
        lead = leadRepository.save(lead);

        Map<String, Object> flowPayload = new HashMap<>();
        flowPayload.put("id", lead.getId());
        flowPayload.put("firstName", lead.getFirstName());
        flowPayload.put("lastName", lead.getLastName());
        flowPayload.put("email", lead.getEmail());
        flowPayload.put("status", lead.getStatus());
        flowPayload.put("score", lead.getScore());
        flowPayload.put("source", lead.getSource());
        flowEngine.fire(FlowEventType.LEAD_CREATED, flowPayload);
        lead = leadRepository.findById(lead.getId()).orElse(lead);

        webhookEventPublisher.publishLeadCreated(lead.getId(), flowPayload,
                idempotencyKey.orElse(null));

        if (idempotencyKey.isPresent() && !idempotencyKey.get().isBlank()) {
            IdempotencyKey ik = new IdempotencyKey();
            ik.setTenantId(tenantId);
            ik.setKeyHash(hashKey(idempotencyKey.get()));
            ik.setEntityType(ENTITY_TYPE_LEAD);
            ik.setEntityId(lead.getId());
            ik.setExpiresAt(Instant.now().plusSeconds(IDEMPOTENCY_TTL_HOURS * 3600L));
            idempotencyKeyRepository.save(ik);
        }

        return lead;
    }

    @Transactional
    public Lead update(UUID id, String firstName, String lastName, String email, String phone,
                       String status, Integer score, String source, UUID ownerUserId) {
        Lead lead = getById(id);
        if (firstName != null) lead.setFirstName(firstName);
        if (lastName != null) lead.setLastName(lastName);
        if (email != null) lead.setEmail(email);
        if (phone != null) lead.setPhone(phone);
        if (status != null) lead.setStatus(status);
        if (score != null) lead.setScore(score);
        if (source != null) lead.setSource(source);
        if (ownerUserId != null) lead.setOwnerUserId(ownerUserId);
        return leadRepository.save(lead);
    }

    @Transactional
    public void delete(UUID id) {
        Lead lead = getById(id);
        leadRepository.delete(lead);
    }

    private String hashKey(String key) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256").digest(key.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
