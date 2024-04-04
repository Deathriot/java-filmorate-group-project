package ru.yandex.practicum.filmorate.service.film;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.util.Collection;

@Service
@RequiredArgsConstructor
public class FilmServiceImpl implements FilmService {

    private final FilmStorage filmStorage;

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
        } else if (genreId == null) {
            return filmStorage.getPopularByYear(count, year);
        } else {
            return filmStorage.getPopularByGenreAndYear(count, genreId, year);
        }
    }

    @Override
    public Film addFilm(Film film) {
        return filmStorage.addFilm(film);
    }

    @Override
    public Film updateFilm(Film film) {
        return filmStorage.updateFilm(film);
    }

    @Override
    public void deleteFilm(Integer id) {
        filmStorage.deleteFilm(id);
    }

    @Override
    public void addLike(Integer id, Integer userId) {
        filmStorage.addLike(id, userId);
    }

    @Override
    public void deleteLike(Integer id, Integer userId) {
        filmStorage.deleteLike(id, userId);
    }
}
