package com.shotaroi.crm.api.account;

import com.shotaroi.crm.application.account.AccountService;
import com.shotaroi.crm.domain.entity.Account;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SALES', 'SUPPORT')")
    public List<AccountResponse> list() {
        return accountService.list().stream().map(this::toResponse).toList();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SALES', 'SUPPORT')")
    public AccountResponse getById(@PathVariable java.util.UUID id) {
        return toResponse(accountService.getById(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SALES')")
    public ResponseEntity<AccountResponse> create(@Valid @RequestBody AccountRequest request) {
        Account account = accountService.create(
                request.name(),
                request.industry(),
                request.ownerUserId()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(account));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SALES')")
    public AccountResponse update(@PathVariable java.util.UUID id, @Valid @RequestBody AccountRequest request) {
        return toResponse(accountService.update(id, request.name(), request.industry(), request.ownerUserId()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable java.util.UUID id) {
        accountService.delete(id);
        return ResponseEntity.noContent().build();
    }

    private AccountResponse toResponse(Account a) {
        return new AccountResponse(
                a.getId(),
                a.getTenantId(),
                a.getName(),
                a.getIndustry(),
                a.getOwnerUserId(),
                a.getCreatedAt(),
                a.getUpdatedAt(),
                a.getVersion()
        );
    }
}
