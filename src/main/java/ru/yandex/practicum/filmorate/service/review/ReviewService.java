package ru.yandex.practicum.filmorate.service.review;

import ru.yandex.practicum.filmorate.model.Review;

import java.util.Collection;

public interface ReviewService {
    Review createReview(Review review);

    Review updateReview(Review review);

    Review getReviewById(Integer id);

    Collection<Review> getAllReviewsOfFilm(Integer filmId, Integer count);

    void deleteReview(Integer id);

    void putLike(Integer id, Integer userId);

    void putDislike(Integer id, Integer userId);

    void deleteLike(Integer id, Integer userId);

    void deleteDislike(Integer id, Integer userId);
}