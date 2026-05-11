package kz.qazaqlearn.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record ProgressResponse(
        UUID id,
        UUID studentId,
        UUID courseId,
        UUID lessonId,
        boolean completed,
        LocalDateTime completedAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
