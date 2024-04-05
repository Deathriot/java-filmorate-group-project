package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class UserDbStorageTest {

    private final UserStorage userDbStorage;

    @Test
    public void addUserTest() {

        User newUser = createDefaultUser();
        User user = userDbStorage.addUser(newUser);

        newUser.setId(user.getId());

        compare(newUser, user);
    }

    @Test
    public void updateUserTest() {

        User newUser = createDefaultUser();
        User user = userDbStorage.addUser(newUser);

        user.setName("NewName");
        user.setEmail("newMail@mail.ru");
        user.setBirthday(LocalDate.of(2001, 1, 1));
        user.setLogin("NewLogin");
        userDbStorage.updateUser(user);

        User savedUser = userDbStorage.getUserById(user.getId());

        compare(user, savedUser);
    }

    @Test
    public void addFriendTest() {
        User user1 = userDbStorage.addUser(createDefaultUser());
        User user2 = userDbStorage.addUser(createDefaultUser());

        assertEquals(0, userDbStorage.getFriends(user1.getId()).size());
        assertEquals(0, userDbStorage.getFriends(user2.getId()).size());

        userDbStorage.addFriend(user1.getId(), user2.getId());
        List<User> friends = new ArrayList<>(userDbStorage.getFriends(user1.getId()));

        assertEquals(1, friends.size());
        assertEquals(0, userDbStorage.getFriends(user2.getId()).size());
        assertEquals(user2.getId(), friends.get(0).getId());
    }

    @Test
    public void removeFriendTest() {
        User user1 = userDbStorage.addUser(createDefaultUser());
        User user2 = userDbStorage.addUser(createDefaultUser());

        userDbStorage.addFriend(user2.getId(), user1.getId());

        assertEquals(0, userDbStorage.getFriends(user1.getId()).size());
        assertEquals(1, userDbStorage.getFriends(user2.getId()).size());

        userDbStorage.deleteFriend(user2.getId(), user1.getId());

        assertEquals(0, userDbStorage.getFriends(user1.getId()).size());
        assertEquals(0, userDbStorage.getFriends(user2.getId()).size());
    }

    @Test
    public void commonFriendsTest() {
        User user1 = userDbStorage.addUser(createDefaultUser());
        User user2 = userDbStorage.addUser(createDefaultUser());
        User user3 = userDbStorage.addUser(createDefaultUser());

        userDbStorage.addFriend(user1.getId(), user2.getId());
        userDbStorage.addFriend(user1.getId(), user3.getId());

        userDbStorage.addFriend(user2.getId(), user1.getId());
        userDbStorage.addFriend(user2.getId(), user3.getId());

        List<User> friends1 = new ArrayList<>(userDbStorage.getFriends((user1.getId())));
        Set<Integer> list1 = friends1.stream().map(User::getId).collect(Collectors.toCollection(HashSet::new));

        List<User> friends2 = new ArrayList<>(userDbStorage.getFriends((user2.getId())));
        Set<Integer> list2 = friends2.stream().map(User::getId).collect(Collectors.toCollection(HashSet::new));

        assertEquals(2, list1.size());
        assertEquals(2, list2.size());
        assertTrue(list1.contains(user3.getId()));
        assertTrue(list2.contains(user3.getId()));
    }

    @Test
    public void deleteUserTest() {
        User user1 = userDbStorage.addUser(createDefaultUser());
        User user2 = userDbStorage.addUser(createDefaultUser());

        userDbStorage.addFriend(user1.getId(), user2.getId());

        userDbStorage.addFriend(user2.getId(), user1.getId());

        List<User> friends1 = new ArrayList<>(userDbStorage.getFriends((user1.getId())));
        Set<Integer> list1 = friends1.stream().map(User::getId).collect(Collectors.toCollection(HashSet::new));

        assertEquals(1, list1.size());
        userDbStorage.deleteUser(user2.getId());

        friends1 = new ArrayList<>(userDbStorage.getFriends((user1.getId())));
        list1 = friends1.stream().map(User::getId).collect(Collectors.toCollection(HashSet::new));

        List<User> users = new ArrayList<>(userDbStorage.getAllUsers());
        Set<Integer> list2 = users.stream().map(User::getId).collect(Collectors.toCollection(HashSet::new));

        assertFalse(list2.contains(user2.getId()));
        assertEquals(0, list1.size());
        assertFalse(list1.contains(user2.getId()));
    }

    private User createDefaultUser() {
        int leftLimit = 97; // letter 'a'
        int rightLimit = 122; // letter 'z'
        int targetStringLength = 10;
        Random random = new Random();
        StringBuilder buffer = new StringBuilder(targetStringLength);
        for (int i = 0; i < targetStringLength; i++) {
            int randomLimitedInt = leftLimit + (int)
                    (random.nextFloat() * (rightLimit - leftLimit + 1));
            buffer.append((char) randomLimitedInt);
        }
        String generatedString = buffer.toString();
        return User.builder()
                .email(String.format("%s@mail.ru", generatedString))
                .login(String.format("%s@mail.ru", generatedString))
                .name("User")
                .birthday(LocalDate.of(2005, 8, 15))
                .build();
    }

    private void compare(User user1, User user2) {
        assertNotNull(user1);
        assertNotNull(user2);
        assertEquals(user1.getId(), user2.getId());
        assertEquals(user1.getLogin(), user2.getLogin());
        assertEquals(user1.getName(), user2.getName());
        assertEquals(user1.getEmail(), user2.getEmail());
        assertEquals(user1.getBirthday(), user2.getBirthday());
    }
}