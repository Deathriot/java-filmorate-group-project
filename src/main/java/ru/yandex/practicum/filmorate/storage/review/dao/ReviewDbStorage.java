package ru.yandex.practicum.filmorate.storage.review.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
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
import java.util.*;

@Component
@Primary
public class ReviewDbStorage implements ReviewStorage {
    private final JdbcTemplate jdbcTemplate;
    private final UserStorage userStorage;
    private final FilmStorage filmStorage;

    @Autowired
    public ReviewDbStorage(JdbcTemplate jdbcTemplate, UserStorage userStorage, FilmStorage filmStorage) {
        this.jdbcTemplate = jdbcTemplate;
        this.userStorage = userStorage;
        this.filmStorage = filmStorage;
    }

    @Override
    public Review create(Review review) {
        userStorage.checkUserExist(review.getUserId());
        filmStorage.checkFilmExist(review.getFilmId());

        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("REVIEWS")
                .usingGeneratedKeyColumns("REVIEW_ID");

        return get(simpleJdbcInsert.executeAndReturnKey(toMap(review)).intValue());
    }

    @Override
    public Review update(Review review) {
        checkReviewExist(review.getReviewId());

        // Для тестов (хотят именно 400, а не 404)
        try {
            userStorage.checkUserExist(review.getUserId());
            filmStorage.checkFilmExist(review.getFilmId());
        } catch (NotFoundException ex) {
            throw new IncorrectParameterException(ex.getMessage());
        }

        String sql = "UPDATE REVIEWS SET CONTENT=?, IS_POSITIVE=? WHERE REVIEW_ID=?;";

        jdbcTemplate.update(sql,
                review.getContent(),
                review.getIsPositive(),
                review.getReviewId());

        return get(review.getReviewId());
    }

    @Override
    public Review get(Integer id) {
        checkReviewExist(id);
        String sql = "SELECT * FROM REVIEWS WHERE REVIEW_ID=?;";

        return jdbcTemplate.queryForObject(sql, (rs, num) -> makeReview(rs), id);
    }

    @Override
    public Collection<Review> getAll(Integer filmId, Integer count) {
        String sql;

        if (filmId == null) {
            sql = "SELECT * FROM REVIEWS LIMIT ?;";
        } else {
            try {
                filmStorage.checkFilmExist(filmId);
            } catch (NotFoundException ex) {
                throw new IncorrectParameterException(ex.getMessage());
            }

            sql = "SELECT * FROM REVIEWS WHERE FILM_ID = " + filmId + " LIMIT ?;";
        }

        // Сортировка на уровне sql запроса почему-то не работает
        List<Review> reviewList = jdbcTemplate.query(sql, (rs, num) -> makeReview(rs), count);
        reviewList.sort(Comparator.comparingInt(Review::getUseful).reversed());
        return reviewList;
    }

    @Override
    public void deleteReview(Integer id) {
        checkReviewExist(id);

        jdbcTemplate.update("DELETE FROM REVIEWS WHERE REVIEW_ID=?", id);
    }

    @Override
    public void putLike(Integer id, Integer userId) {
        changeRate(id, userId, true, false);
    }

    @Override
    public void putDislike(Integer id, Integer userId) {
        changeRate(id, userId, false, false);
    }

    @Override
    public void deleteLike(Integer id, Integer userId) {
        changeRate(id, userId, true, true);
    }

    @Override
    public void deleteDislike(Integer id, Integer userId) {
        changeRate(id, userId, false, true);
    }

    // 4 метода уж слишком однообразные, нужно их как-то сократить, например вот так..
    private void changeRate(Integer reviewId, Integer userId, boolean isPositive, boolean isDelete) {
        checkReviewExist(reviewId);
        try {
            userStorage.checkUserExist(userId);
        } catch (NotFoundException ex) {
            throw new IncorrectParameterException(ex.getMessage());
        }

        String sql = "SELECT IS_POSITIVE FROM USER_REVIEW_RATE WHERE REVIEW_ID=? AND USER_ID=?";
        SqlRowSet userRate = jdbcTemplate.queryForRowSet(sql, reviewId, userId);

        String sqlReviewUseful = "SELECT USEFUL FROM REVIEWS WHERE REVIEW_ID=?";
        Integer useful = jdbcTemplate.queryForObject(sqlReviewUseful, (rs, num) -> rs.getInt("USEFUL"), reviewId);

        if (!isDelete) {
            if (userRate.next())
                throw new IncorrectParameterException("Отзыву id = " + reviewId +
                        ", пользователь id=" + userId + " уже ставил оценку");

            if (isPositive) {
                useful++;
            } else {
                useful--;
            }

            String sqlUpdateUserLikes = "INSERT INTO USER_REVIEW_RATE (USER_ID, REVIEW_ID, IS_POSITIVE) VALUES(?, ?, ?);";
            jdbcTemplate.update(sqlUpdateUserLikes, userId, reviewId, isPositive);

        } else {
            if (!userRate.next())
                throw new IncorrectParameterException("Отзыву id = " + reviewId +
                        ", пользователь id=" + userId + " не ставил оценку");

            if (isPositive != userRate.getBoolean("IS_POSITIVE"))
                throw new IncorrectParameterException("Отзыву id = " + reviewId +
                        ", пользователь id=" + userId + " не ставил такую оценку");

            if (isPositive) {
                useful--;
            } else {
                useful++;
            }

            String sqlDelete = "DELETE FROM USER_REVIEW_RATE WHERE USER_ID=? AND REVIEW_ID=?;";
            jdbcTemplate.update(sqlDelete, userId, reviewId);
        }

        String sqlUsefulUpdate = "UPDATE REVIEWS SET USEFUL=? WHERE REVIEW_ID=?";
        jdbcTemplate.update(sqlUsefulUpdate, useful, reviewId);
    }

    private void checkReviewExist(Integer reviewId) {
        String sql = "SELECT * FROM REVIEWS WHERE REVIEW_ID=?";
        SqlRowSet sqlRowSet = jdbcTemplate.queryForRowSet(sql, reviewId);

        if (!sqlRowSet.next()) {
            throw new NotFoundException("Отзыва с id = " + reviewId + " не существует");
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