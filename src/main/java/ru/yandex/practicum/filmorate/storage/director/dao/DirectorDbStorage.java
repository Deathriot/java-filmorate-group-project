package ru.yandex.practicum.filmorate.storage.director.dao;

import lombok.AllArgsConstructor;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@AllArgsConstructor
public class DirectorDbStorage implements DirectorStorage {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public Director createDirector(Director director) {
        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("DIRECTOR")
                .usingGeneratedKeyColumns("DIRECTOR_ID");
        director.setId(simpleJdbcInsert.executeAndReturnKey(directorToMap(director)).intValue());
        log.info("Director was created with id:{}", director.getId());
        return getDirectorById(director.getId());
    }

    @Override
    public List<Director> getDirectors() {
        SqlRowSet directorRows = jdbcTemplate.queryForRowSet("SELECT * FROM DIRECTOR");
        return directorParsing(directorRows);
    }

    @Override
    public Director getDirectorById(Integer id) {
        SqlRowSet directorRowsSet = jdbcTemplate.queryForRowSet("SELECT * FROM DIRECTOR WHERE DIRECTOR_ID = ?", id);
        List<Director> director = directorParsing(directorRowsSet);
        if (director.size() == 1) {
            return director.get(0);
        } else {
            log.info("Can not find director id:{}.", id);
            throw new NotFoundException("Can not find director id:{}." + id);
        }
    }

    @Override
    public Director updateDirector(Director director) {
        String sqlQuery = "UPDATE DIRECTOR SET " +
                " NAME = ? " +
                " WHERE DIRECTOR_ID = ?";
        int linesChanged = jdbcTemplate.update(sqlQuery, director.getName(), director.getId());
        if (linesChanged > 0) {
            log.info("Director id:{} name:{} updated", director.getId(), director.getName());
        } else {
            throw new NotFoundException("Can not find director id:" + director.getId());
        }
        return getDirectorById(director.getId());
    }

    @Override
    public void deleteDirectorById(Integer id) {
        String sqlDeletePairs = "DELETE FROM FILM_DIRECTOR WHERE DIRECTOR_ID = ?";
        jdbcTemplate.update(sqlDeletePairs, id);
        String sqlDeleteDirector = "DELETE FROM DIRECTOR WHERE DIRECTOR_ID = ?";
        int linesDeleted = jdbcTemplate.update(sqlDeleteDirector, id);
        if (linesDeleted == 0)
            throw new NotFoundException("Can not find director id:" + id);
    }


    @Override
    public List<Director> existDirector(List<Integer> directorToInt) {
        NamedParameterJdbcTemplate namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
        String sqlQuery = "SELECT DIRECTOR_ID, NAME FROM DIRECTOR WHERE DIRECTOR.DIRECTOR_ID IN (:values)";
        MapSqlParameterSource mapSqlParameterSource = new MapSqlParameterSource("values", directorToInt);

        return namedParameterJdbcTemplate.query(
                sqlQuery, mapSqlParameterSource,
                (rs, rowNow) -> Director.builder()
                        .id(rs.getInt("DIRECTOR_ID"))
                        .name(rs.getString("NAME"))
                        .build());
    }

    @Override
    public List<Director> directorParsing(SqlRowSet dirRows) {
        List<Director> directors = new ArrayList<>();
        while (dirRows.next()) {
            Director director = Director.builder()
                    .id(dirRows.getInt("DIRECTOR_ID"))
                    .name(dirRows.getString("NAME"))
                    .build();
            directors.add(director);
        }
        return directors;
    }

    private Map<String, Object> directorToMap(Director director) {
        Map<String, Object> values = new HashMap<>();
        values.put("NAME", director.getName());
        return values;
    }
}
