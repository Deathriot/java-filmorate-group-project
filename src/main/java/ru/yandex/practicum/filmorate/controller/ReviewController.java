package ru.yandex.practicum.filmorate.controller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.service.review.ReviewService;

import javax.validation.Valid;
import java.util.Collection;

@RestController
@RequestMapping("/reviews")
@AllArgsConstructor
@Slf4j
public class ReviewController {
    private final ReviewService reviewService;

    @PostMapping
    public Review createReview(@RequestBody @Valid Review review) {
        Review review1 = reviewService.create(review);
        log.info("createReview, id = " + review1.getReviewId());
        return review1;
    }

    @PutMapping
    public Review updateReview(@RequestBody @Valid Review review) {
        log.info("updateReview, id = " + review.getReviewId());
        return reviewService.update(review);
    }

    @DeleteMapping("/{id}")
    public void deleteReview(@PathVariable Integer id) {
        log.info("deleteReview, id = " + id);
        reviewService.deleteReview(id);
    }

    @GetMapping("/{id}")
    public Review findReview(@PathVariable Integer id) {
        log.info("findReview, id = " + id);
        return reviewService.get(id);
    }

    @GetMapping
    public Collection<Review> getReviews(@RequestParam(required = false) Integer filmId,
                                         @RequestParam(defaultValue = "10") Integer count) {
        log.info("getReviews, filmId = " + filmId + ", count = " + count);
        return reviewService.getAll(filmId, count);
    }

    @PutMapping("/{id}/like/{userId}")
    public void likeReview(@PathVariable Integer id, @PathVariable Integer userId) {
        log.info("likeReview, id = " + id + "userId = " + userId);
        reviewService.putLike(id, userId);
    }

    @PutMapping("/{id}/dislike/{userId}")
    public void dislikeReview(@PathVariable Integer id, @PathVariable Integer userId) {
        log.info("dislikeReview, id = " + id + "userId = " + userId);
        reviewService.putDislike(id, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void deleteLikeReview(@PathVariable Integer id, @PathVariable Integer userId) {
        log.info("deleteLikeReview, id = " + id + "userId = " + userId);
        reviewService.deleteLike(id, userId);
    }

    @DeleteMapping("/{id}/dislike/{userId}")
    public void deleteDislikeReview(@PathVariable Integer id, @PathVariable Integer userId) {
        log.info("deleteDislikeReview, id = " + id + "userId = " + userId);
        reviewService.deleteDislike(id, userId);
    }
}
