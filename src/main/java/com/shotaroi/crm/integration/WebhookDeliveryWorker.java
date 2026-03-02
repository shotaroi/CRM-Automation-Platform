package com.shotaroi.crm.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shotaroi.crm.common.TenantContext;
import com.shotaroi.crm.domain.entity.WebhookDelivery;
import com.shotaroi.crm.domain.entity.WebhookEndpoint;
import com.shotaroi.crm.infrastructure.persistence.WebhookDeliveryRepository;
import com.shotaroi.crm.infrastructure.persistence.WebhookEndpointRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Component
public class WebhookDeliveryWorker {

    private static final Logger log = LoggerFactory.getLogger(WebhookDeliveryWorker.class);
    private static final int MAX_ATTEMPTS = 5;
    private static final long BASE_DELAY_MS = 1000;

    private final WebhookDeliveryRepository deliveryRepository;
    private final WebhookEndpointRepository endpointRepository;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    public WebhookDeliveryWorker(WebhookDeliveryRepository deliveryRepository,
                                 WebhookEndpointRepository endpointRepository,
                                 ObjectMapper objectMapper) {
        this.deliveryRepository = deliveryRepository;
        this.endpointRepository = endpointRepository;
        this.objectMapper = objectMapper;
    }

    @Scheduled(fixedDelay = 5000)
    @Transactional
    public void processPendingDeliveries() {
        List<WebhookDelivery> pending = deliveryRepository.findPendingDeliveries(Instant.now());
        for (WebhookDelivery d : pending) {
            try {
                TenantContext.setTenantId(d.getTenantId());
                deliver(d);
            } catch (Exception e) {
                log.warn("Webhook delivery {} failed: {}", d.getId(), e.getMessage());
            } finally {
                TenantContext.clear();
            }
        }
    }

    private void deliver(WebhookDelivery d) {
        WebhookEndpoint ep = endpointRepository.findById(d.getEndpointId()).orElse(null);
        if (ep == null || !ep.isActive()) {
            d.setStatus("CANCELLED");
            deliveryRepository.save(d);
            return;
        }

        try {
            String body = objectMapper.writeValueAsString(d.getPayload());
            var request = HttpRequest.newBuilder()
                    .uri(URI.create(ep.getUrl()))
                    .header("Content-Type", "application/json")
                    .header("X-Webhook-Event", d.getEventType())
                    .header("X-Idempotency-Key", d.getIdempotencyKey() != null ? d.getIdempotencyKey() : d.getId().toString())
                    .timeout(Duration.ofSeconds(15))
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            var response = httpClient.send(request, java.net.http.HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                d.setStatus("DELIVERED");
                deliveryRepository.save(d);
            } else {
                scheduleRetry(d);
            }
        } catch (Exception e) {
            scheduleRetry(d);
        }
    }

    private void scheduleRetry(WebhookDelivery d) {
        d.setAttemptCount(d.getAttemptCount() + 1);
        if (d.getAttemptCount() >= MAX_ATTEMPTS) {
            d.setStatus("FAILED");
        } else {
            long delay = (long) (BASE_DELAY_MS * Math.pow(2, d.getAttemptCount()));
            d.setNextAttemptAt(Instant.now().plusMillis(delay));
        }
        deliveryRepository.save(d);
    }
}
