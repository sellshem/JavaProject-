package kz.qazaqlearn.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record LessonResponse(
        UUID id,
        UUID courseId,
        String titleKk,
        String contentKk,
        Integer lessonOrder,
        boolean published,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
