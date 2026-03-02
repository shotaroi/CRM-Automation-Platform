package com.shotaroi.crm.api.webhook;

import com.shotaroi.crm.application.webhook.WebhookService;
import com.shotaroi.crm.domain.entity.WebhookEndpoint;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/webhooks")
public class WebhookController {

    private final WebhookService webhookService;

    public WebhookController(WebhookService webhookService) {
        this.webhookService = webhookService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<WebhookResponse> listEndpoints() {
        return webhookService.listEndpoints().stream()
                .map(this::toResponse)
                .toList();
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<WebhookResponse> createEndpoint(@Valid @RequestBody WebhookRequest request) {
        WebhookEndpoint ep = webhookService.createEndpoint(request.url(), request.secret());
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(ep));
    }

    private WebhookResponse toResponse(WebhookEndpoint ep) {
        return new WebhookResponse(
                ep.getId(),
                ep.getTenantId(),
                ep.getUrl(),
                ep.isActive(),
                ep.getCreatedAt()
        );
    }
}
