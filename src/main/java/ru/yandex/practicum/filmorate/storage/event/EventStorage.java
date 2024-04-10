package ru.yandex.practicum.filmorate.storage.event;

import ru.yandex.practicum.filmorate.model.event.Event;
import ru.yandex.practicum.filmorate.model.event.EventOperation;
import ru.yandex.practicum.filmorate.model.event.EventType;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public interface EventStorage {

    void addEvent(EventType eventType,
                  EventOperation operation,
                  Integer userId,
                  Integer entityId);

    List<Event> getEventFeed(Integer userId);

    Event mapRowToEvent(ResultSet resultSet) throws SQLException;
}
