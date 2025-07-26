package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.exception.NotFoundException;

import java.util.List;

public interface MpaStorage {
    List<Mpa> getAllMpa();

    Mpa getMpaById(int id) throws NotFoundException;
}