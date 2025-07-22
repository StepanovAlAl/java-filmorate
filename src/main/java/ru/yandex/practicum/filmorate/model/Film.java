package ru.yandex.practicum.filmorate.model;

import lombok.Data;
import jakarta.validation.constraints.*;
import ru.yandex.practicum.filmorate.validation.MinReleaseDate;

import java.time.LocalDate;

@Data
public class Film {

    @Null(groups = ValidationGroups.Create.class)
    @NotNull(groups = ValidationGroups.Update.class)
    private int id;

    @NotBlank(groups = ValidationGroups.Create.class, message = "Название не может быть пустым")
    private String name;

    @Size(max = 200, message = "Максимальная длина описания — 200 символов")
    private String description;

    @NotNull(groups = ValidationGroups.Create.class, message = "Дата релиза должна быть указана")
    @MinReleaseDate
    private LocalDate releaseDate;

    @Positive(message = "Продолжительность фильма должна быть положительным числом")
    private int duration;
}