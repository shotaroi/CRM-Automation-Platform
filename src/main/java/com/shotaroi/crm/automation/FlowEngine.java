package com.shotaroi.crm.automation;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shotaroi.crm.common.TenantContext;
import com.shotaroi.crm.domain.entity.Lead;
import com.shotaroi.crm.domain.entity.Task;
import com.shotaroi.crm.infrastructure.persistence.FlowDefinitionRepository;
import com.shotaroi.crm.infrastructure.persistence.LeadRepository;
import com.shotaroi.crm.infrastructure.persistence.TaskRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Flow runtime: evaluates conditions and executes actions from flow definitions.
 */
@Service
public class FlowEngine {

    private static final Logger log = LoggerFactory.getLogger(FlowEngine.class);

    private final FlowDefinitionRepository flowDefinitionRepository;
    private final LeadRepository leadRepository;
    private final TaskRepository taskRepository;
    private final ObjectMapper objectMapper;

    public FlowEngine(FlowDefinitionRepository flowDefinitionRepository,
                     LeadRepository leadRepository,
                     TaskRepository taskRepository,
                     ObjectMapper objectMapper) {
        this.flowDefinitionRepository = flowDefinitionRepository;
        this.leadRepository = leadRepository;
        this.taskRepository = taskRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public void fire(String eventType, Map<String, Object> payload) {
        UUID tenantId = TenantContext.getTenantId();
        List<com.shotaroi.crm.domain.entity.FlowDefinition> flows =
                flowDefinitionRepository.findByTenantIdAndEventTypeAndActiveTrue(tenantId, eventType);

        for (var flow : flows) {
            try {
                Map<String, Object> def = flow.getJsonDefinition();
                if (def == null) continue;

                @SuppressWarnings("unchecked")
                List<Map<String, Object>> conditions = (List<Map<String, Object>>) def.get("conditions");
                if (conditions != null && !evaluateConditions(conditions, payload)) {
                    continue;
                }

                @SuppressWarnings("unchecked")
                List<Map<String, Object>> actions = (List<Map<String, Object>>) def.get("actions");
                if (actions != null) {
                    executeActions(actions, payload, tenantId);
                }
            } catch (Exception e) {
                log.warn("Flow {} failed: {}", flow.getId(), e.getMessage());
            }
        }
    }

    private boolean evaluateConditions(List<Map<String, Object>> conditions, Map<String, Object> payload) {
        for (var cond : conditions) {
            String field = (String) cond.get("field");
            String op = (String) cond.get("operator");
            Object value = cond.get("value");

            Object actual = getNested(payload, field);
            if (!evaluateCondition(actual, op, value)) {
                return false;
            }
        }
        return true;
    }

    private boolean evaluateCondition(Object actual, String op, Object expected) {
        if (actual == null) return "isEmpty".equals(op);

        return switch (op) {
            case "equals" -> String.valueOf(actual).equals(String.valueOf(expected));
            case "contains" -> String.valueOf(actual).toLowerCase().contains(String.valueOf(expected).toLowerCase());
            case "greaterThan" -> compare(actual, expected) > 0;
            case "lessThan" -> compare(actual, expected) < 0;
            case "isEmpty" -> false;
            default -> false;
        };
    }

    private int compare(Object a, Object b) {
        if (a instanceof Number na && b instanceof Number nb) {
            return Double.compare(na.doubleValue(), nb.doubleValue());
        }
        return String.valueOf(a).compareTo(String.valueOf(b));
    }

    private void executeActions(List<Map<String, Object>> actions, Map<String, Object> payload, UUID tenantId) {
        for (var action : actions) {
            String type = (String) action.get("type");
            if ("setField".equals(type)) {
                String field = (String) action.get("field");
                Object value = action.get("value");
                if (field != null && value != null) {
                    payload.put(field, value);
                    Object idObj = payload.get("id");
                    if (idObj != null) {
                        UUID leadId = idObj instanceof UUID ? (UUID) idObj : UUID.fromString(String.valueOf(idObj));
                        leadRepository.findByIdAndTenantId(leadId, tenantId).ifPresent(lead -> {
                            if ("score".equals(field) && value instanceof Number n) {
                                lead.setScore(n.intValue());
                                leadRepository.save(lead);
                            }
                        });
                    }
                }
            } else if ("createTask".equals(type)) {
                String title = (String) action.get("title");
                String relatedType = (String) action.get("relatedType");
                Object relatedIdObj = action.get("relatedId");
                if (relatedIdObj == null) relatedIdObj = payload.get("id");
                UUID relatedId = relatedIdObj instanceof UUID ? (UUID) relatedIdObj : UUID.fromString(String.valueOf(relatedIdObj));

                Task task = new Task();
                task.setTenantId(tenantId);
                task.setRelatedType(relatedType != null ? relatedType : "Lead");
                task.setRelatedId(relatedId);
                task.setTitle(title != null ? title : "Follow up");
                task.setStatus("PENDING");
                taskRepository.save(task);
            } else if ("sendWebhook".equals(type)) {
                // Delegate to webhook service - will be called from integration
                // For now we skip - webhook service will pick up events
            }
        }
    }

    private Object getNested(Map<String, Object> map, String path) {
        if (path == null) return null;
        String[] parts = path.split("\\.");
        Object current = map;
        for (String p : parts) {
            if (current instanceof Map) {
                current = ((Map<?, ?>) current).get(p);
            } else {
                return null;
            }
        }
        return current;
    }
}
