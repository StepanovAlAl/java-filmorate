package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import java.util.*;

@Service
public class FilmService {
    private final FilmStorage filmStorage;
    private final Map<Integer, Set<Integer>> likes = new HashMap<>();

    @Autowired
    public FilmService(FilmStorage filmStorage) {
        this.filmStorage = filmStorage;
    }

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
        return filmStorage.getById(id);
    }

    public Collection<Film> getAll() {
        return filmStorage.getAll();
    }

    public void addLike(int id, int userId) {
        Film film = filmStorage.getById(id);
        if (film == null) {
            throw new NotFoundException("Фильм не найден! id=" + id);
        }
        likes.computeIfAbsent(id, k -> new HashSet<>()).add(userId);
    }

    public void removeLike(int filmId, int userId) {
        if (likes.containsKey(filmId)) {
            likes.get(filmId).remove(userId);
        }
    }

    public List<Film> getPopular(int count) {
        return filmStorage.getAll().stream()
                .sorted((f1, f2) -> Integer.compare(
                        likes.getOrDefault(f2.getId(), Collections.emptySet()).size(),
                        likes.getOrDefault(f1.getId(), Collections.emptySet()).size()))
                .limit(count)
                .toList();
    }
}