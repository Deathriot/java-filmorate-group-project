package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.List;

public interface FilmStorage {

    Film addFilm(Film film);

    void deleteFilm(Integer id);

    Film updateFilm(Film film);

    Film getFilmById(Integer filmId);

    Collection<Film> getAllFilms();

    void addLike(Integer id, Integer userId);

    void deleteLike(Integer id, Integer userId);

    Collection<Film> getPopular(Integer count);

    Collection<Film> getPopularByGenre(Integer count, Integer genreId);

    Collection<Film> getPopularByYear(Integer count, String year);

    Collection<Film> getPopularByGenreAndYear(Integer count, Integer genreId, String year);

    void checkFilmExist(Integer id);

    List<Film> getByDirectorSortByLikes(int id);

    Collection<Film> commonFilms(Integer userId, Integer friendId);

    List<Film> getByDirectorSortByYear(int id);

    Collection<Film> getUserRecommendations(Integer userId);
}