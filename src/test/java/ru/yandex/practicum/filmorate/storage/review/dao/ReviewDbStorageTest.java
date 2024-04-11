package ru.yandex.practicum.filmorate.storage.review.dao;

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
import ru.yandex.practicum.filmorate.service.review.ReviewService;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
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
    private final ReviewService service;
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
        service.createReview(review);
        review = service.getReviewById(1);

        Review review1 = buildReview();
        review1.setUseful(0);
        review1.setReviewId(1);

        assertEquals(review, review1);
    }

    @Test
    public void createFailureReviewTest() {
        review.setFilmId(3);
        assertThrows(NotFoundException.class, () -> service.createReview(review));

        review.setFilmId(1);
        review.setUserId(100);
        assertThrows(NotFoundException.class, () -> service.createReview(review));
    }

    @Test
    public void updateReviewTest() {
        review = service.createReview(review);
        review.setContent("i change my mind");
        review.setIsPositive(true);

        Review review1 = service.updateReview(review);

        assertEquals(review, review1);
    }

    @Test
    public void updateFailureTest() {
        review = service.createReview(review);
        review.setFilmId(33);

        assertThrows(NotFoundException.class, () -> service.updateReview(review));
    }

    @Test
    public void putLikeTest() {
        review = service.createReview(review);
        service.putLike(1, 1);

        review = service.getReviewById(1);
        assertEquals(review.getUseful(), 1);

        assertThrows(IncorrectParameterException.class, () -> service.deleteDislike(1, 1));
        assertThrows(IncorrectParameterException.class, () -> service.putLike(1, 1));
        assertThrows(IncorrectParameterException.class, () -> service.putDislike(1, 1));
    }

    @Test
    public void putDisLikeTest() {
        review = service.createReview(review);
        service.putDislike(1, 1);

        review = service.getReviewById(1);
        assertEquals(review.getUseful(), -1);
        assertThrows(IncorrectParameterException.class, () -> service.deleteLike(1, 1));
        assertThrows(IncorrectParameterException.class, () -> service.putDislike(1, 1));
        assertThrows(IncorrectParameterException.class, () -> service.putLike(1, 1));
    }

    @Test
    public void deleteLikeTest() {
        review = service.createReview(review);
        service.putLike(1, 1);
        service.deleteLike(1, 1);

        review = service.getReviewById(1);
        assertEquals(review.getUseful(), 0);

        assertThrows(IncorrectParameterException.class, () -> service.deleteLike(1, 1));
        assertThrows(IncorrectParameterException.class, () -> service.deleteDislike(1, 1));
    }

    @Test
    public void deleteDislikeTest() {
        review = service.createReview(review);
        service.putDislike(1, 1);
        service.deleteDislike(1, 1);

        review = service.getReviewById(1);
        assertEquals(review.getUseful(), 0);

        assertThrows(IncorrectParameterException.class, () -> service.deleteLike(1, 1));
        assertThrows(IncorrectParameterException.class, () -> service.deleteDislike(1, 1));
    }

    @Test
    public void getAllTest() {
        Review review1 = service.createReview(review); //1
        Review review2 = service.createReview(review); //3
        Review review3 = service.createReview(review); //2

        service.putLike(1, 1);
        service.putDislike(2, 1);
        review1 = service.getReviewById(review1.getReviewId());
        review2 = service.getReviewById(review2.getReviewId());

        Collection<Review> reviews = service.getAllReviewsOfFilm(null, 10);
        List<Review> reviewList = new ArrayList<>(reviews);

        assertEquals(reviewList.size(), 3);
        assertEquals(reviewList.get(0), review1);
        assertEquals(reviewList.get(1), review3);
        assertEquals(reviewList.get(2), review2);
    }

    @Test
    public void deleteReviewTest() {
        service.createReview(review);
        service.createReview(review);

        service.deleteReview(1);
        service.deleteReview(2);

        assertThrows(NotFoundException.class, () -> service.getReviewById(1));
        assertEquals(service.getAllReviewsOfFilm(null, 100).size(), 0);
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
