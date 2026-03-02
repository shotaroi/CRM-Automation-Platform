package com.shotaroi.crm.application.contact;

import com.shotaroi.crm.common.ResourceNotFoundException;
import com.shotaroi.crm.common.TenantContext;
import com.shotaroi.crm.domain.entity.Contact;
import com.shotaroi.crm.infrastructure.persistence.ContactRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class ContactService {

    private final ContactRepository contactRepository;

    public ContactService(ContactRepository contactRepository) {
        this.contactRepository = contactRepository;
    }

    @Transactional(readOnly = true)
    public List<Contact> list() {
        return contactRepository.findByTenantIdOrderByCreatedAtDesc(TenantContext.getTenantId());
    }

    @Transactional(readOnly = true)
    public Contact getById(UUID id) {
        return contactRepository.findByIdAndTenantId(id, TenantContext.getTenantId())
                .orElseThrow(() -> new ResourceNotFoundException("Contact", id));
    }

    @Transactional
    public Contact create(UUID accountId, String firstName, String lastName, String email, String phone, String personalNumber) {
        Contact contact = new Contact();
        contact.setTenantId(TenantContext.getTenantId());
        contact.setAccountId(accountId);
        contact.setFirstName(firstName);
        contact.setLastName(lastName);
        contact.setEmail(email);
        contact.setPhone(phone);
        contact.setPersonalNumber(personalNumber);
        return contactRepository.save(contact);
    }

    @Transactional
    public Contact update(UUID id, UUID accountId, String firstName, String lastName, String email, String phone, String personalNumber) {
        Contact contact = getById(id);
        if (accountId != null) contact.setAccountId(accountId);
        if (firstName != null) contact.setFirstName(firstName);
        if (lastName != null) contact.setLastName(lastName);
        if (email != null) contact.setEmail(email);
        if (phone != null) contact.setPhone(phone);
        if (personalNumber != null) contact.setPersonalNumber(personalNumber);
        return contactRepository.save(contact);
    }

    @Transactional
    public void delete(UUID id) {
        Contact contact = getById(id);
        contactRepository.delete(contact);
    }
}
