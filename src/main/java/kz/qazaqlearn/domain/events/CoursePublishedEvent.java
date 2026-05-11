package kz.qazaqlearn.domain.events;

import java.time.LocalDateTime;
import java.util.UUID;

public record CoursePublishedEvent(
    UUID courseId,
    UUID teacherId,
    boolean published,
    LocalDateTime timestamp
) implements DomainEvent {
    @Override
    public String getEventType() {
        return "COURSE_PUBLISHED";
    }

    @Override
    public UUID getAggregateId() {
        return courseId;
    }

    @Override
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
}
