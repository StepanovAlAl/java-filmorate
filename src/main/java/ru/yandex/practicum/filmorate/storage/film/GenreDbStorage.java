package ru.yandex.practicum.filmorate.storage.film;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@Slf4j
@Repository
@RequiredArgsConstructor
public class GenreDbStorage implements GenreStorage {
    private final JdbcTemplate jdbcTemplate;

    public List<Genre> getFilmGenres(int filmId) {
        String sql = "SELECT g.id, g.name FROM film_genres fg " +
                "JOIN genres g ON fg.genre_id = g.id " +
                "WHERE fg.film_id = ? ORDER BY g.id";
        return jdbcTemplate.query(sql, this::mapRowToGenre, filmId);
    }

    public void addFilmGenres(int filmId, Set<Genre> genres) {
        String sql = "INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?) "; //+
        //  "ON CONFLICT (film_id, genre_id) DO NOTHING";
        genres.forEach(genre -> {
            if (!genreExistsForFilm(filmId, genre.getId())) {
                log.info("Добавлены жанры для фильма {}: {}", filmId, genres);
                jdbcTemplate.update(sql, filmId, genre.getId());
            }
        });
    }

    private boolean genreExistsForFilm(int filmId, int genreId) {
        String sql = "SELECT COUNT(*) FROM film_genres WHERE film_id = ? AND genre_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, filmId, genreId);
        return count != null && count > 0;
    }

    public void removeFilmGenres(int filmId) {
        String sql = "DELETE FROM film_genres WHERE film_id = ?";
        jdbcTemplate.update(sql, filmId);
    }

    public List<Genre> getAllGenres() {
        String sql = "SELECT * FROM genres ORDER BY id";
        return jdbcTemplate.query(sql, this::mapRowToGenre);
    }

    public Genre getGenreById(int id) {
        log.debug("Получение жанра по ID: {}", id);

        String sql = "SELECT * FROM genres WHERE id = ?";
        try {
            log.error("Ошибка получения жанра. Жанр с id={} отсутствует.", id);
            return jdbcTemplate.queryForObject(sql, this::mapRowToGenre, id);
        } catch (EmptyResultDataAccessException e) {
            log.error("Жанр с ID {} не найден", id);
            throw new NotFoundException("genre c id=" + id + " не найден.");
        }
    }

    private Genre mapRowToGenre(ResultSet rs, int rowNum) throws SQLException {
        return new Genre(rs.getInt("id"), rs.getString("name"));
    }
}
