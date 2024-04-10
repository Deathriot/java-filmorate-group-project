package ru.yandex.practicum.filmorate.service.review;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.model.event.EventOperation;
import ru.yandex.practicum.filmorate.model.event.EventType;
import ru.yandex.practicum.filmorate.storage.event.EventStorage;
import ru.yandex.practicum.filmorate.storage.review.ReviewStorage;

import java.util.Collection;

@Service
@AllArgsConstructor
public class ReviewServiceImpl implements ReviewService {
    private final ReviewStorage storage;
    private final EventStorage eventStorage;

    @Override
    public Review createReview(Review review) {
        Review review1 = storage.createReview(review);
        eventStorage.addEvent(EventType.REVIEW, EventOperation.ADD, review1.getUserId(), review1.getReviewId());
        return review1;
    }

    @Override
    public Review updateReview(Review review) {
        Review review1 = storage.updateReview(review);
        eventStorage.addEvent(EventType.REVIEW, EventOperation.UPDATE, review1.getUserId(), review1.getReviewId());
        return review1;
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
        eventStorage.addEvent(EventType.REVIEW, EventOperation.REMOVE, getReviewById(id).getUserId(), id);
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
