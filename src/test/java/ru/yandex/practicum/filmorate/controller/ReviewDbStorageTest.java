package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.review.ReviewStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class ReviewDbStorageTest {
    private final ReviewStorage storage;
    private final UserStorage userStorage;
    private final FilmStorage filmStorage;
    private Review review;

    @BeforeEach
    public void create(){
        User user = User.builder()
                .email("user@mail.ru")
                .login("User")
                .name("User")
                .birthday(LocalDate.of(2005, 8, 15))
                .build();

        userStorage.addUser(user);

        Film film = Film.builder()
                .name("testFilm")
                .description("testFilm")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(120).mpa(Mpa.builder()
                        .id(1)
                        .build())
                .build();

        filmStorage.addFilm(film);

        review = buildReview();
    }

    @Test
    public void createReviewTest(){
        storage.create(review);
        review = storage.get(1);

        Review review1 = buildReview();
        review1.setUseful(0);
        review1.setReviewId(1);

        assertEquals(review, review1);
    }

    @Test
    public void createFailureReviewTest(){
        review.setFilmId(3);
        assertThrows(NotFoundException.class, () -> storage.create(review));

        review.setFilmId(1);
        review.setUserId(100);
        assertThrows(NotFoundException.class, () -> storage.create(review));
    }

    @Test
    public void updateReviewTest(){
        review = storage.create(review);
        review.setContent("i change my mind");
        review.setIsPositive(true);

        Review review1 = storage.update(review);

        assertEquals(review, review1);
    }

    private Review buildReview(){
        return Review.builder()
                .content("Dis film is shieeet")
                .userId(1)
                .filmId(1)
                .isPositive(false)
                .build();
    }
}
