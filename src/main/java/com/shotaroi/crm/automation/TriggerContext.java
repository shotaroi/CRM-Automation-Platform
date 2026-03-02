package com.shotaroi.crm.automation;

import com.shotaroi.crm.domain.entity.Lead;

public record TriggerContext<T>(
        String entityType,
        TriggerEventType eventType,
        T entity,
        T oldEntity  // null for CREATE
) {
    public static TriggerContext<Lead> lead(TriggerEventType eventType, Lead lead, Lead oldLead) {
        return new TriggerContext<>("Lead", eventType, lead, oldLead);
    }
}
