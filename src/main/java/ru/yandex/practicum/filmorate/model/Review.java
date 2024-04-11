package ru.yandex.practicum.filmorate.model;

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@Builder
public final class Review {
    private Integer reviewId;

    @NotBlank
    @NotNull
    private String content;

    @NotNull
    private Boolean isPositive;

    @NotNull
    private Integer userId;

    @NotNull
    private Integer filmId;

    private int useful;
}
