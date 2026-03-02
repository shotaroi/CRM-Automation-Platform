package com.shotaroi.crm.automation.triggers;

import com.shotaroi.crm.automation.TriggerContext;
import com.shotaroi.crm.automation.TriggerHandler;
import com.shotaroi.crm.domain.entity.Lead;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Lead BEFORE_CREATE: normalize email/phone, validate source.
 */
@Component
public class LeadBeforeCreateTrigger implements TriggerHandler<Lead> {

    @Override
    public void handle(TriggerContext<Lead> context) {
        Lead lead = context.entity();

        if (lead.getEmail() != null && !lead.getEmail().isBlank()) {
            lead.setEmail(lead.getEmail().trim().toLowerCase());
        }

        if (lead.getPhone() != null && !lead.getPhone().isBlank()) {
            lead.setPhone(normalizePhone(lead.getPhone()));
        }

        if (lead.getSource() != null && !lead.getSource().isBlank()) {
            lead.setSource(lead.getSource().trim().toUpperCase());
        }
    }

    private String normalizePhone(String phone) {
        return phone.replaceAll("[^0-9+]", "");
    }
}
