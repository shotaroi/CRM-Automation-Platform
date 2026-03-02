package com.shotaroi.crm.api.contact;

import com.shotaroi.crm.application.contact.ContactService;
import com.shotaroi.crm.domain.UserRole;
import com.shotaroi.crm.domain.entity.Contact;
import com.shotaroi.crm.infrastructure.security.AuthenticatedUser;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/contacts")
public class ContactController {

    private final ContactService contactService;

    public ContactController(ContactService contactService) {
        this.contactService = contactService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SALES', 'SUPPORT')")
    public List<ContactResponse> list(@AuthenticationPrincipal AuthenticatedUser user) {
        return contactService.list().stream()
                .map(c -> toResponse(c, user.role()))
                .toList();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SALES', 'SUPPORT')")
    public ContactResponse getById(@PathVariable java.util.UUID id, @AuthenticationPrincipal AuthenticatedUser user) {
        return toResponse(contactService.getById(id), user.role());
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SALES')")
    public ResponseEntity<ContactResponse> create(@Valid @RequestBody ContactRequest request,
                                                 @AuthenticationPrincipal AuthenticatedUser user) {
        Contact contact = contactService.create(
                request.accountId(),
                request.firstName(),
                request.lastName(),
                request.email(),
                request.phone(),
                request.personalNumber()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(contact, user.role()));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SALES')")
    public ContactResponse update(@PathVariable java.util.UUID id, @Valid @RequestBody ContactRequest request,
                                 @AuthenticationPrincipal AuthenticatedUser user) {
        return toResponse(contactService.update(
                id, request.accountId(), request.firstName(), request.lastName(),
                request.email(), request.phone(), request.personalNumber()
        ), user.role());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable java.util.UUID id) {
        contactService.delete(id);
        return ResponseEntity.noContent().build();
    }

    private ContactResponse toResponse(Contact c, UserRole role) {
        String personalNumber = (role == UserRole.SALES) ? "***" : c.getPersonalNumber();
        return new ContactResponse(
                c.getId(),
                c.getTenantId(),
                c.getAccountId(),
                c.getFirstName(),
                c.getLastName(),
                c.getEmail(),
                c.getPhone(),
                personalNumber,
                c.getCreatedAt(),
                c.getUpdatedAt(),
                c.getVersion()
        );
    }
}
