package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.film.GenreStorage;
import ru.yandex.practicum.filmorate.storage.film.GenreDbStorage;
import ru.yandex.practicum.filmorate.storage.mappers.GenreRowMapper;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({GenreDbStorage.class, GenreRowMapper.class})
class GenreDbStorageTest {

    @Autowired
    private GenreStorage genreStorage;

    @Test
    @DisplayName("Получение всех жанров возвращает список жанров")
    void testGetAllGenresShouldReturnAllGenres() {
        Genre genre1 = genreStorage.getGenreById(1);
        Genre genre2 = genreStorage.getGenreById(2);
        Genre genre3 = genreStorage.getGenreById(3);
        List<Genre> genres = genreStorage.getAllGenres();

        assertThat(genres).hasSize(6);
        assertThat(genres.getFirst()).isEqualTo(genre1);
        assertThat(genres.get(1)).isEqualTo(genre2);
        assertThat(genres.get(2)).isEqualTo(genre3);
    }

    @Test
    @DisplayName("Получение жанра по ID")
    void testGetGenreByIdShouldReturnGenre() {
        Genre expectedGenre = genreStorage.getGenreById(1);
        Genre actualGenre = genreStorage.getGenreById(expectedGenre.getId());

        assertThat(actualGenre).usingRecursiveComparison().isEqualTo(expectedGenre);
        assertEquals("Комедия", actualGenre.getName());
    }

    @Test
    @DisplayName("Получение жанра по ID вызывает исключение, если жанр не найден")
    void testGetGenreByIdShouldThrowWhenGenreNotFound() {
        assertThrows(NotFoundException.class, () -> genreStorage.getGenreById(9999));
    }
}