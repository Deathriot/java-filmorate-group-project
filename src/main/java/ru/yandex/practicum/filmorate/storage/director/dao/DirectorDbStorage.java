package ru.yandex.practicum.filmorate.storage.director.dao;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.storage.director.DirectorStorage;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

@Slf4j
@Component
public class DirectorDbStorage implements DirectorStorage {
    private final JdbcTemplate jdbcTemplate;

    public DirectorDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }
    @Override
    public Director createDirector(Director director) {
        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("DIRECTOR")
                .usingGeneratedKeyColumns("DIRECTOR_ID");
        director.setId(simpleJdbcInsert.executeAndReturnKey(director.directorToMap()).intValue());
        log.info("В базе создан режиссёр с id {}", director.getId());
        return director;
    }

    @Override
    public List<Director> getDirectors() {
        SqlRowSet directorRows = jdbcTemplate.queryForRowSet("SELECT * FROM DIRECTOR");
        return directorParsing(directorRows);
    }

    @Override
    public Director getDirectorById(int id) {
        SqlRowSet directorRowsSet = jdbcTemplate.queryForRowSet("SELECT * FROM DIRECTOR WHERE DIRECTOR_ID = ?", id);
        List<Director> director = directorParsing(directorRowsSet);
        if (director.size() == 1) {
            return director.get(0);
        } else {
            log.info("Режиссёр с id {} не найден.", id);
            throw new NotFoundException("Режиссёр с id " + id + " не найден.");
        }
    }

    @Override
    public Director updateDirector(Director director) {
        String sqlQuery = "UPDATE DIRECTOR SET " +
                " NAME = ? " +
                " WHERE DIRECTOR_ID = ?";
        int linesChanged = jdbcTemplate.update(sqlQuery, director.getName(), director.getId());
        if (linesChanged > 0) {
            log.info("Обновлены данные режиссёра с id {} и именем {}", director.getId(), director.getName());
        } else {
            throw new NotFoundException("Режиссёр с id " + director.getId() + " не найден.");
        }
        return director;
    }

    @Override
    public void deleteDirectorById(int id) {
        String sqlDeletePairs = "DELETE FROM FILM_DIRECTOR WHERE DIRECTOR_ID = ?";
        jdbcTemplate.update(sqlDeletePairs, id);
        String sqlDeleteDirector = "DELETE FROM DIRECTOR WHERE DIRECTOR_ID = ?";
        int linesDeleted = jdbcTemplate.update(sqlDeleteDirector, id);
        if (linesDeleted == 0)
            throw new NoSuchElementException("Режиссёр с id " + id + "не найден.");
    }


    @Override
    public List<Director> existDirector(List<Integer> directorToInt) {
        NamedParameterJdbcTemplate namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
        String sqlQuery = "SELECT DIRECTOR_ID, NAME FROM DIRECTOR WHERE DIRECTOR.DIRECTOR_ID IN (:values)";
        MapSqlParameterSource mapSqlParameterSource = new MapSqlParameterSource("values", directorToInt);

        return namedParameterJdbcTemplate.query(
                sqlQuery, mapSqlParameterSource,
                (rs, rowNow) -> new Director(rs.getInt("DIRECTOR_ID"), rs.getString("NAME")));
    }


    @Override
    public List<Director> directorParsing(SqlRowSet dirRows) {
        List<Director> directors = new ArrayList<>();
        while (dirRows.next()) {
            Director director = new Director(
                    dirRows.getInt("DIRECTOR_ID"),
                    dirRows.getString("NAME"));
            directors.add(director);
        }
        return directors;
    }
}
