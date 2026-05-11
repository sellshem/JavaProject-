package kz.qazaqlearn.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record CourseStudentResponse(
        UUID studentId,
        String fullName,
        String email,
        LocalDateTime enrolledAt,
        int completedLessonsCount,
        int totalLessonsCount,
        int submittedAssignmentsCount,
        int totalAssignmentsCount,
        int progressPercent
) {
}
