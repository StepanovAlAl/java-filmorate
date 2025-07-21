package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;

@RestController
@RequestMapping("/films")
@Slf4j
@Validated
public class FilmController {
    private final HashMap<Integer, Film> films = new HashMap<>();
    private int idCounter = 1;
    private static final LocalDate MINIMAL_RELEASE_DATE = LocalDate.of(1895, 12, 28);

    @PostMapping
    public Film createFilm(@Valid @RequestBody Film film) {
        film.setId(idCounter++);
        films.put(film.getId(), film);
        log.info("Добавление фильма {}", film);
        return film;
    }

    @PutMapping
    public Film updateFilm(@Valid @RequestBody Film film) {
        if (film.getId() == 0 || !films.containsKey(film.getId())) {
            throw new ValidationException("Фильм не найден! id=" + film.getId());
        }

        Film existingFilm = films.get(film.getId());

        if (film.getName() != null) {
            existingFilm.setName(film.getName());
        }
        if (film.getDescription() != null) {
            existingFilm.setDescription(film.getDescription());
        }
        if (film.getReleaseDate() != null) {
            existingFilm.setReleaseDate(film.getReleaseDate());
        }
        if (film.getDuration() != 0) {
            existingFilm.setDuration(film.getDuration());
        }

        log.info("Обновление фильма {}", existingFilm);
        return existingFilm;
    }

    @GetMapping
    public Collection<Film> getAllFilms() {
        return films.values();
    }

}