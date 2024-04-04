package ru.yandex.practicum.filmorate.storage.film.dao;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.genre.GenreStorage;
import ru.yandex.practicum.filmorate.storage.genre.dao.GenreDbStorage;
import ru.yandex.practicum.filmorate.storage.mpa.MpaStorage;
import ru.yandex.practicum.filmorate.storage.mpa.dao.MpaDbStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;
import ru.yandex.practicum.filmorate.storage.user.dao.UserDbStorage;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;

@Component
@Slf4j
@Primary
public class FilmDbStorage implements FilmStorage {

    private final JdbcTemplate jdbcTemplate;
    private final UserStorage userStorage;
    private final MpaStorage mpaStorage;
    private final GenreStorage genreStorage;

    @Autowired
    public FilmDbStorage(JdbcTemplate jdbcTemplate,
                         UserDbStorage userStorage,
                         MpaDbStorage mpaStorage,
                         GenreDbStorage genreStorage) {
        this.jdbcTemplate = jdbcTemplate;
        this.userStorage = userStorage;
        this.mpaStorage = mpaStorage;
        this.genreStorage = genreStorage;
    }

    @Override
    public Film addFilm(Film film) {
        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("FILMS")
                .usingGeneratedKeyColumns("FILM_ID");

        Integer filmId = simpleJdbcInsert.executeAndReturnKey(toMap(film)).intValue();

        if (film.getGenres() != null) {
            updateGenreForFilm(filmId, film.getGenres());
        }
        Film newFilm = getFilmById(filmId);
        log.info("Film added: {}.", newFilm);
        return newFilm;
    }

    @Override
    public void deleteFilm(Integer id) {
        checkFilmExist(id);
        Film film = getFilmById(id);
        jdbcTemplate.update("DELETE FROM FILMS WHERE FILM_ID=?", id);
        log.info("film deleted. film{}.", film);
    }

    @Override
    public Film updateFilm(Film film) {
        Integer filmId = film.getId();
        checkFilmExist(filmId);
        String updateSql = "UPDATE FILMS SET TITLE=?, DESCRIPTION=?, RELEASE_DATE=?, DURATION=?, RATING=? " +
                "WHERE FILM_ID=?;";
        jdbcTemplate.update(updateSql,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                film.getMpa().getId(),
                filmId);
        updateGenreForFilm(filmId, film.getGenres());
        Film newFilm = getFilmById(filmId);
        log.info("Film updated. film{}.", newFilm);
        return newFilm;
    }

    @Override
    public Film getFilmById(Integer filmId) {
        String sql = "SELECT * FROM FILMS AS F " +
                "LEFT OUTER JOIN RATING AS R ON R.RATING_ID = F.RATING " +
                "WHERE FILM_ID=?;";
        checkFilmExist(filmId);
        Film film = jdbcTemplate.queryForObject(sql, (rs, rowNum) -> makeFilm(rs), filmId);
        log.info("Get film. film{}.", film);
        return film;
    }

    @Override
    public Collection<Film> getAllFilms() {
        String sql = "SELECT * FROM FILMS AS F " +
                "LEFT OUTER JOIN RATING AS R ON R.RATING_ID = F.RATING " +
                "ORDER BY FILM_ID;";
        Collection<Film> films = jdbcTemplate.query(sql, (rs, rowNum) -> makeFilm(rs));
        log.info("Get all films. Count of films {}.", films.size());
        return films;
    }

    @Override
    public void addLike(Integer filmId, Integer userId) {
        checkFilmExist(filmId);
        userStorage.checkUserExist(userId);
        String sql = "INSERT INTO USER_FILM (USER_ID, FILM_ID) VALUES(?, ?);";
        jdbcTemplate.update(sql, userId, filmId);
        log.info("Like added to film with id={}.", filmId);
    }

    @Override
    public void deleteLike(Integer filmId, Integer userId) {
        checkFilmExist(filmId);
        userStorage.checkUserExist(userId);
        String sql = "DELETE FROM USER_FILM WHERE FILM_ID=? AND USER_ID=?;";
        jdbcTemplate.update(sql, filmId, userId);
        log.info("Like remove.");
    }

    @Override
    public Collection<Film> getPopular(Integer count) {
        String sql = "SELECT F.*, R.*, COUNT(UF.FILM_ID) AS LIKES " +
                "FROM FILMS AS F " +
                "LEFT JOIN RATING AS R ON R.RATING_ID = F.RATING " +
                "LEFT JOIN USER_FILM AS UF ON F.FILM_ID = UF.FILM_ID " +
                "GROUP BY F.FILM_ID " +
                "ORDER BY LIKES DESC " +
                "LIMIT ?;";
        Collection<Film> films = jdbcTemplate.query(sql, (rs, rowNum) -> makeFilm(rs), count);
        log.info("Get top films: {}.", films.size());
        return films;
    }

