package ru.yandex.practicum.filmorate.service.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.model.event.Event;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Collection;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

    @Override
    public Collection<User> getALlUsers() {
        return userStorage.getAllUsers();
    }

    @Override
    public User getUserById(Integer id) {
        return userStorage.getUserById(id);
    }

    @Override
    public User addUser(User user) {
        return userStorage.addUser(user);
    }

    @Override
    public void deleteUser(Integer id) {
        userStorage.deleteUser(id);
    }

    @Override
    public User updateUser(User user) {
        return userStorage.updateUser(user);
    }

    @Override
    public void addFriend(Integer id, Integer friendId) {
        userStorage.addFriend(id, friendId);
    }

    @Override
    public void deleteFriend(Integer id, Integer friendId) {
        userStorage.deleteFriend(id, friendId);
    }

    @Override
    public Collection<User> getFriends(Integer id) {
        return userStorage.getFriends(id);
    }

    @Override
    public Collection<User> getCommonFriends(Integer id, Integer otherId) {
        return userStorage.getCommonFriends(id, otherId);
    }

    @Override
    public Collection<Film> getUserRecommendations(Integer userId) {
        return filmStorage.getUserRecommendations(userId);
    }

    @Override
    public Collection<Event> getEventFeed(Integer userId) {
        return userStorage.getEventFeed(userId);
    }
}
