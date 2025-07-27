package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.film.GenreDbStorage;
import ru.yandex.practicum.filmorate.storage.film.MpaDbStorage;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserService userService;
    private final GenreDbStorage genreDbStorage;
    private final MpaDbStorage mpaDbStorage;

    public Film create(Film film) {
        log.info("Создание фильма: {}", film.getName());

        validateMpa(film.getMpa());
        validateGenres(film.getGenres());

        Film createdFilm = filmStorage.create(film);
        log.info("Фильм успешно создан с ID: {}", createdFilm.getId());
        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            genreDbStorage.addFilmGenres(createdFilm.getId(), film.getGenres());
        }
        return createdFilm;
    }

    public Film update(Film film) {
        log.info("Обновление фильма с ID: {}", film.getId());

        getById(film.getId()); // Проверка существования фильма
        validateMpa(film.getMpa());
        validateGenres(film.getGenres());

        Film updatedFilm = filmStorage.update(film);
        log.info("Фильм с ID {} успешно обновлен", updatedFilm.getId());
        genreDbStorage.removeFilmGenres(updatedFilm.getId());
        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            genreDbStorage.addFilmGenres(updatedFilm.getId(), film.getGenres());
        }
        return updatedFilm;
    }

    public Film getById(int id) {
        log.debug("Получение фильма по ID: {}", id);
        Film film = filmStorage.getById(id);
        if (film != null) {
            film.setGenres(new LinkedHashSet<>(genreDbStorage.getFilmGenres(film.getId())));
        }
        return film;
    }

    public Collection<Film> getAll() {
        log.debug("Получение всех фильмов");
        Collection<Film> films = filmStorage.getAll();
        films.forEach(f -> f.setGenres(new LinkedHashSet<>(genreDbStorage.getFilmGenres(f.getId()))));
        return films;
    }

    public void addLike(int filmId, int userId) {
        log.info("Добавление лайка фильму ID {} от пользователя {}", filmId, userId);

        Film film = filmStorage.getById(filmId);
        User user = userService.getById(userId);

        if (film == null || user == null) {
            log.error("Ошибка при добавлении лайка: фильм или пользователь не найдены");
            throw new NotFoundException("Фильм или пользователь не найдены");
        }

        filmStorage.addLike(filmId, userId);
        log.debug("Лайк успешно добавлен");
    }

    public void removeLike(int filmId, int userId) {
        log.info("Удаление лайка фильму ID {} от пользователя {}", filmId, userId);

        Film film = filmStorage.getById(filmId);
        User user = userService.getById(userId);

        if (film == null || user == null) {
            log.error("Ошибка при удалении лайка: фильм или пользователь не найдены");
            throw new NotFoundException("Фильм или пользователь не найдены");
        }

        filmStorage.removeLike(filmId, userId);
        log.debug("Лайк успешно удален");
    }

    public List<Film> getPopular(int count) {
        log.info("Получение {} популярных фильмов", count);

        if (count <= 0) {
            log.error("Некорректное количество фильмов: {}", count);
            throw new ValidationException("Количество фильмов должно быть положительным числом");
        }

        return filmStorage.getPopularFilms(count);
    }

    private void validateMpa(Mpa mpa) {
        if (mpa == null) {
            log.error("MPA рейтинг не указан");
            throw new ValidationException("MPA рейтинг обязателен");
        }

        try {
            mpaDbStorage.getMpaById(mpa.getId());
        } catch (EmptyResultDataAccessException e) {
            log.error("MPA с ID {} не найден", mpa.getId());
            throw new NotFoundException("MPA с id=" + mpa.getId() + " не найден");
        }
    }

    private void validateGenres(Set<Genre> genres) {
        if (genres != null && !genres.isEmpty()) {
            Set<Integer> genreIds = genres.stream()
                    .map(Genre::getId)
                    .collect(Collectors.toSet());

            List<Genre> existingGenres = genreDbStorage.getGenresByIds(genreIds);
            if (existingGenres.size() != genreIds.size()) {
                Set<Integer> existingIds = existingGenres.stream()
                        .map(Genre::getId)
                        .collect(Collectors.toSet());

                genreIds.removeAll(existingIds);
                throw new NotFoundException("Жанры с id=" + genreIds + " не найдены");
            }
        }
    }
}