    @Override
    public Collection<Film> getPopularByGenre(Integer count, Integer genreId) {
        String sql = "SELECT F.*,\n" +
                "R.*,\n" +
                "FG.*,\n" +
                "COUNT(UF.FILM_ID) AS LIKES\n" +
                "FROM FILMS AS F\n" +
                "LEFT JOIN RATING AS R ON R.RATING_ID = F.RATING\n" +
                "LEFT JOIN USER_FILM AS UF ON F.FILM_ID = UF.FILM_ID\n" +
                "LEFT JOIN FILM_GENRE AS FG ON F.FILM_ID = FG.FILM_ID \n" +
                "LEFT JOIN GENRE AS G ON FG.GENRE_ID = G.GENRE_ID\n" +
                "WHERE G.GENRE_ID = ?\n" +
                "GROUP BY F.FILM_ID\n" +
                "ORDER BY LIKES DESC\n" +
                "LIMIT ?";
        Collection<Film> films = jdbcTemplate.query(sql, (rs, rowNum) -> makeFilm(rs), genreId, count);
        log.info("Get top films by GENRE: {}.", films.size());
        return films;
    }

    @Override
    public Collection<Film> getPopularByYear(Integer count, String year) {
        String sql = "SELECT F.*,\n" +
                "R.*,\n" +
                "COUNT(UF.FILM_ID) AS LIKES\n" +
                "FROM FILMS AS F\n" +
                "LEFT JOIN RATING AS R ON R.RATING_ID = F.RATING\n" +
                "LEFT JOIN USER_FILM AS UF ON F.FILM_ID = UF.FILM_ID\n" +
                "WHERE EXTRACT(YEAR FROM CAST(F.RELEASE_DATE AS date)) = ?\n" +
                "GROUP BY F.FILM_ID\n" +
                "ORDER BY LIKES DESC\n" +
                "LIMIT ?";
        Collection<Film> films = jdbcTemplate.query(sql, (rs, rowNum) -> makeFilm(rs), year, count);
        log.info("Get top films by YEAR: {}.", films.size());
        return films;
    }

    @Override
    public Collection<Film> getPopularByGenreAndYear(Integer count, Integer genreId, String year) {
        String sql = "SELECT F.*,\n" +
                "R.*,\n" +
                "FG.*,\n" +
                "COUNT(UF.FILM_ID) AS LIKES\n" +
                "FROM FILMS AS F\n" +
                "LEFT JOIN RATING AS R ON R.RATING_ID = F.RATING\n" +
                "LEFT JOIN USER_FILM AS UF ON F.FILM_ID = UF.FILM_ID\n" +
                "LEFT JOIN FILM_GENRE AS FG ON F.FILM_ID = FG.FILM_ID \n" +
                "LEFT JOIN GENRE AS G ON FG.GENRE_ID = G.GENRE_ID\n" +
                "WHERE G.GENRE_ID = ? AND EXTRACT(YEAR FROM CAST(F.RELEASE_DATE AS date)) = ?\n" +
                "GROUP BY F.FILM_ID\n" +
                "ORDER BY LIKES DESC\n" +
                "LIMIT ?";
        Collection<Film> films = jdbcTemplate.query(sql, (rs, rowNum) -> makeFilm(rs), genreId, year, count);
        log.info("Get top films by GENRE and YEAR: {}.", films.size());
        return films;
    }

    @Override
    public void checkFilmExist(Integer id) {
        String sqlQuery = "SELECT COUNT(*) FROM FILMS WHERE FILM_ID=? ";
        Optional.ofNullable(jdbcTemplate.queryForObject(sqlQuery, Integer.class, id))
                .filter(count -> count == 1)
                .orElseThrow(() -> new NotFoundException(String.format("No such film with this id:%s.", id)));
    }

    private Film makeFilm(ResultSet rs) throws SQLException {
        Integer id = rs.getInt("FILM_ID");
        String sqlGenre = "SELECT * FROM GENRE WHERE GENRE_ID IN (SELECT GENRE_ID FROM FILM_GENRE WHERE FILM_ID=?);";
        List<Genre> genres = jdbcTemplate.query(sqlGenre, (result, rowNum) -> genreStorage.makeGenre(result), id);
        return Film.builder()
                .id(rs.getInt("FILM_ID"))
                .name(rs.getString("TITLE"))
                .description(rs.getString("DESCRIPTION"))
                .releaseDate(LocalDate.parse(rs.getString("RELEASE_DATE")))
                .duration(rs.getInt("DURATION"))
                .mpa(mpaStorage.makeMpa(rs))
                .genres(genres)
                .build();
    }

    private void updateGenreForFilm(Integer filmId, List<Genre> genres) {
        String sqlDelete = "DELETE FROM FILM_GENRE WHERE FILM_ID=? ";
        jdbcTemplate.update(sqlDelete, filmId);

        if (genres != null && !genres.isEmpty()) {
            String sqlInsert = "MERGE INTO FILM_GENRE (FILM_ID, GENRE_ID) VALUES (?, ?) ";
            jdbcTemplate.batchUpdate(sqlInsert, new BatchPreparedStatementSetter() {
                @Override
                public void setValues(PreparedStatement ps, int i) throws SQLException {
                    Genre genre = genres.get(i);
                    ps.setInt(1, filmId);
                    ps.setInt(2, genre.getId());
                }

                @Override
                public int getBatchSize() {
                    return genres.size();
                }
            });
        }
    }

    private Map<String, Object> toMap(Film film) {
        Map<String, Object> values = new HashMap<>();
        values.put("TITLE", film.getName());
        values.put("DESCRIPTION", film.getDescription());
        values.put("RELEASE_DATE", film.getReleaseDate());
        values.put("DURATION", film.getDuration());
        values.put("RATING", film.getMpa().getId());
        return values;
    }
}