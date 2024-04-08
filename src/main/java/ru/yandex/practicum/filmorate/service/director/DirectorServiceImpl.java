package ru.yandex.practicum.filmorate.service.director;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.storage.director.DirectorStorage;

import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
public class DirectorServiceImpl implements DirectorService {

    private final DirectorStorage directorStorage;

    @Override
    public Director createDirector(Director director) {
        return directorStorage.createDirector(director);
    }

    @Override
    public List<Director> getDirectors() {
        return directorStorage.getDirectors();
    }

    @Override
    public Director getDirectorById(Integer id) {
        return directorStorage.getDirectorById(id);
    }

    @Override
    public Director updateDirector(Director director) {
        return directorStorage.updateDirector(director);
    }

    @Override
    public void deleteDirectorById(Integer id) {
        directorStorage.deleteDirectorById(id);
    }
}
