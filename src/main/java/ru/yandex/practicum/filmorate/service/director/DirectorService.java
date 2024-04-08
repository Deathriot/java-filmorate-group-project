package ru.yandex.practicum.filmorate.service.director;

import ru.yandex.practicum.filmorate.model.Director;

import java.util.List;

public interface DirectorService {

    Director createDirector(Director director);

    List<Director> getDirectors();

    Director getDirectorById(int id);

    Director updateDirector(Director director);

    void deleteDirectorById(int id);
}