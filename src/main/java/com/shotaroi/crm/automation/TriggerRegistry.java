package com.shotaroi.crm.automation;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class TriggerRegistry {

    private final Map<String, List<TriggerHandler<?>>> handlers = new ConcurrentHashMap<>();

    public void register(String entityType, TriggerEventType eventType, TriggerHandler<?> handler) {
        String key = key(entityType, eventType);
        handlers.computeIfAbsent(key, k -> new ArrayList<>()).add(handler);
    }

    @SuppressWarnings("unchecked")
    public <T> void fire(TriggerContext<T> context) {
        String key = key(context.entityType(), context.eventType());
        List<TriggerHandler<?>> list = handlers.get(key);
        if (list != null) {
            for (TriggerHandler<?> h : list) {
                ((TriggerHandler<T>) h).handle(context);
            }
        }
    }

    private String key(String entityType, TriggerEventType eventType) {
        return entityType + ":" + eventType.name();
    }
}
