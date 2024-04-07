package ru.yandex.practicum.filmorate.storage.review;

import ru.yandex.practicum.filmorate.model.Review;

import java.util.Collection;

public interface ReviewStorage {
    Review create(Review review);

    Review update(Review review);

    Review get(Integer id);

    Collection<Review> getAll(Integer filmId, Integer count);

    void deleteReview(Integer id);

    void putLike(Integer id, Integer userId);

    void putDislike(Integer id, Integer userId);

    void deleteLike(Integer id, Integer userId);

    void deleteDislike(Integer id, Integer userId);
}