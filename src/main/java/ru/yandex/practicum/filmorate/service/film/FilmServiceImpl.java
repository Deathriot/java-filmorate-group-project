package ru.yandex.practicum.filmorate.service.film;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.director.DirectorStorage;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.util.Collection;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@Slf4j
@RequiredArgsConstructor
public class FilmServiceImpl implements FilmService {

    private final FilmStorage filmStorage;
    private final DirectorStorage directorStorage;

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
    }

    @Override
    public void deleteLike(Integer id, Integer userId) {
        filmStorage.deleteLike(id, userId);
    }

    private void setDirectors(Film film) {
        try {
            if (film.getDirectors() != null) {
                List<Integer> directorsInt = film.directorToInt();
                List<Director> directors = directorStorage.existDirector(directorsInt);
                if (directorsInt.size() != directors.size())
                    throw new NoSuchElementException("Переданы некорректные id режиссёров.");
                film.setDirectors(directors);
            }
        } catch (NoSuchElementException e) {
            throw new IllegalArgumentException("Фильм с названием - " + film.getName() + " не создан." + e.getMessage());
        }
    }

    @Override
    public List<Film> getByDirector(Integer id, String sortBy) {
        directorStorage.getDirectorById(id);
        if (sortBy.equals("year")) {
            return filmStorage.getByDirectorSortByYear(id);
        } else if (sortBy.equals("likes")) {
            return filmStorage.getByDirectorSortByLikes(id);
        } else {
            throw new IllegalArgumentException("Неверный формат сортировки");
        }
    }
}
