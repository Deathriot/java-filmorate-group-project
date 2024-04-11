package ru.yandex.practicum.filmorate.service.film;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.IncorrectParameterException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.event.EventOperation;
import ru.yandex.practicum.filmorate.model.event.EventType;
import ru.yandex.practicum.filmorate.storage.director.DirectorStorage;
import ru.yandex.practicum.filmorate.storage.event.EventStorage;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@Slf4j
@AllArgsConstructor
public class FilmServiceImpl implements FilmService {

    private final FilmStorage filmStorage;
    private final DirectorStorage directorStorage;
    private final EventStorage eventStorage;

    @Override
    public Collection<Film> getAllFilms() {
        return filmStorage.getAllFilms();
    }

    @Override
    public Film getFilmById(Integer id) {
        return filmStorage.getFilmById(id);
    }

    @Override
    public Collection<Film> getPopular(Integer count) {
        return filmStorage.getPopular(count);
    }

    @Override
    public Collection<Film> getPopular(Integer count, Integer genreId, String year) {
        if (year == null) {
            return filmStorage.getPopularByGenre(count, genreId);
        }

        if (genreId == null) {
            return filmStorage.getPopularByYear(count, year);
        }

        return filmStorage.getPopularByGenreAndYear(count, genreId, year);
    }

    @Override
    public Film addFilm(Film film) {
        setDirectors(film);
        return filmStorage.addFilm(film);
    }

    @Override
    public Film updateFilm(Film film) {
        setDirectors(film);
        return filmStorage.updateFilm(film);
    }

    @Override
    public void deleteFilm(Integer id) {
        filmStorage.deleteFilm(id);
    }

    @Override
    public void addLike(Integer id, Integer userId) {
        filmStorage.addLike(id, userId);
        eventStorage.addEvent(EventType.LIKE, EventOperation.ADD, userId, id);
    }

    @Override
    public void deleteLike(Integer id, Integer userId) {
        filmStorage.deleteLike(id, userId);
        eventStorage.addEvent(EventType.LIKE, EventOperation.REMOVE, userId, id);
    }

    private void setDirectors(Film film) {
        try {
            if (film.getDirectors() != null) {
                List<Integer> directorIds = new ArrayList<>();
                for (Director director : film.getDirectors()) {
                    directorIds.add(director.getId());
                }
                List<Director> directors = directorStorage.existDirector(directorIds);
                if (directorIds.size() != directors.size())
                    throw new NoSuchElementException("Wrong director id received.");
                film.setDirectors(directors);
            }
        } catch (NoSuchElementException e) {
            throw new IllegalArgumentException("Film - " + film.getName() + " not created." + e.getMessage());
        }
    }

    @Override
    public List<Film> getByDirector(Integer id, String sortBy) {
        directorStorage.getDirectorById(id);
        if (sortBy.equals("year")) {
            return filmStorage.getByDirectorSortByYear(id);
        }
        if (sortBy.equals("likes")) {
            return filmStorage.getByDirectorSortByLikes(id);
        }

        throw new IllegalArgumentException("Wrong sort format");
    }

    @Override
    public Collection<Film> findCommonFilms(Integer userId, Integer friendId) {
        return filmStorage.findCommonFilms(userId, friendId);
    }

    @Override
    public Collection<Film> getFilmsBy(String query, String by) {
        switch (by.toLowerCase().strip()) {
            case "director":
                return filmStorage.findFilmsByDirector(query);
            case "title":
                return filmStorage.findFilmsByTitle(query);
            case "title,director":
            case "director,title":
                return filmStorage.findFilmsByDirectorAndTitle(query);
            default:
                throw new IncorrectParameterException("Incorrect params. query:" + query + " by:" + by);
        }
    }
}
