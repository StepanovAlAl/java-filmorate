package ru.yandex.practicum.filmorate.storage.film;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

//@Disabled("Тест отключен, не могу победить ошибку: [ERROR]   FilmDbStorageTest.shouldCreateAndGetFilm » IllegalState ApplicationContext failure threshold (1) exceeded: skipping repeated attempt to load context ..")
@JdbcTest
@ActiveProfiles("dev")
@AutoConfigureTestDatabase
@Import({FilmDbStorage.class, GenreDbStorage.class, MpaDbStorage.class})
class FilmDbStorageTest {
    @Autowired
    private FilmDbStorage filmDbStorage;

    private Film testFilm;

    @BeforeEach
    void setUp() {
        Mpa mpa = new Mpa(1, "G", "Нет возрастных ограничений");
        Set<Genre> genres = new HashSet<>();
        genres.add(new Genre(1, "Комедия"));

        testFilm = new Film();
        testFilm.setName("Test Film");
        testFilm.setDescription("Test Description");
        testFilm.setReleaseDate(LocalDate.of(2000, 1, 1));
        testFilm.setDuration(120);
        testFilm.setMpa(mpa);
        testFilm.setGenres(genres);
    }

    @Test
    void createAndGetFilm() {
        Film createdFilm = filmDbStorage.create(testFilm);

        Film retrievedFilm = filmDbStorage.getById(createdFilm.getId());

        assertThat(retrievedFilm)
                .isNotNull()
                .usingRecursiveComparison()
                .ignoringFields("id")
                .isEqualTo(testFilm);
    }

    @Test
    void updateFilm() {
        Film createdFilm = filmDbStorage.create(testFilm);

        createdFilm.setName("Updated Name");
        createdFilm.setDuration(150);
        filmDbStorage.update(createdFilm);

        Film updatedFilm = filmDbStorage.getById(createdFilm.getId());

        assertThat(updatedFilm.getName()).isEqualTo("Updated Name");
        assertThat(updatedFilm.getDuration()).isEqualTo(150);
    }

    @Test
    void getAllFilms() {
        filmDbStorage.create(testFilm);

        List<Film> films = (List<Film>) filmDbStorage.getAll();

        assertThat(films).isNotEmpty();
        assertThat(films.get(0).getName()).isEqualTo("Test Film");
    }
}