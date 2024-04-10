package ru.yandex.practicum.filmorate.storage.event.dao;

import lombok.AllArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.event.Event;
import ru.yandex.practicum.filmorate.model.event.EventOperation;
import ru.yandex.practicum.filmorate.model.event.EventType;
import ru.yandex.practicum.filmorate.storage.event.EventStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.List;
import java.util.Map;

@Component
@AllArgsConstructor
public class EventDbStorage implements EventStorage {

    private final JdbcTemplate jdbcTemplate;
    private final UserStorage userStorage;

    @Override
    public void addEvent(EventType eventType,
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

    @Override
    public List<Event> getEventFeed(Integer userId) {
        userStorage.checkUserExist(userId);
        String sqlQuery = "SELECT * FROM FEEDS WHERE USER_ID=?;";
        return jdbcTemplate.query(sqlQuery, (rs, rowNum) -> mapRowToEvent(rs), userId);
    }

    @Override
    public Event mapRowToEvent(ResultSet resultSet) throws SQLException {
        return Event.builder()
                .eventId(resultSet.getInt("EVENT_ID"))
                .eventType(EventType.valueOf(resultSet.getString("EVENT_TYPE")))
                .operation(EventOperation.valueOf(resultSet.getString("OPERATION")))
                .userId(resultSet.getInt("USER_ID"))
                .entityId(resultSet.getInt("ENTITY_ID"))
                .timestamp(resultSet.getTimestamp("EVENT_TIMESTAMP").getTime())
                .build();
    }

    private Map<String, Object> toMap(Event event) {
        return Map.of(
                "EVENT_TYPE", event.getEventType(),
                "OPERATION", event.getOperation(),
                "USER_ID", event.getUserId(),
                "ENTITY_ID", event.getEntityId(),
                "EVENT_TIMESTAMP", Instant.ofEpochSecond(event.getTimestamp()));
    }
}
