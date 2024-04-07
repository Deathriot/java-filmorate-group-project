package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PastOrPresent;
import java.time.LocalDate;
import java.util.Set;

@Data
@Builder
public class User {

    private Integer id;

    @Email(message = "Email must not be empty and must have \"@\" ")
    private String email;

    @NotNull(message = "Login must not be empty or blank")
    @NotBlank(message = "Login must not be empty or blank")
    private String login;

    private String name;

    @PastOrPresent(message = "Date of birth must not be in the future")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthday;

    private Set<Integer> friends;
}
