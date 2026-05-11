package kz.qazaqlearn.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record AuditLogResponse(
        UUID id,
        UUID actorId,
        String actorEmail,
        String action,
        String entityType,
        UUID entityId,
        LocalDateTime timestamp,
        String ipAddress
) {
}
