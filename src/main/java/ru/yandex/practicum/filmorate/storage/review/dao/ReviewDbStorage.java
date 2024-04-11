package ru.yandex.practicum.filmorate.storage.review.dao;

import lombok.AllArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.IncorrectParameterException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.review.ReviewStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Component
@AllArgsConstructor
public class ReviewDbStorage implements ReviewStorage {
    private final JdbcTemplate jdbcTemplate;
    private final UserStorage userStorage;
    private final FilmStorage filmStorage;

    @Override
    public Review createReview(Review review) {
        userStorage.checkUserExist(review.getUserId());
        filmStorage.checkFilmExist(review.getFilmId());

        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("REVIEWS")
                .usingGeneratedKeyColumns("REVIEW_ID");

        Integer reviewId = simpleJdbcInsert.executeAndReturnKey(toMap(review)).intValue();
        review.setReviewId(reviewId);
        return review;
    }

    @Override
    public Review updateReview(Review review) {
        checkReviewExist(review.getReviewId());
        userStorage.checkUserExist(review.getUserId());
        filmStorage.checkFilmExist(review.getFilmId());

        String sql = "UPDATE REVIEWS SET CONTENT=?, IS_POSITIVE=? WHERE REVIEW_ID=?;";

        jdbcTemplate.update(sql,
                review.getContent(),
                review.getIsPositive(),
                review.getReviewId());

        // Выгружать из БД нужно (написал в комментарии в ПР)
        return getReviewById(review.getReviewId());
    }

    @Override
    public Review getReviewById(Integer id) {
        checkReviewExist(id);
        String sql = "SELECT * FROM REVIEWS WHERE REVIEW_ID=?;";

        return jdbcTemplate.queryForObject(sql, (rs, num) -> makeReview(rs), id);
    }

    @Override
    public Collection<Review> getAllReviews(Integer count) {
        String sql = "SELECT * FROM REVIEWS ORDER BY USEFUL DESC FETCH FIRST ? ROWS ONLY;";

        return jdbcTemplate.query(sql, (rs, num) -> makeReview(rs), count);
    }

    @Override
    public Collection<Review> getAllReviewsOfFilm(Integer filmId, Integer count) {
        filmStorage.checkFilmExist(filmId);
        String sql = "SELECT * FROM REVIEWS WHERE FILM_ID=? ORDER BY USEFUL DESC FETCH FIRST ? ROWS ONLY;";

        return jdbcTemplate.query(sql, (rs, num) -> makeReview(rs), filmId, count);
    }

    @Override
    public void deleteReview(Integer id) {
        checkReviewExist(id);
        jdbcTemplate.update("DELETE FROM REVIEWS WHERE REVIEW_ID=?", id);
    }

    @Override
    public void deleteRate(Integer reviewId, Integer userId, boolean isPositive) {
        String sql = "SELECT IS_POSITIVE FROM USER_REVIEW_RATE WHERE REVIEW_ID=? AND USER_ID=?";
        SqlRowSet userRate = jdbcTemplate.queryForRowSet(sql, reviewId, userId);

        if (!userRate.next()) {
            throw new IncorrectParameterException("User id=" + userId + " do not rate this review id=" + reviewId);
        }

        if (isPositive != userRate.getBoolean("IS_POSITIVE")) {
            throw new IncorrectParameterException("User id=" + userId + " do not rate this review id=" + reviewId);
        }

        String sqlDelete = "DELETE FROM USER_REVIEW_RATE WHERE USER_ID=? AND REVIEW_ID=?;";
        jdbcTemplate.update(sqlDelete, userId, reviewId);
    }

    @Override
    public void addRate(Integer reviewId, Integer userId, boolean isPositive) {
        String sql = "SELECT IS_POSITIVE FROM USER_REVIEW_RATE WHERE REVIEW_ID=? AND USER_ID=?";
        SqlRowSet userRate = jdbcTemplate.queryForRowSet(sql, reviewId, userId);


        if (userRate.next()) {
            throw new IncorrectParameterException("User id=" + userId + " already rate this review id=" + reviewId);
        }

        String sqlUpdateUserLikes = "INSERT INTO USER_REVIEW_RATE (USER_ID, REVIEW_ID, IS_POSITIVE) VALUES(?, ?, ?);";
        jdbcTemplate.update(sqlUpdateUserLikes, userId, reviewId, isPositive);
    }

    //Через update изменять useful нельзя (что логично)
    @Override
    public void changeReviewUseful(Integer reviewId, Integer useful) {
        String sql = "UPDATE REVIEWS SET USEFUL=? WHERE REVIEW_ID=?;";
        jdbcTemplate.update(sql, useful, reviewId);
    }

    private void checkReviewExist(Integer reviewId) {
        String sql = "SELECT * FROM REVIEWS WHERE REVIEW_ID=?";
        SqlRowSet sqlRowSet = jdbcTemplate.queryForRowSet(sql, reviewId);

        if (!sqlRowSet.next()) {
            throw new NotFoundException("Review with id=" + reviewId + " does not exist");
        }
    }

    private Review makeReview(ResultSet rs) throws SQLException {
        return Review.builder()
                .reviewId(rs.getInt("REVIEW_ID"))
                .content(rs.getString("CONTENT"))
                .isPositive(rs.getBoolean("IS_POSITIVE"))
                .userId(rs.getInt("USER_ID"))
                .filmId(rs.getInt("FILM_ID"))
                .useful(rs.getInt("USEFUL"))
                .build();
    }

    private Map<String, Object> toMap(Review review) {
        final Map<String, Object> reviewMap = new HashMap<>();

        reviewMap.put("review_id", review.getReviewId());
        reviewMap.put("content", review.getContent());
        reviewMap.put("is_positive", review.getIsPositive());
        reviewMap.put("user_id", review.getUserId());
        reviewMap.put("film_id", review.getFilmId());
        reviewMap.put("useful", review.getUseful());

        return reviewMap;
    }
}