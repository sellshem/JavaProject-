package kz.qazaqlearn.domain.events;

import java.time.LocalDateTime;
import java.util.UUID;

public record CourseCreatedEvent(
    UUID courseId,
    UUID teacherId,
    String titleKk,
    LocalDateTime timestamp
) implements DomainEvent {
    @Override
    public String getEventType() {
        return "COURSE_CREATED";
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
