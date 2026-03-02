package com.shotaroi.crm.automation;

public interface TriggerHandler<T> {

    void handle(TriggerContext<T> context);
}
