package kz.qazaqlearn.mapper;

import kz.qazaqlearn.domain.AuditLog;
import kz.qazaqlearn.dto.AuditLogResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AuditLogMapper {

    AuditLogResponse toDto(AuditLog auditLog);
}
