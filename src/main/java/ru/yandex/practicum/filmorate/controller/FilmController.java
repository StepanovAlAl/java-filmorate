package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.model.ValidationGroups;

import java.util.Collection;
import java.util.List;

@RestController
@RequestMapping("/films")
@Slf4j
@Validated
public class FilmController {
    private final FilmService filmService;

    @Autowired
    public FilmController(FilmService filmService) {
        this.filmService = filmService;
    }

    @PostMapping
    @Validated(ValidationGroups.Create.class)
    public Film createFilm(@Valid @RequestBody Film film) {
        if (film.getId() != null) {
            throw new ValidationException("ID должен быть null при создании");
        }
        log.info("Добавление фильма {}", film);
        return filmService.create(film);
    }

    @PutMapping
    @Validated(ValidationGroups.Update.class)
    public Film updateFilm(@Valid @RequestBody Film film) {
        log.info("Обновление фильма {}", film);
        return filmService.update(film);
    }

    @GetMapping("/{id}")
    public Film getFilm(@PathVariable int id) {
        Film film = filmService.getById(id);
        if (film == null) {
            throw new NotFoundException("Фильм не найден! id=" + id);
        }
        return film;
    }

    @GetMapping
    public Collection<Film> getAllFilms() {
        return filmService.getAll();
    }

    @PutMapping("/{id}/like/{userId}")
    public void addLike(@PathVariable int id, @PathVariable int userId) {
        filmService.addLike(id, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void removeLike(@PathVariable int id, @PathVariable int userId) {
        filmService.removeLike(id, userId);
    }

    @GetMapping("/popular")
    public List<Film> getPopular(@RequestParam(defaultValue = "10") int count) {
        return filmService.getPopular(count);
    }
}