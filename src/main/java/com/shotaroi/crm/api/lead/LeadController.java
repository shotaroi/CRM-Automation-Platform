package com.shotaroi.crm.api.lead;

import com.shotaroi.crm.application.lead.LeadService;
import com.shotaroi.crm.domain.entity.Lead;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/leads")
public class LeadController {

    private final LeadService leadService;

    public LeadController(LeadService leadService) {
        this.leadService = leadService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SALES')")
    public Page<LeadResponse> list(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) UUID owner,
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, Math.min(size, 100));
        return leadService.list(status, owner, q, pageable).map(this::toResponse);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SALES')")
    public LeadResponse getById(@PathVariable UUID id) {
        return toResponse(leadService.getById(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SALES')")
    public ResponseEntity<LeadResponse> create(
            @Valid @RequestBody LeadRequest request,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey) {
        Lead lead = leadService.create(
                request.firstName(),
                request.lastName(),
                request.email(),
                request.phone(),
                request.status(),
                request.score(),
                request.source(),
                request.ownerUserId(),
                Optional.ofNullable(idempotencyKey).filter(k -> !k.isBlank())
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(lead));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SALES')")
    public LeadResponse update(@PathVariable UUID id, @Valid @RequestBody LeadRequest request) {
        return toResponse(leadService.update(
                id, request.firstName(), request.lastName(), request.email(), request.phone(),
                request.status(), request.score(), request.source(), request.ownerUserId()
        ));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        leadService.delete(id);
        return ResponseEntity.noContent().build();
    }

    private LeadResponse toResponse(Lead l) {
        return new LeadResponse(
                l.getId(),
                l.getTenantId(),
                l.getFirstName(),
                l.getLastName(),
                l.getEmail(),
                l.getPhone(),
                l.getStatus(),
                l.getScore(),
                l.getSource(),
                l.getOwnerUserId(),
                l.getCreatedAt(),
                l.getUpdatedAt(),
                l.getVersion()
        );
    }
}
