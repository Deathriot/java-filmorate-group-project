package ru.yandex.practicum.filmorate.storage.review;

import ru.yandex.practicum.filmorate.model.Review;

import java.util.Collection;

public interface ReviewStorage {
    Review createReview(Review review);

    Review updateReview(Review review);

    Review getReviewById(Integer reviewId);

    Collection<Review> getAllReviews(Integer count);

    Collection<Review> getAllReviewsOfFilm(Integer filmId, Integer count);

    void deleteReview(Integer reviewId);

    void deleteRate(Integer reviewId, Integer userId, boolean isPositive);

    void addRate(Integer reviewId, Integer userId, boolean isPositive);

    void changeReviewUseful(Integer reviewId, Integer useful);
}