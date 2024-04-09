package ru.yandex.practicum.filmorate.service.review;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.review.ReviewStorage;

import java.util.Collection;

@Service
@AllArgsConstructor
public class ReviewServiceImpl implements ReviewService {
    private final ReviewStorage storage;

    @Override
    public Review createReview(Review review) {
        return storage.createReview(review);
    }

    @Override
    public Review updateReview(Review review) {
        return storage.updateReview(review);
    }

    @Override
    public Review getReviewById(Integer id) {
        return storage.getReviewById(id);
    }

    @Override
    public Collection<Review> getAllReviewsOfFilm(Integer filmId, Integer count) {
        if (filmId == null) {
            return storage.getAllReviews(count);
        }

        return storage.getAllReviewsOfFilm(filmId, count);
    }

    @Override
    public void deleteReview(Integer id) {
        storage.deleteReview(id);
    }

    @Override
    public void putLike(Integer id, Integer userId) {
        storage.addRate(id, userId, true);

        Review review = storage.getReviewById(id);
        Integer useful = review.getUseful();
        useful++;

        storage.changeReviewUseful(id, useful);
    }

    @Override
    public void putDislike(Integer id, Integer userId) {
        storage.addRate(id, userId, false);

        Review review = storage.getReviewById(id);
        Integer useful = review.getUseful();
        useful--;

        storage.changeReviewUseful(id, useful);
    }

    @Override
    public void deleteLike(Integer id, Integer userId) {
        storage.deleteRate(id, userId, true);

        Review review = storage.getReviewById(id);
        Integer useful = review.getUseful();
        useful--;

        storage.changeReviewUseful(id, useful);
    }

    @Override
    public void deleteDislike(Integer id, Integer userId) {
        storage.deleteRate(id, userId, false);

        Review review = storage.getReviewById(id);
        Integer useful = review.getUseful();
        useful++;

        storage.changeReviewUseful(id, useful);
    }
}
