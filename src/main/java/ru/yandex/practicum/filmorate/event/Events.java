package ru.yandex.practicum.filmorate.event;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import ru.yandex.practicum.filmorate.model.event.Event;
import ru.yandex.practicum.filmorate.model.event.EventOperation;
import ru.yandex.practicum.filmorate.model.event.EventType;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Map;

public class Events {
    public static void addEvent(JdbcTemplate jdbcTemplate,
                                EventType eventType,
                                EventOperation operation,
                                Integer userId,
                                Integer entityId) {
        Event event = Event.builder()
                .eventType(eventType)
                .operation(operation)
                .userId(userId)
                .entityId(entityId)
                .timestamp(Instant.now().getEpochSecond())
                .build();

        SimpleJdbcInsert eventInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("FEEDS")
                .usingGeneratedKeyColumns("EVENT_ID");
        eventInsert.execute(toMap(event));
    }

    public static Event mapRowToEvent(ResultSet resultSet, int rowNum) throws SQLException {
        return Event.builder()
                .eventId(resultSet.getInt("EVENT_ID"))
                .eventType(EventType.valueOf(resultSet.getString("EVENT_TYPE")))
                .operation(EventOperation.valueOf(resultSet.getString("OPERATION")))
                .userId(resultSet.getInt("USER_ID"))
                .entityId(resultSet.getInt("ENTITY_ID"))
                .timestamp(resultSet.getTimestamp("EVENT_TIMESTAMP").getTime())
                .build();
    }

    private static Map<String, Object> toMap(Event event) {
        return Map.of(
                "EVENT_TYPE", event.getEventType(),
                "OPERATION", event.getOperation(),
                "USER_ID", event.getUserId(),
                "ENTITY_ID", event.getEntityId(),
                "EVENT_TIMESTAMP", Instant.ofEpochSecond(event.getTimestamp()));
    }
}
