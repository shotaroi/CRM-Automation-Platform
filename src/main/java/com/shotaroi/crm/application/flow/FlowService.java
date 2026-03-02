package com.shotaroi.crm.application.flow;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shotaroi.crm.common.ResourceNotFoundException;
import com.shotaroi.crm.common.TenantContext;
import com.shotaroi.crm.domain.entity.FlowDefinition;
import com.shotaroi.crm.infrastructure.persistence.FlowDefinitionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class FlowService {

    private final FlowDefinitionRepository flowDefinitionRepository;
    private final ObjectMapper objectMapper;

    public FlowService(FlowDefinitionRepository flowDefinitionRepository, ObjectMapper objectMapper) {
        this.flowDefinitionRepository = flowDefinitionRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional(readOnly = true)
    public List<FlowDefinition> list(String eventType) {
        UUID tenantId = TenantContext.getTenantId();
        if (eventType != null && !eventType.isBlank()) {
            return flowDefinitionRepository.findByTenantIdAndEventType(tenantId, eventType);
        }
        return flowDefinitionRepository.findByTenantId(tenantId);
    }

    @Transactional(readOnly = true)
    public FlowDefinition getById(UUID id) {
        return flowDefinitionRepository.findByIdAndTenantId(id, TenantContext.getTenantId())
                .orElseThrow(() -> new ResourceNotFoundException("Flow", id));
    }

    @Transactional
    public FlowDefinition create(String eventType, String name, Map<String, Object> jsonDefinition) {
        FlowDefinition flow = new FlowDefinition();
        flow.setTenantId(TenantContext.getTenantId());
        flow.setEventType(eventType);
        flow.setName(name);
        flow.setActive(false);
        flow.setJsonDefinition(jsonDefinition != null ? jsonDefinition : Map.of());
        return flowDefinitionRepository.save(flow);
    }

    @Transactional
    public FlowDefinition update(UUID id, String eventType, String name, Map<String, Object> jsonDefinition) {
        FlowDefinition flow = getById(id);
        if (eventType != null) flow.setEventType(eventType);
        if (name != null) flow.setName(name);
        if (jsonDefinition != null) flow.setJsonDefinition(jsonDefinition);
        return flowDefinitionRepository.save(flow);
    }

    @Transactional
    public FlowDefinition activate(UUID id) {
        FlowDefinition flow = getById(id);
        flow.setActive(true);
        return flowDefinitionRepository.save(flow);
    }

    @Transactional
    public FlowDefinition deactivate(UUID id) {
        FlowDefinition flow = getById(id);
        flow.setActive(false);
        return flowDefinitionRepository.save(flow);
    }
}
