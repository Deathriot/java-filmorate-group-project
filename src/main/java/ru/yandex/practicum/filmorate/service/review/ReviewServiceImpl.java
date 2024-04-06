package ru.yandex.practicum.filmorate.service.review;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.review.ReviewStorage;

import java.util.Collection;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {
    private final ReviewStorage storage;

    @Override
    public Review create(Review review) {
        return storage.create(review);
    }

    @Override
    public Review update(Review review) {
        return storage.update(review);
    }

    @Override
    public Review get(Integer id) {
        return storage.get(id);
    }

    @Override
    public Collection<Review> getAll(Integer filmId, Integer count) {
        return storage.getAll(filmId, count);
    }

    @Override
    public void deleteReview(Integer id) {
        storage.deleteReview(id);
    }

    @Override
    public void putLike(Integer id, Integer userId) {
        storage.putLike(id, userId);
    }

    @Override
    public void putDislike(Integer id, Integer userId) {
        storage.putDislike(id, userId);
    }

    @Override
    public void deleteLike(Integer id, Integer userId) {
        storage.deleteLike(id, userId);
    }

    @Override
    public void deleteDislike(Integer id, Integer userId) {
        storage.deleteDislike(id, userId);
    }
}
