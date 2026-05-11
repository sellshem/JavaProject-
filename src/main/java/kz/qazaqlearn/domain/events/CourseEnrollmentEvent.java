package kz.qazaqlearn.domain.events;

import java.time.LocalDateTime;
import java.util.UUID;

public record CourseEnrollmentEvent(
    UUID enrollmentId,
    UUID courseId,
    UUID studentId,
    LocalDateTime timestamp
) implements DomainEvent {
    @Override
    public String getEventType() {
        return "COURSE_ENROLLMENT";
    }

    @Override
    public UUID getAggregateId() {
        return enrollmentId;
    }

    @Override
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
}
