package com.shotaroi.crm.application.account;

import com.shotaroi.crm.common.ResourceNotFoundException;
import com.shotaroi.crm.common.TenantContext;
import com.shotaroi.crm.domain.entity.Account;
import com.shotaroi.crm.infrastructure.persistence.AccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class AccountService {

    private final AccountRepository accountRepository;

    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Transactional(readOnly = true)
    public List<Account> list() {
        return accountRepository.findByTenantIdOrderByCreatedAtDesc(TenantContext.getTenantId());
    }

    @Transactional(readOnly = true)
    public Account getById(UUID id) {
        return accountRepository.findByIdAndTenantId(id, TenantContext.getTenantId())
                .orElseThrow(() -> new ResourceNotFoundException("Account", id));
    }

    @Transactional
    public Account create(String name, String industry, UUID ownerUserId) {
        Account account = new Account();
        account.setTenantId(TenantContext.getTenantId());
        account.setName(name);
        account.setIndustry(industry);
        account.setOwnerUserId(ownerUserId);
        return accountRepository.save(account);
    }

    @Transactional
    public Account update(UUID id, String name, String industry, UUID ownerUserId) {
        Account account = getById(id);
        if (name != null) account.setName(name);
        if (industry != null) account.setIndustry(industry);
        if (ownerUserId != null) account.setOwnerUserId(ownerUserId);
        return accountRepository.save(account);
    }

    @Transactional
    public void delete(UUID id) {
        Account account = getById(id);
        accountRepository.delete(account);
    }
}
