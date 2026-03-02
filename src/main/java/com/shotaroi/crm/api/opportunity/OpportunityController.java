package com.shotaroi.crm.api.opportunity;

import com.shotaroi.crm.application.opportunity.OpportunityService;
import com.shotaroi.crm.domain.entity.Opportunity;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/opportunities")
public class OpportunityController {

    private final OpportunityService opportunityService;

    public OpportunityController(OpportunityService opportunityService) {
        this.opportunityService = opportunityService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SALES')")
    public List<OpportunityResponse> list() {
        return opportunityService.list().stream().map(this::toResponse).toList();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SALES')")
    public OpportunityResponse getById(@PathVariable java.util.UUID id) {
        return toResponse(opportunityService.getById(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SALES')")
    public ResponseEntity<OpportunityResponse> create(@Valid @RequestBody OpportunityRequest request) {
        Opportunity opp = opportunityService.create(
                request.accountId(),
                request.name(),
                request.amount(),
                request.stage(),
                request.closeDate(),
                request.ownerUserId()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(opp));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SALES')")
    public OpportunityResponse update(@PathVariable java.util.UUID id, @Valid @RequestBody OpportunityRequest request) {
        return toResponse(opportunityService.update(
                id, request.accountId(), request.name(), request.amount(),
                request.stage(), request.closeDate(), request.ownerUserId()
        ));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable java.util.UUID id) {
        opportunityService.delete(id);
        return ResponseEntity.noContent().build();
    }

    private OpportunityResponse toResponse(Opportunity o) {
        return new OpportunityResponse(
                o.getId(),
                o.getTenantId(),
                o.getAccountId(),
                o.getName(),
                o.getAmount(),
                o.getStage(),
                o.getCloseDate(),
                o.getOwnerUserId(),
                o.getCreatedAt(),
                o.getUpdatedAt(),
                o.getVersion()
        );
    }
}
