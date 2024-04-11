package ru.yandex.practicum.filmorate.model.event;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public final class Event {
    private Integer eventId;
    private EventType eventType;
    private EventOperation operation;
    private Integer userId;
    private Integer entityId;
    private Long timestamp;
}