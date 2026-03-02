package com.shotaroi.crm.application.webhook;

import com.shotaroi.crm.common.TenantContext;
import com.shotaroi.crm.infrastructure.persistence.WebhookEndpointRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class WebhookEventPublisher {

    private final WebhookService webhookService;
    private final WebhookEndpointRepository endpointRepository;

    public WebhookEventPublisher(WebhookService webhookService,
                                 WebhookEndpointRepository endpointRepository) {
        this.webhookService = webhookService;
        this.endpointRepository = endpointRepository;
    }

    @Transactional(readOnly = true)
    public void publishLeadCreated(UUID leadId, Map<String, Object> leadData, String idempotencyKey) {
        UUID tenantId = TenantContext.getTenantId();
        var endpoints = endpointRepository.findByTenantIdAndActiveTrue(tenantId);
        Map<String, Object> payload = new HashMap<>(leadData != null ? leadData : Map.of());
        payload.put("event", "LeadCreated");
        payload.put("id", leadId.toString());

        for (var ep : endpoints) {
            String key = idempotencyKey != null && !idempotencyKey.isBlank()
                    ? idempotencyKey + "-" + ep.getId()
                    : leadId + "-" + ep.getId();
            webhookService.enqueueDelivery(ep.getId(), "LeadCreated", payload, key);
        }
    }
}
