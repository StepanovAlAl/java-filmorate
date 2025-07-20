package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.controller.FilmController;
import org.junit.jupiter.api.BeforeEach;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class FilmControllerTest {
    private FilmController filmController;

    @BeforeEach
    void setUp() {
        filmController = new FilmController();
    }

    @Test
    void emptyFilmName() {
        Film film = new Film();
        film.setName("");
        assertThrows(ValidationException.class, () -> filmController.createFilm(film));
    }

    @Test
    void tooLongFilmDescription() {
        Film film = new Film();
        film.setName("Test");
        film.setDescription("a".repeat(201));
        assertThrows(ValidationException.class, () -> filmController.createFilm(film));
    }

    @Test
    void normalLongFilmDescription() {
        Film film = new Film();
        film.setName("Test");
        film.setDescription("a".repeat(200));
        assertDoesNotThrow(() -> filmController.createFilm(film));
    }

    @Test
    void minReleaseDateFail() {
        Film film = new Film();
        film.setName("Test");
        film.setReleaseDate(LocalDate.of(1895, 12, 27));
        assertThrows(ValidationException.class, () -> filmController.createFilm(film));
    }

    @Test
    void minReleaseDatePass() {
        Film film = new Film();
        film.setName("Test");
        film.setReleaseDate(LocalDate.of(1895, 12, 28));
        assertDoesNotThrow(() -> filmController.createFilm(film));
    }

    @Test
    void minDurationFail() {
        Film film = new Film();
        film.setName("Test");
        film.setDuration(-1);
        assertThrows(ValidationException.class, () -> filmController.createFilm(film));
    }

    @Test
    void zeroDurationFail() {
        Film film = new Film();
        film.setName("Test");
        film.setDuration(0);
        assertThrows(ValidationException.class, () -> filmController.createFilm(film));
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
