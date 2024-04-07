package ru.yandex.practicum.filmorate.service.film;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;

public interface FilmService {

    Collection<Film> getAllFilms();

    Film getFilmById(Integer id);

    Film addFilm(Film film);

    void deleteFilm(Integer id);

    Film updateFilm(Film film);

    void addLike(Integer id, Integer userId);

    void deleteLike(Integer id, Integer userId);

    Collection<Film> getPopular(Integer count);

    Collection<Film> getPopular(Integer count, Integer genreId, String year);
}
