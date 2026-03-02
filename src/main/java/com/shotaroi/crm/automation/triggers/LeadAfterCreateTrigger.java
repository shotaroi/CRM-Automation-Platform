package com.shotaroi.crm.automation.triggers;

import com.shotaroi.crm.automation.TriggerContext;
import com.shotaroi.crm.automation.TriggerHandler;
import com.shotaroi.crm.common.TenantContext;
import com.shotaroi.crm.domain.entity.Lead;
import com.shotaroi.crm.domain.entity.User;
import com.shotaroi.crm.infrastructure.persistence.UserRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * Lead AFTER_CREATE: assign owner based on routing rules (round-robin among users).
 */
@Component
public class LeadAfterCreateTrigger implements TriggerHandler<Lead> {

    private final UserRepository userRepository;

    public LeadAfterCreateTrigger(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void handle(TriggerContext<Lead> context) {
        Lead lead = context.entity();
        if (lead.getOwnerUserId() != null) {
            return; // Already assigned
        }

        UUID tenantId = TenantContext.getTenantId();
        List<User> users = userRepository.findAll().stream()
                .filter(u -> u.getTenantId().equals(tenantId))
                .toList();

        if (users.isEmpty()) return;

        UUID leadId = lead.getId();
        int index = (int) (leadId.getLeastSignificantBits() % users.size());
        if (index < 0) index = -index;
        lead.setOwnerUserId(users.get(index).getId());
    }
}
