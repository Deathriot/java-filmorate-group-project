package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.yandex.practicum.filmorate.exception.IncorrectParameterException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.review.ReviewStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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
    public void create() {
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
    public void createReviewTest() {
        storage.create(review);
        review = storage.get(1);

        Review review1 = buildReview();
        review1.setUseful(0);
        review1.setReviewId(1);

        assertEquals(review, review1);
    }

    @Test
    public void createFailureReviewTest() {
        review.setFilmId(3);
        assertThrows(NotFoundException.class, () -> storage.create(review));

        review.setFilmId(1);
        review.setUserId(100);
        assertThrows(NotFoundException.class, () -> storage.create(review));
    }

    @Test
    public void updateReviewTest() {
        review = storage.create(review);
        review.setContent("i change my mind");
        review.setIsPositive(true);

        Review review1 = storage.update(review);

        assertEquals(review, review1);
    }

    @Test
    public void updateFailureTest() {
        review = storage.create(review);
        review.setFilmId(33);

        assertThrows(IncorrectParameterException.class, () -> storage.update(review));
    }

    @Test
    public void putLikeTest() {
        review = storage.create(review);
        storage.putLike(1, 1);

        review = storage.get(1);
        assertEquals(review.getUseful(), 1);

        assertThrows(IncorrectParameterException.class, () -> storage.deleteDislike(1, 1));
        assertThrows(IncorrectParameterException.class, () -> storage.putLike(1, 1));
        assertThrows(IncorrectParameterException.class, () -> storage.putDislike(1, 1));
    }

    @Test
    public void putDisLikeTest() {
        review = storage.create(review);
        storage.putDislike(1, 1);

        review = storage.get(1);
        assertEquals(review.getUseful(), -1);
        assertThrows(IncorrectParameterException.class, () -> storage.deleteLike(1, 1));
        assertThrows(IncorrectParameterException.class, () -> storage.putDislike(1, 1));
        assertThrows(IncorrectParameterException.class, () -> storage.putLike(1, 1));
    }

    @Test
    public void deleteLikeTest() {
        review = storage.create(review);
        storage.putLike(1, 1);
        storage.deleteLike(1, 1);

        review = storage.get(1);
        assertEquals(review.getUseful(), 0);

        assertThrows(IncorrectParameterException.class, () -> storage.deleteLike(1, 1));
        assertThrows(IncorrectParameterException.class, () -> storage.deleteDislike(1, 1));
    }

    @Test
    public void deleteDislikeTest() {
        review = storage.create(review);
        storage.putDislike(1, 1);
        storage.deleteDislike(1, 1);

        review = storage.get(1);
        assertEquals(review.getUseful(), 0);

        assertThrows(IncorrectParameterException.class, () -> storage.deleteLike(1, 1));
        assertThrows(IncorrectParameterException.class, () -> storage.deleteDislike(1, 1));
    }

    @Test
    public void getAllTest() {
        Review review1 = storage.create(review); //1
        Review review2 = storage.create(review); //3
        Review review3 = storage.create(review); //2

        storage.putLike(1, 1);
        storage.putDislike(2, 1);
        review1 = storage.get(review1.getReviewId());
        review2 = storage.get(review2.getReviewId());

        Collection<Review> reviews = storage.getAll(1, 10);
        List<Review> reviewList = new ArrayList<>(reviews);

        assertEquals(reviewList.size(), 3);
        assertEquals(reviewList.get(0), review1);
        assertEquals(reviewList.get(1), review3);
        assertEquals(reviewList.get(2), review2);
    }

    @Test
    public void deleteReviewTest() {
        storage.create(review);
        storage.create(review);

        storage.deleteReview(1);
        storage.deleteReview(2);

        assertThrows(NotFoundException.class, () -> storage.get(1));
        assertEquals(storage.getAll(null, 100).size(), 0);
    }

    private Review buildReview() {
        return Review.builder()
                .content("Dis film is shieeet")
                .userId(1)
                .filmId(1)
                .isPositive(false)
                .build();
    }
}
