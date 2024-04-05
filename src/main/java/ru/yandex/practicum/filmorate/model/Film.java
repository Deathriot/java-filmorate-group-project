package ru.yandex.practicum.filmorate.model;

import lombok.Builder;
import lombok.Data;
import ru.yandex.practicum.filmorate.validator.IsAfterDate;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
@Builder(toBuilder = true)
public class Film {

    private Integer id;

    @NotEmpty(message = "Film name can not be empty")
    private String name;

    @Size(max = 200, message = "Maximum description length is 200 char")
    @NotNull
    private String description;

    @IsAfterDate(current = "1895-12-27")
    private LocalDate releaseDate;

    @Positive(message = "The length of the film must be positive")
    private Integer duration;

    protected Mpa mpa;

    private List<Genre> genres;

    private List<Director> directors;

    public List<Integer> directorToInt() {
        Set<Integer> directorWithoutDuplicate = new HashSet<>();
        for (Director director: directors) {
            directorWithoutDuplicate.add(director.getId());
        }
        return new ArrayList<>(directorWithoutDuplicate);
    }
}
