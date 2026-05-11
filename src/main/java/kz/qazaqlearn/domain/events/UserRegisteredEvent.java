package kz.qazaqlearn.domain.events;

import java.time.LocalDateTime;
import java.util.UUID;

public record UserRegisteredEvent(
    UUID userId,
    String email,
    String fullName,
    String role,
    LocalDateTime timestamp
) implements DomainEvent {
    @Override
    public String getEventType() {
        return "USER_REGISTERED";
    }

    @Override
    public UUID getAggregateId() {
        return userId;
    }

    @Override
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
}
