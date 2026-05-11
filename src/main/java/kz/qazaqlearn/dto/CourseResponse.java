package kz.qazaqlearn.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record CourseResponse(
        UUID id,
        String titleKk,
        String descriptionKk,
        boolean published,
        UUID teacherId,
        String teacherName,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
