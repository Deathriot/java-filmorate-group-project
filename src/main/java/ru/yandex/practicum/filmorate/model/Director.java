package ru.yandex.practicum.filmorate.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.Map;

@Data
@AllArgsConstructor
public class Director {

    @NotNull
    protected int id;
    @NotBlank
    protected String name;

    public Map<String, Object> directorToMap() {
        Map<String, Object> values = new HashMap<>();
        values.put("NAME", name);
        return values;
    }
}
