package com.shotaroi.crm.common;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Aspect
@Component
public class AuditAspect {

    private final AuditService auditService;

    public AuditAspect(AuditService auditService) {
        this.auditService = auditService;
    }

    @AfterReturning(
            pointcut = "execution(* com.shotaroi.crm.application.account.AccountService.create(..)) || " +
                    "execution(* com.shotaroi.crm.application.contact.ContactService.create(..)) || " +
                    "execution(* com.shotaroi.crm.application.lead.LeadService.create(..)) || " +
                    "execution(* com.shotaroi.crm.application.opportunity.OpportunityService.create(..))",
            returning = "result"
    )
    public void auditCreate(JoinPoint jp, Object result) {
        if (result != null) {
            String entityType = result.getClass().getSimpleName();
            UUID entityId = getEntityId(result);
            if (entityId != null) {
                auditService.log(entityType, entityId, "CREATE", null, result);
            }
        }
    }

    @AfterReturning(
            pointcut = "execution(* com.shotaroi.crm.application.account.AccountService.update(..)) || " +
                    "execution(* com.shotaroi.crm.application.contact.ContactService.update(..)) || " +
                    "execution(* com.shotaroi.crm.application.lead.LeadService.update(..)) || " +
                    "execution(* com.shotaroi.crm.application.opportunity.OpportunityService.update(..))",
            returning = "result"
    )
    public void auditUpdate(JoinPoint jp, Object result) {
        if (result != null) {
            String entityType = result.getClass().getSimpleName();
            UUID entityId = getEntityId(result);
            if (entityId != null) {
                auditService.log(entityType, entityId, "UPDATE", null, result);
            }
        }
    }

    private UUID getEntityId(Object entity) {
        try {
            var method = entity.getClass().getMethod("getId");
            Object id = method.invoke(entity);
            return id instanceof UUID ? (UUID) id : null;
        } catch (Exception e) {
            return null;
        }
    }
}
