package ru.yandex.practicum.filmorate;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.controller.FilmController;
import org.junit.jupiter.api.BeforeEach;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.ValidationGroups;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.storage.film.InMemoryFilmStorage;

import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class FilmControllerTest {
    private static Validator validator;
    private FilmController filmController;
    private FilmService filmService;

    @BeforeAll
    static void setup() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @BeforeEach
    void setUp() {
        filmService = new FilmService(new InMemoryFilmStorage());
        filmController = new FilmController(filmService);
    }

    @Test
    void shouldCreateValidFilm() {
        Film film = new Film();
        film.setName("Correct Film");
        film.setDescription("Valid description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);

        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertTrue(violations.isEmpty(), "Корректный фильм должен проходить валидацию.");
    }

    @Test
    void emptyFilmName() {
        Film film = new Film();
        film.setName("");

        Set<ConstraintViolation<Film>> violations = validator.validate(film, ValidationGroups.Create.class);
        assertFalse(violations.isEmpty(), "Должны быть нарушения валидации для пустого названия");
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("Название не может быть пустым")));
    }

    @Test
    void tooLongFilmDescription() {
        Film film = new Film();
        film.setName("Test");
        film.setDescription("a".repeat(201));

        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertFalse(violations.isEmpty(), "Должны быть нарушения валидации для слишком длинного описания");
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("Максимальная длина описания — 200 символов")));
    }

    @Test
    void normalLongFilmDescription() {
        Film film = new Film();
        film.setName("Test");
        film.setDescription("a".repeat(200));
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);

        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertTrue(violations.isEmpty(), "Описание длиной 200 символов должно проходить валидацию");
    }

    @Test
    void minReleaseDatePass() {
        Film film = new Film();
        film.setName("Test");
        film.setReleaseDate(LocalDate.of(1895, 12, 28));
        film.setDescription("Description");
        film.setDuration(120);

        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertTrue(violations.isEmpty(), "Минимальная допустимая дата релиза должна проходить валидацию");
    }

    @Test
    void minDurationFail() {
        Film film = new Film();
        film.setName("Test");
        film.setDuration(-1);

        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertFalse(violations.isEmpty(), "Должны быть нарушения валидации для отрицательной продолжительности");
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("Продолжительность фильма должна быть положительным числом")));
    }

    @Test
    void zeroDurationFail() {
        Film film = new Film();
        film.setName("Test");
        film.setDuration(0);

        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertFalse(violations.isEmpty(), "Должны быть нарушения валидации для нулевой продолжительности");
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("Продолжительность фильма должна быть положительным числом")));
    }

    @Test
    void updateFilmPartDataPass() {
        Film film = new Film();
        film.setName("Test");
        film.setDuration(100);
        Film createdFilm = filmController.createFilm(film);

        Film update = new Film();
        update.setId(createdFilm.getId());
        update.setName("Updated Name");
        assertDoesNotThrow(() -> filmController.updateFilm(update));
    }
}
