package ru.yandex.practicum.filmorate.storage.user.dao;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.event.Events;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.model.event.Event;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;

import static ru.yandex.practicum.filmorate.model.event.EventOperation.ADD;
import static ru.yandex.practicum.filmorate.model.event.EventOperation.REMOVE;
import static ru.yandex.practicum.filmorate.model.event.EventType.FRIEND;

@Component
@Slf4j
@Primary
public class UserDbStorage implements UserStorage {

    private final JdbcTemplate jdbcTemplate;

    public UserDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public User addUser(User user) {
        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("USERS")
                .usingGeneratedKeyColumns("USER_ID");
        checkUserName(user);
        User newUser = getUserById(simpleJdbcInsert.executeAndReturnKey(toMap(user)).intValue());
        log.info("User added. user{}.", newUser);
        return newUser;
    }

    @Override
    public User updateUser(User user) {
        Integer userId = user.getId();
        checkUserExist(userId);
        checkUserName(user);
        String updateSql = "UPDATE USERS SET EMAIL=?, LOGIN=?, NAME=?, BIRTHDAY=? WHERE USER_ID=?;";
        jdbcTemplate.update(updateSql,
                user.getEmail(),
                user.getLogin(),
                user.getName(),
                user.getBirthday(),
                userId);
        User newUser = getUserById(userId);
        log.info("User updated. User{}.", newUser);
        return newUser;
    }

    @Override
    public User getUserById(Integer id) {
        String sql = "SELECT * FROM USERS WHERE USER_ID=?;";
        checkUserExist(id);
        User user = jdbcTemplate.queryForObject(sql, (rs, rowNum) -> makeUser(rs), id);
        log.info("Get user. {}.", user);
        return user;
    }

    @Override
    public Collection<User> getAllUsers() {
        String sql = "SELECT * FROM USERS;";
        Collection<User> users = jdbcTemplate.query(sql, (rs, rowNum) -> makeUser(rs));
        log.info("Get all users. Count of users {}.", users.size());
        return users;
    }

    @Override
    public void deleteUser(Integer id) {
        checkUserExist(id);
        User user = getUserById(id);
        jdbcTemplate.update("DELETE FROM USERS WHERE USER_ID=?;", id);
        log.info("User deleted. {} ", user);
    }

    @Override
    public void addFriend(Integer id, Integer friendId) {
        checkUserExist(id);
        checkUserExist(friendId);
        String sql = "INSERT INTO FRIENDS (USER_ID, FRIEND_ID) VALUES(?, ?);";
        jdbcTemplate.update(sql, id, friendId);
        Events.addEvent(jdbcTemplate, FRIEND, ADD, id, friendId);
        log.info("User{} added a friend{}.", getUserById(id), getUserById(friendId));
    }

    @Override
    public void deleteFriend(Integer id, Integer friendId) {
        checkUserExist(id);
        checkUserExist(friendId);
        User user = getUserById(id);
        User friend = getUserById(friendId);
        String sql = "DELETE FROM FRIENDS WHERE USER_ID=? AND FRIEND_ID=?;";
        jdbcTemplate.update(sql, id, friendId);
        Events.addEvent(jdbcTemplate, FRIEND, REMOVE, id, friendId);
        log.info("User{} added a friend{}.", user, friend);
    }

    @Override
    public Collection<User> getFriends(Integer id) {
        checkUserExist(id);
        String sql = "SELECT * FROM USERS WHERE USER_ID IN " +
                "(SELECT FRIEND_ID FROM FRIENDS WHERE USER_ID=?)";
        Collection<User> userFriends = jdbcTemplate.query(sql, (rs, rowNum) -> makeUser(rs), id);
        log.info("User{} get list of friends", id);
        return userFriends;
    }

    @Override
    public Collection<User> getCommonFriends(Integer id, Integer otherId) {
        checkUserExist(id);
        checkUserExist(otherId);

        String sql = "SELECT U.* " +
                "FROM USERS AS U " +
                "JOIN FRIENDS F1 ON U.USER_ID = F1.FRIEND_ID AND F1.USER_ID = ?" +
                "JOIN FRIENDS F2 ON U.USER_ID = F2.FRIEND_ID AND F2.USER_ID = ?";

        Collection<User> commonFriends = jdbcTemplate.query(sql, (rs, rowNum) -> makeUser(rs), id, otherId);

        log.info("Count of same friends of user{} with user{}: {}.", id, otherId, commonFriends.size());

        return commonFriends;
    }

    @Override
    public void checkUserExist(Integer id) {
        String sqlQuery = "SELECT COUNT(*) FROM USERS WHERE USER_ID=?";
        Optional.ofNullable(jdbcTemplate.queryForObject(sqlQuery, Integer.class, id))
                .filter(count -> count == 1)
                .orElseThrow(() -> new NotFoundException(String.format("No such user with this id:%s.", id)));
    }

    @Override
    public List<Event> getEventFeed(Integer userId) {
        checkUserExist(userId);
        String sqlQuery = "SELECT * FROM FEEDS WHERE USER_ID=?;";
        return jdbcTemplate.query(sqlQuery, Events::mapRowToEvent, userId);
    }

    private User makeUser(ResultSet rs) throws SQLException {
        return User.builder()
                .id(rs.getInt("USER_ID"))
                .email(rs.getString("EMAIL"))
                .login(rs.getString("LOGIN"))
                .name(rs.getString("NAME"))
                .birthday(LocalDate.parse(rs.getString("BIRTHDAY")))
                .build();
    }

    private void checkUserName(User user) {
        if (user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
    }

    public Map<String, Object> toMap(User user) {
        Map<String, Object> values = new HashMap<>();
        values.put("EMAIL", user.getEmail());
        values.put("LOGIN", user.getLogin());
        values.put("NAME", user.getName());
        values.put("BIRTHDAY", user.getBirthday());
        return values;
    }
}
