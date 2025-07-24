package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.film.GenreDbStorage;
import ru.yandex.practicum.filmorate.storage.film.MpaDbStorage;

import java.util.Collection;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserService userService;
    private final GenreDbStorage genreDbStorage;
    private final MpaDbStorage mpaDbStorage;

    public Film create(Film film) {
        if (film.getMpa() != null) {
            try {
                mpaDbStorage.getMpaById(film.getMpa().getId());
            } catch (EmptyResultDataAccessException e) {
                throw new NotFoundException("MPA с id=" + film.getMpa().getId() + " не найден");
            }
        }

        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            for (Genre genre : film.getGenres()) {
                try {
                    genreDbStorage.getGenreById(genre.getId());
                } catch (EmptyResultDataAccessException e) {
                    throw new NotFoundException("Жанр с id=" + genre.getId() + " не найден");
                }
            }
        }

        return filmStorage.create(film);
    }

    public Film update(Film film) {
        Film existingFilm = filmStorage.getById(film.getId());
        if (existingFilm == null) {
            throw new NotFoundException("Фильм не найден! id=" + film.getId());
        }
        if (film.getMpa() != null) {
            try {
                mpaDbStorage.getMpaById(film.getMpa().getId());
            } catch (EmptyResultDataAccessException e) {
                throw new NotFoundException("MPA с id=" + film.getMpa().getId() + " не найден");
            }
        }

        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            for (Genre genre : film.getGenres()) {
                try {
                    genreDbStorage.getGenreById(genre.getId());
                } catch (EmptyResultDataAccessException e) {
                    throw new NotFoundException("Жанр с id=" + genre.getId() + " не найден");
                }
            }
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
        film.addLike(userId);
        filmStorage.update(film);
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
        film.removeLike(userId);
        filmStorage.update(film);
    }


    public List<Film> getPopular(int count) {
        if (count <= 0) {
            throw new ValidationException("Количество фильмов должно быть положительным числом");
        }
        return filmStorage.getPopularFilms(count);
    }


}