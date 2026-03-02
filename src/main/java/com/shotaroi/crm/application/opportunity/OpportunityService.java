package com.shotaroi.crm.application.opportunity;

import com.shotaroi.crm.common.ResourceNotFoundException;
import com.shotaroi.crm.common.TenantContext;
import com.shotaroi.crm.domain.entity.Opportunity;
import com.shotaroi.crm.infrastructure.persistence.OpportunityRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class OpportunityService {

    private final OpportunityRepository opportunityRepository;

    public OpportunityService(OpportunityRepository opportunityRepository) {
        this.opportunityRepository = opportunityRepository;
    }

    @Transactional(readOnly = true)
    public List<Opportunity> list() {
        return opportunityRepository.findByTenantIdOrderByCreatedAtDesc(TenantContext.getTenantId());
    }

    @Transactional(readOnly = true)
    public Opportunity getById(UUID id) {
        return opportunityRepository.findByIdAndTenantId(id, TenantContext.getTenantId())
                .orElseThrow(() -> new ResourceNotFoundException("Opportunity", id));
    }

    @Transactional
    public Opportunity create(UUID accountId, String name, BigDecimal amount, String stage, LocalDate closeDate, UUID ownerUserId) {
        Opportunity opp = new Opportunity();
        opp.setTenantId(TenantContext.getTenantId());
        opp.setAccountId(accountId);
        opp.setName(name);
        opp.setAmount(amount);
        opp.setStage(stage != null ? stage : "PROSPECTING");
        opp.setCloseDate(closeDate);
        opp.setOwnerUserId(ownerUserId);
        return opportunityRepository.save(opp);
    }

    @Transactional
    public Opportunity update(UUID id, UUID accountId, String name, BigDecimal amount, String stage, LocalDate closeDate, UUID ownerUserId) {
        Opportunity opp = getById(id);
        if (accountId != null) opp.setAccountId(accountId);
        if (name != null) opp.setName(name);
        if (amount != null) opp.setAmount(amount);
        if (stage != null) opp.setStage(stage);
        if (closeDate != null) opp.setCloseDate(closeDate);
        if (ownerUserId != null) opp.setOwnerUserId(ownerUserId);
        return opportunityRepository.save(opp);
    }

    @Transactional
    public void delete(UUID id) {
        Opportunity opp = getById(id);
        opportunityRepository.delete(opp);
    }
}
