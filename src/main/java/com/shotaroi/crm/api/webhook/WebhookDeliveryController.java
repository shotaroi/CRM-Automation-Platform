package com.shotaroi.crm.api.webhook;

import com.shotaroi.crm.application.webhook.WebhookService;
import com.shotaroi.crm.domain.entity.WebhookDelivery;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/webhook-deliveries")
public class WebhookDeliveryController {

    private final WebhookService webhookService;

    public WebhookDeliveryController(WebhookService webhookService) {
        this.webhookService = webhookService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<WebhookDeliveryResponse> list(@RequestParam(required = false) String status) {
        return webhookService.listDeliveries(status, 50).stream()
                .map(this::toResponse)
                .toList();
    }

    private WebhookDeliveryResponse toResponse(WebhookDelivery d) {
        return new WebhookDeliveryResponse(
                d.getId(),
                d.getTenantId(),
                d.getEndpointId(),
                d.getEventType(),
                d.getPayload(),
                d.getStatus(),
                d.getAttemptCount(),
                d.getNextAttemptAt(),
                d.getIdempotencyKey(),
                d.getCreatedAt()
        );
    }
}
