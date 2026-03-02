package com.shotaroi.crm.automation;

import com.shotaroi.crm.automation.triggers.LeadAfterCreateTrigger;
import com.shotaroi.crm.automation.triggers.LeadBeforeCreateTrigger;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TriggerConfig {

    public TriggerConfig(TriggerRegistry registry,
                         LeadBeforeCreateTrigger leadBeforeCreateTrigger,
                         LeadAfterCreateTrigger leadAfterCreateTrigger) {
        registry.register("Lead", TriggerEventType.BEFORE_CREATE, leadBeforeCreateTrigger);
        registry.register("Lead", TriggerEventType.AFTER_CREATE, leadAfterCreateTrigger);
    }
}
