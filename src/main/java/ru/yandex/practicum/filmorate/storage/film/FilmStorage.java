package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.List;

public interface FilmStorage {
    Film create(Film film);

    Film update(Film film);

    Film getById(int id);

    Collection<Film> getAll();

    void delete(int id);

    List<Film> getPopularFilms(int count);
}