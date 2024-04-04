package ru.yandex.practicum.filmorate.storage.user;

import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.model.event.Event;

import java.util.Collection;

public interface UserStorage {
    User addUser(User user);

    void deleteUser(Integer id);

    User getUserById(Integer id);

    User updateUser(User user);

    Collection<User> getAllUsers();

    void checkUserExist(Integer id);

    void addFriend(Integer id, Integer friendId);

    Collection<User> getFriends(Integer id);

    void deleteFriend(Integer id, Integer friendId);

    Collection<User> getCommonFriends(Integer id, Integer otherId);

    Collection<Event> getEventFeed(Integer userId);
}
