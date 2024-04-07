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
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

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

        if(review.getUserId() == null){
            throw new IncorrectParameterException("Не указан пользователь у отзыва");
        }

        if(review.getFilmId() == null){
            throw new IncorrectParameterException("Не указан фильм у отзыва");
        }

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

        String sql = "UPDATE REVIEWS SET CONTENT=?, IS_POSITIVE=?, USER_ID=?, FILM_ID=?, USEFUL=? WHERE REVIEW_ID=?;";

        jdbcTemplate.update(sql,
                review.getContent(),
                review.getIsPositive(),
                review.getUserId(),
                review.getFilmId(),
                review.getUseful(),
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
            sql = "SELECT * FROM REVIEWS ORDER BY USEFUL DESC LIMIT ?;";
        } else {
            try {
                filmStorage.checkFilmExist(filmId);
            } catch (NotFoundException ex) {
                throw new IncorrectParameterException(ex.getMessage());
            }

            sql = "SELECT * FROM REVIEWS WHERE FILM_ID = " + filmId + " ORDER BY USEFUL DESC LIMIT ?;";
        }

        return jdbcTemplate.query(sql, (rs, num) -> makeReview(rs), count);
    }

    @Override
    public void deleteReview(Integer id) {
        checkReviewExist(id);

        jdbcTemplate.update("DELETE FROM REVIEWS WHERE REVIEW_ID=?", id);
    }

    @Override
    public void putLike(Integer id, Integer userId) {
        if (checkRateExist(id, userId, true)) {
            throw new IncorrectParameterException("Отзыву id = " + id + ", пользователь id=" + userId + " уже ставил лайк");
        }

        String sqlReviewUseful = "SELECT USEFUL FROM REVIEWS WHERE REVIEW_ID=?";

        Integer useful = jdbcTemplate.queryForObject(sqlReviewUseful, (rs, num) -> rs.getInt("USEFUL"), id);
        useful++;

        String sqlUsefulUpdate = "UPDATE REVIEWS SET USEFUL=? WHERE REVIEW_ID=?";
        jdbcTemplate.update(sqlUsefulUpdate, useful, id);

        String sqlUpdateUserLikes = "INSERT INTO USER_REVIEW_RATE (USER_ID, REVIEW_ID, IS_POSITIVE) VALUES(?, ?, ?);";
        jdbcTemplate.update(sqlUpdateUserLikes, userId, id, true);
    }

    @Override
    public void putDislike(Integer id, Integer userId) {
        if (checkRateExist(id, userId, false)) {
            throw new IncorrectParameterException("Отзыву id = " + id + ", пользователь id=" + userId + " уже ставил дизлайк");
        }

        String sqlReviewUseful = "SELECT USEFUL FROM REVIEWS WHERE REVIEW_ID=?";

        Integer useful = jdbcTemplate.queryForObject(sqlReviewUseful, (rs, num) -> rs.getInt("USEFUL"), id);
        useful--;

        String sqlUsefulUpdate = "UPDATE REVIEWS SET USEFUL=? WHERE REVIEW_ID=?";
        jdbcTemplate.update(sqlUsefulUpdate, useful, id);

        String sqlUpdateUserLikes = "INSERT INTO USER_REVIEW_RATE (USER_ID, REVIEW_ID, IS_POSITIVE) VALUES(?, ?, ?);";
        jdbcTemplate.update(sqlUpdateUserLikes, userId, id, false);
    }

    @Override
    public void deleteLike(Integer id, Integer userId) {
        if (!checkRateExist(id, userId, true)) {
            throw new IncorrectParameterException("Отзыву id = " + id + ", пользователь id=" + userId + " не ставил лайк");
        }

        String sqlReviewUseful = "SELECT USEFUL FROM REVIEWS WHERE REVIEW_ID=?";
        Integer useful = jdbcTemplate.queryForObject(sqlReviewUseful, (rs, num) -> rs.getInt("USEFUL"), id);
        useful--;

        String sqlUsefulUpdate = "UPDATE REVIEWS SET USEFUL=? WHERE REVIEW_ID=?";
        jdbcTemplate.update(sqlUsefulUpdate, useful, id);

        String sqlDelete = "DELETE FROM USER_REVIEW_RATE WHERE USER_ID=? AND REVIEW_ID=?;";
        jdbcTemplate.update(sqlDelete, userId, id);
    }

    @Override
    public void deleteDislike(Integer id, Integer userId) {
        if (!checkRateExist(id, userId, false)) {
            throw new IncorrectParameterException("Отзыву id = " + id + ", пользователь id=" + userId + " не ставил дизлайк");
        }

        String sqlReviewUseful = "SELECT USEFUL FROM REVIEWS WHERE REVIEW_ID=?";
        Integer useful = jdbcTemplate.queryForObject(sqlReviewUseful, (rs, num) -> rs.getInt("USEFUL"), id);
        useful++;

        String sqlUsefulUpdate = "UPDATE REVIEWS SET USEFUL=? WHERE REVIEW_ID=?";
        jdbcTemplate.update(sqlUsefulUpdate, useful, id);

        String sqlDelete = "DELETE FROM USER_REVIEW_RATE WHERE USER_ID=? AND REVIEW_ID=?;";
        jdbcTemplate.update(sqlDelete, userId, id);
    }

    // Возвращает false, если у отзыва нет лайка/дизлайка или удаление оценки отзыва не соответствует тому,
    // как пользователь этот отзыв оценил (Пользователь не может удалить дизлайк, если он ставил лайк - и наоборот)
    private boolean checkRateExist(Integer reviewId, Integer userId, boolean isPositive) {
        checkReviewExist(reviewId);
        try {
            userStorage.checkUserExist(userId);
        } catch (NotFoundException ex) {
            throw new IncorrectParameterException(ex.getMessage());
        }

        String sql = "SELECT IS_POSITIVE FROM USER_REVIEW_RATE WHERE REVIEW_ID=? AND USER_ID=?";

        SqlRowSet sqlRowSet = jdbcTemplate.queryForRowSet(sql, reviewId, userId);

        if (!sqlRowSet.next()) {
            return false;
        }

        return isPositive == sqlRowSet.getBoolean("IS_POSITIVE");
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
