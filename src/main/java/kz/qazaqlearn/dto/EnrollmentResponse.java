package kz.qazaqlearn.dto;

import kz.qazaqlearn.domain.EnrollmentStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record EnrollmentResponse(
        UUID id,
        UUID courseId,
        UUID studentId,
        String studentName,
        EnrollmentStatus status,
        LocalDateTime enrolledAt
) {
}
