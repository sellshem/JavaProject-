package kz.qazaqlearn.domain.events;

import java.time.LocalDateTime;
import java.util.UUID;

public interface DomainEvent {
    LocalDateTime getTimestamp();
    String getEventType();
    UUID getAggregateId();
}
