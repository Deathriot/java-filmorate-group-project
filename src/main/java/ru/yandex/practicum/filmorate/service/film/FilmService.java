package ru.yandex.practicum.filmorate.service.film;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.List;

public interface FilmService {

    Collection<Film> getAllFilms();

    Film getFilmById(Integer id);

    Film addFilm(Film film);

    void deleteFilm(Integer id);

    Film updateFilm(Film film);

    void addScore(Integer id, Integer userId, Integer score);

    void deleteScore(Integer id, Integer userId);

    Collection<Film> getPopular(Integer count);

    List<Film> getByDirector(Integer id, String sortBy);

    Collection<Film> getPopular(Integer count, Integer genreId, String year);

    Collection<Film> findCommonFilms(Integer userId, Integer friendId);

    Collection<Film> getFilmsBy(String query, String by);
}
