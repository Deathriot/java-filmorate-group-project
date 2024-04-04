package ru.yandex.practicum.filmorate.storage.film.dao;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureTestDatabase
@Transactional
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class FilmDbStorageTest {

    private final FilmStorage filmDbStorage;
    private final UserStorage userDbStorage;

    @Test
    public void addFilmTest() {

        Film newFilm = createDefaultFilm();
        Film film = filmDbStorage.addFilm(newFilm);

        newFilm.setId(film.getId());

        compare(newFilm, film);
    }

    @Test
    public void updateFilmTest() {

        Film newFilm = createDefaultFilm();
        Film film = filmDbStorage.addFilm(newFilm);

        film.setName("NewFilm");
        film.setDescription("NewDescr");
        film.setReleaseDate(LocalDate.of(2001, 1, 1));
        film.setDuration(111);
        filmDbStorage.updateFilm(film);

        Film savedFilm = filmDbStorage.getFilmById(film.getId());

        compare(film, savedFilm);
    }

    @Test
    public void addLikeTest() {

        User user = userDbStorage.addUser(User.builder()
                .email("user@mail.ru")
                .login("User").name("User")
                .birthday(LocalDate.of(1980, 12, 12))
                .build());


        Film newFilm = filmDbStorage.addFilm(createDefaultFilm());

        filmDbStorage.addLike(newFilm.getId(), user.getId());

        List<Film> likes = new ArrayList<>(filmDbStorage.getPopular(10));

        assertTrue(likes.contains(newFilm));

    }

    @Test
    public void deleteLikeTest() {

        User user = userDbStorage.addUser(User.builder()
                .email("user2@mail.ru")
                .login("User2")
                .name("User2")
                .birthday(LocalDate.of(1980, 12, 12))
                .build());
        User user2 = userDbStorage.addUser(User.builder()
                .email("user3@mail.ru")
                .login("User3")
                .name("User3")
                .birthday(LocalDate.of(1980, 12, 12))
                .build());

        Film newFilm1 = filmDbStorage.addFilm(createDefaultFilm());
        Film newFilm2 = filmDbStorage.addFilm(createDefaultFilm());

        filmDbStorage.addLike(newFilm1.getId(), user.getId());
        filmDbStorage.addLike(newFilm2.getId(), user.getId());
        filmDbStorage.addLike(newFilm2.getId(), user2.getId());

        List<Film> likes = new ArrayList<>(filmDbStorage.getPopular(1));

        assertTrue(likes.contains(newFilm2));

        filmDbStorage.deleteLike(newFilm2.getId(), user.getId());
        filmDbStorage.deleteLike(newFilm2.getId(), user2.getId());

        likes = new ArrayList<>(filmDbStorage.getPopular(1));

        assertFalse(likes.contains(newFilm2));
        assertTrue(likes.contains(newFilm1));
    }

    @Test
    public void deleteFilmTest() {

        Film newFilm = filmDbStorage.addFilm(createDefaultFilm());

        List<Film> likes = new ArrayList<>(filmDbStorage.getAllFilms());

        assertTrue(likes.contains(newFilm));

        filmDbStorage.deleteFilm(newFilm.getId());

        likes = new ArrayList<>(filmDbStorage.getAllFilms());

        assertFalse(likes.contains(newFilm));
    }

    @Test
    public void getPopularByGenre() {
        // given
        Film filmFromDb = filmDbStorage.addFilm(createFilmWithAllParameters());
        User userFromDb = userDbStorage.addUser(createUser());
        filmDbStorage.addLike(filmFromDb.getId(), userFromDb.getId());

        // when
        Collection<Film> popularByGenre = filmDbStorage.getPopularByGenre(10, 1);

        // then
        assertThat(popularByGenre)
                .isNotNull()
                .usingRecursiveComparison()
                .isEqualTo(List.of(filmFromDb));
    }

    @Test
    public void getPopularByGenreNotExist() {
        // given
        Film filmFromDb = filmDbStorage.addFilm(createFilmWithAllParameters());
        User userFromDb = userDbStorage.addUser(createUser());
        filmDbStorage.addLike(filmFromDb.getId(), userFromDb.getId());

        // when
        Collection<Film> popularByGenre = filmDbStorage.getPopularByGenre(10, 2);

        // then
        assertThat(popularByGenre)
                .isNotNull()
                .usingRecursiveComparison()
                .isEqualTo(Collections.EMPTY_LIST);
    }

    @Test
    public void getPopularByYear() {
        // given
        Film filmFromDb = filmDbStorage.addFilm(createFilmWithAllParameters());
        User userFromDb = userDbStorage.addUser(createUser());
        filmDbStorage.addLike(filmFromDb.getId(), userFromDb.getId());

        // when
        Collection<Film> popularByYear = filmDbStorage.getPopularByYear(10, "1999");

        // then
        assertThat(popularByYear)
                .isNotNull()
                .usingRecursiveComparison()
                .isEqualTo(List.of(filmFromDb));
    }

    @Test
    public void getPopularByYearNotExist() {
        // given
        Film filmFromDb = filmDbStorage.addFilm(createFilmWithAllParameters());
        User userFromDb = userDbStorage.addUser(createUser());
        filmDbStorage.addLike(filmFromDb.getId(), userFromDb.getId());

        // when
        Collection<Film> popularByYear = filmDbStorage.getPopularByYear(10, "2000");

        // then
        assertThat(popularByYear)
                .isNotNull()
                .usingRecursiveComparison()
                .isEqualTo(Collections.EMPTY_LIST);
    }

    @Test
    public void getPopularByGenreAndYear() {
        // given
        Film filmFromDb = filmDbStorage.addFilm(createFilmWithAllParameters());
        User userFromDb = userDbStorage.addUser(createUser());
        filmDbStorage.addLike(filmFromDb.getId(), userFromDb.getId());

        // when
        Collection<Film> popularByGenreAndYear =
                filmDbStorage.getPopularByGenreAndYear(10, 1, "1999");

        // then
        assertThat(popularByGenreAndYear)
                .isNotNull()
                .usingRecursiveComparison()
                .isEqualTo(List.of(filmFromDb));
    }

    @Test
    public void getPopularByGenreAndYearNotExist() {
        // given
        Film filmFromDb = filmDbStorage.addFilm(createFilmWithAllParameters());
        User userFromDb = userDbStorage.addUser(createUser());
        filmDbStorage.addLike(filmFromDb.getId(), userFromDb.getId());

        // when
        Collection<Film> popularByGenreAndYearWithNotExistGenre =
                filmDbStorage.getPopularByGenreAndYear(10, 2, "1999");

        // then
        assertThat(popularByGenreAndYearWithNotExistGenre)
                .isNotNull()
                .usingRecursiveComparison()
                .isEqualTo(Collections.EMPTY_LIST);

        // when
        Collection<Film> popularByGenreAndYearWithNotExistYear =
                filmDbStorage.getPopularByGenreAndYear(10, 1, "2000");

        // then
        assertThat(popularByGenreAndYearWithNotExistYear)
                .isNotNull()
                .usingRecursiveComparison()
                .isEqualTo(Collections.EMPTY_LIST);
    }

    private Film createDefaultFilm() {
        return Film.builder()
                .name("testFilm")
                .description("testFilm")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(120).mpa(Mpa.builder()
                        .id(1)
                        .build())
                .build();
    }

    private Film createFilmWithAllParameters() {
        return Film.builder()
                .name("film")
                .description("filmDescription")
                .releaseDate(LocalDate.of(1999, 1, 1))
                .mpa(Mpa.builder()
                        .id(1)
                        .build())
                .duration(200)
                .genres(List.of(Genre.builder()
                        .id(1)
                        .name("Комедия")
                        .build()))
                .build();
    }

    private User createUser() {
        return User.builder()
                .email("user@mail.ru")
                .login("user")
                .name("User")
                .birthday(LocalDate.of(1980, 12, 12))
                .build();
    }

    private void compare(Film film1, Film film2) {
        assertNotNull(film1);
        assertNotNull(film2);
        assertEquals(film1.getId(), film2.getId());
        assertEquals(film1.getName(), film2.getName());
        assertEquals(film1.getDescription(), film2.getDescription());
        assertEquals(film1.getDuration(), film2.getDuration());
        assertEquals(film1.getReleaseDate(), film2.getReleaseDate());
        assertEquals(1, film1.getMpa().getId());
        assertEquals(1, film2.getMpa().getId());
    }
}