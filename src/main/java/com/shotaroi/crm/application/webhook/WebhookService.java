package com.shotaroi.crm.application.webhook;

import com.shotaroi.crm.common.ResourceNotFoundException;
import com.shotaroi.crm.common.TenantContext;
import com.shotaroi.crm.domain.entity.WebhookDelivery;
import com.shotaroi.crm.domain.entity.WebhookEndpoint;
import com.shotaroi.crm.infrastructure.persistence.WebhookDeliveryRepository;
import com.shotaroi.crm.infrastructure.persistence.WebhookEndpointRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class WebhookService {

    private final WebhookEndpointRepository endpointRepository;
    private final WebhookDeliveryRepository deliveryRepository;

    public WebhookService(WebhookEndpointRepository endpointRepository,
                          WebhookDeliveryRepository deliveryRepository) {
        this.endpointRepository = endpointRepository;
        this.deliveryRepository = deliveryRepository;
    }

    @Transactional(readOnly = true)
    public List<WebhookEndpoint> listEndpoints() {
        return endpointRepository.findByTenantId(TenantContext.getTenantId());
    }

    @Transactional
    public WebhookEndpoint createEndpoint(String url, String secret) {
        WebhookEndpoint ep = new WebhookEndpoint();
        ep.setTenantId(TenantContext.getTenantId());
        ep.setUrl(url);
        ep.setSecret(secret);
        ep.setActive(true);
        return endpointRepository.save(ep);
    }

    @Transactional(readOnly = true)
    public List<WebhookDelivery> listDeliveries(String status, int limit) {
        var pageable = PageRequest.of(0, Math.min(limit, 100));
        var page = status != null && !status.isBlank()
                ? deliveryRepository.findByTenantIdAndStatusOrderByCreatedAtDesc(TenantContext.getTenantId(), status, pageable)
                : deliveryRepository.findByTenantIdOrderByCreatedAtDesc(TenantContext.getTenantId(), pageable);
        return page.getContent();
    }

    @Transactional
    public void enqueueDelivery(UUID endpointId, String eventType, Map<String, Object> payload, String idempotencyKey) {
        WebhookEndpoint ep = endpointRepository.findByIdAndTenantId(endpointId, TenantContext.getTenantId())
                .orElseThrow(() -> new ResourceNotFoundException("WebhookEndpoint", endpointId));
        if (!ep.isActive()) return;

        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            var existing = deliveryRepository.findByEndpointIdAndIdempotencyKey(endpointId, idempotencyKey);
            if (existing.isPresent()) return;
        }

        WebhookDelivery d = new WebhookDelivery();
        d.setTenantId(TenantContext.getTenantId());
        d.setEndpointId(endpointId);
        d.setEventType(eventType);
        d.setPayload(payload);
        d.setStatus("PENDING");
        d.setAttemptCount(0);
        d.setNextAttemptAt(java.time.Instant.now());
        d.setIdempotencyKey(idempotencyKey);
        deliveryRepository.save(d);
    }
}
