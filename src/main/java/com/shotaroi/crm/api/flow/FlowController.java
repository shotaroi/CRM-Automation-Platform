package com.shotaroi.crm.api.flow;

import com.shotaroi.crm.application.flow.FlowService;
import com.shotaroi.crm.domain.entity.FlowDefinition;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/flows")
public class FlowController {

    private final FlowService flowService;

    public FlowController(FlowService flowService) {
        this.flowService = flowService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SALES')")
    public List<FlowResponse> list(@RequestParam(required = false) String eventType) {
        return flowService.list(eventType).stream()
                .map(this::toResponse)
                .toList();
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<FlowResponse> create(@Valid @RequestBody FlowRequest request) {
        FlowDefinition flow = flowService.create(
                request.eventType(),
                request.name(),
                request.jsonDefinition()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(flow));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public FlowResponse update(@PathVariable java.util.UUID id, @Valid @RequestBody FlowRequest request) {
        FlowDefinition flow = flowService.update(
                id,
                request.eventType(),
                request.name(),
                request.jsonDefinition()
        );
        return toResponse(flow);
    }

    @PostMapping("/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    public FlowResponse activate(@PathVariable java.util.UUID id) {
        return toResponse(flowService.activate(id));
    }

    @PostMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public FlowResponse deactivate(@PathVariable java.util.UUID id) {
        return toResponse(flowService.deactivate(id));
    }

    private FlowResponse toResponse(FlowDefinition f) {
        return new FlowResponse(
                f.getId(),
                f.getTenantId(),
                f.getEventType(),
                f.getName(),
                f.isActive(),
                f.getJsonDefinition(),
                f.getCreatedAt(),
                f.getUpdatedAt()
        );
    }
}
