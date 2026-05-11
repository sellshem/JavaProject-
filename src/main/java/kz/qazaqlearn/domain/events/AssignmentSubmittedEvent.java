package kz.qazaqlearn.domain.events;

import java.time.LocalDateTime;
import java.util.UUID;

public record AssignmentSubmittedEvent(
    UUID submissionId,
    UUID assignmentId,
    UUID courseId,
    UUID studentId,
    LocalDateTime timestamp
) implements DomainEvent {
    @Override
    public String getEventType() {
        return "ASSIGNMENT_SUBMITTED";
    }

    @Override
    public UUID getAggregateId() {
        return submissionId;
    }

    @Override
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
}
