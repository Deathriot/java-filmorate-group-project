package ru.yandex.practicum.filmorate.validator;

import ru.yandex.practicum.filmorate.annotation.FilmAnnotation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.time.LocalDate;

public class FilmValidator implements ConstraintValidator<FilmAnnotation, LocalDate> {

    private LocalDate minimumDate;

    @Override
    public void initialize(FilmAnnotation constraintAnnotation) {
        minimumDate = LocalDate.parse(constraintAnnotation.value());
    }

    @Override
    public boolean isValid(LocalDate value, ConstraintValidatorContext context) {
        return value == null || !value.isBefore(minimumDate);
    }
}