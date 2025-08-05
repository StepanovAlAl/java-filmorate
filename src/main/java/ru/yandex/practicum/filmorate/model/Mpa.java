package ru.yandex.practicum.filmorate.model;

import lombok.Builder;
import lombok.Data;
import lombok.AllArgsConstructor;

@Data
@Builder
@AllArgsConstructor
public class Mpa {
    private int id;
    private String name;
    private String description;

}