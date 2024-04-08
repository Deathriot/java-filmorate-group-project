package ru.yandex.practicum.filmorate.service.user;

import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.model.event.Event;

import java.util.Collection;

public interface UserService {
    Collection<User> getALlUsers();

    User getUserById(Integer id);

    User addUser(User user);

    void deleteUser(Integer id);

    User updateUser(User user);

    void addFriend(Integer id, Integer friendId);

    void deleteFriend(Integer id, Integer friendId);

    Collection<User> getFriends(Integer id);

    Collection<User> getCommonFriends(Integer id, Integer otherId);

    Collection<Film> getUserRecommendations(Integer userId);

    Collection<Event> getEventFeed(Integer userId);
}
