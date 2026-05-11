package kz.qazaqlearn.service;

import kz.qazaqlearn.domain.AuditLog;
import kz.qazaqlearn.domain.User;
import kz.qazaqlearn.repository.AuditLogRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    public AuditLogService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    public void logAction(User actor, String action, String entityType, UUID entityId, String ipAddress) {
        AuditLog log = new AuditLog();
        log.setActorId(actor != null ? actor.getId() : null);
        log.setActorEmail(actor != null ? actor.getEmail() : null);
        log.setAction(action);
        log.setEntityType(entityType);
        log.setEntityId(entityId);
        log.setIpAddress(ipAddress);
        auditLogRepository.save(log);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public List<AuditLog> getAllAuditLogs() {
        return auditLogRepository.findAll();
    }
}