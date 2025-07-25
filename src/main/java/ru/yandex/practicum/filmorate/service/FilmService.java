package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.util.*;

@Service
@RequiredArgsConstructor
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserService userService;

    public Film create(Film film) {
        return filmStorage.create(film);
    }

    public Film update(Film film) {
        Film existingFilm = filmStorage.getById(film.getId());
        if (existingFilm == null) {
            throw new NotFoundException("Фильм не найден! id=" + film.getId());
        }
        return filmStorage.update(film);
    }

    public Film getById(int id) {
        Film film = filmStorage.getById(id);
        if (film == null) {
            throw new NotFoundException("Фильм не найден! id=" + id);
        }
        return film;
    }

    public Collection<Film> getAll() {
        return filmStorage.getAll();
    }

    public void addLike(int filmId, int userId) {
        Film film = filmStorage.getById(filmId);
        User user = userService.getById(userId);
        if (film == null) {
            throw new NotFoundException("Фильм не найден! id=" + filmId);
        }
        if (user == null) {
            throw new NotFoundException("Пользователь не найден! id=" + userId);
        }
        film.getLikes().add(userId);
    }

    public void removeLike(int filmId, int userId) {
        Film film = filmStorage.getById(filmId);
        User user = userService.getById(userId);
        if (film == null) {
            throw new NotFoundException("Фильм не найден! id=" + filmId);
        }
        if (user == null) {
            throw new NotFoundException("Пользователь не найден! id=" + userId);
        }
        film.getLikes().remove(userId);
    }

    public List<Film> getPopular(int count) {
        return filmStorage.getAll().stream()
                .sorted((f1, f2) -> Integer.compare(
                        f2.getLikes().size(),
                        f1.getLikes().size()))
                .limit(count)
                .toList();
    }
}