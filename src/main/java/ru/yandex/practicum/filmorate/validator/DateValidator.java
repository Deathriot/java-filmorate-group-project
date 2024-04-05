package ru.yandex.practicum.filmorate.validator;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class DateValidator implements ConstraintValidator<IsAfterDate, LocalDate> {

    LocalDate validDate;

    @Override
    public void initialize(IsAfterDate constraintAnnotation) {
        validDate = LocalDate.parse(constraintAnnotation.current(), DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }

    @Override
    public boolean isValid(LocalDate date, ConstraintValidatorContext constraintValidatorContext) {
        if (!date.isAfter(validDate)) {
            constraintValidatorContext.buildConstraintViolationWithTemplate("Дата релиза должна быть позже "
                    + validDate).addConstraintViolation();
            return false;
        }
        return true;
    }
}