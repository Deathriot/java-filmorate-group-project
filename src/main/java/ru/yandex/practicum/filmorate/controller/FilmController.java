package ru.yandex.practicum.filmorate.controller;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.film.FilmService;

import javax.validation.Valid;
import java.util.Collection;
import java.util.List;

@RestController
@RequestMapping("/films")
@AllArgsConstructor
public class FilmController {

    private final FilmService filmService;

    @GetMapping("/{id}")
    public Film findFilm(@PathVariable Integer id) {
        return filmService.getFilmById(id);
    }

    @GetMapping("/popular")
    public Collection<Film> findPopularFilms(@RequestParam(defaultValue = "10") Integer count,
                                             @RequestParam(required = false) Integer genreId,
                                             @RequestParam(required = false) String year) {
        if (genreId == null && year == null) {
            return filmService.getPopular(count);
        }
        return filmService.getPopular(count, genreId, year);
    }

    @GetMapping
    public Collection<Film> findAllFilms() {
        return filmService.getAllFilms();
    }

    @PostMapping
    public Film createFilm(@Valid @RequestBody Film film) {
        return filmService.addFilm(film);
    }

    @PutMapping
    public Film updateFilm(@Valid @RequestBody Film film) {
        return filmService.updateFilm(film);
    }

    @DeleteMapping("/{id}")
    public void deleteFilm(@PathVariable Integer id) {
        filmService.deleteFilm(id);
    }

    @PutMapping("/{id}/like/{userId}")
    public void addLike(@PathVariable Integer id, @PathVariable Integer userId) {
        filmService.addLike(id, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void deleteLike(@PathVariable Integer id, @PathVariable Integer userId) {
        filmService.deleteLike(id, userId);
    }

    @GetMapping("/director/{id}")
    public List<Film> getByDirector(@RequestParam(defaultValue = "year") String sortBy, @PathVariable Integer id) {
        return filmService.getByDirector(id, sortBy);
    }

    @GetMapping("/common")
    public Collection<Film> findCommonFilms(@RequestParam Integer userId, Integer friendId) {
        return filmService.findCommonFilms(userId, friendId);
    }
}

