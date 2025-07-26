package ru.yandex.practicum.filmorate.storage.film;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.mappers.FilmRowMapper;

import java.util.*;

@Slf4j
@Repository
@Primary
@RequiredArgsConstructor
public class FilmDbStorage implements FilmStorage {
    private final JdbcTemplate jdbcTemplate;
    private final GenreDbStorage genreDbStorage;
    private final FilmRowMapper filmRowMapper;

    @Override
    public Film create(Film film) {
        log.info("Создание фильма: {}", film);
        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("films")
                .usingGeneratedKeyColumns("id");

        int filmId = simpleJdbcInsert.executeAndReturnKey(film.toMap()).intValue();
        film.setId(filmId);
        log.debug("Фильму присвоен ID: {}", filmId);

        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            log.debug("Добавление жанров для фильма ID {}: {}", filmId, film.getGenres());
            genreDbStorage.addFilmGenres(filmId, film.getGenres());
        }

        Film createdFilm = getById(filmId);
        log.info("Фильм успешно создан: {}", createdFilm);
        return createdFilm;
    }

    @Override
    public Film update(Film film) {
        log.info("Обновление фильма ID {}: {}", film.getId(), film);

        String sql = "UPDATE films SET name = ?, description = ?, release_date = ?, duration = ?, mpa_id = ? WHERE id = ?";
        jdbcTemplate.update(sql,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                film.getMpa().getId(),
                film.getId());

        log.debug("Обновление жанров для фильма ID {}", film.getId());
        genreDbStorage.removeFilmGenres(film.getId());
        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            genreDbStorage.addFilmGenres(film.getId(), film.getGenres());
        }

        Film updatedFilm = getById(film.getId());
        log.info("Фильм успешно обновлен: {}", updatedFilm);
        return updatedFilm;
    }

    @Override
    public Film getById(int id) {
        log.debug("Получение фильма по ID: {}", id);

        String sql = "SELECT f.*, m.name AS mpa_name, m.description AS mpa_description " +
                "FROM films f " +
                "LEFT JOIN mpa m ON f.mpa_id = m.id " +
                "WHERE f.id = ?";
        return jdbcTemplate.queryForObject(sql, filmRowMapper, id);
    }

    @Override
    public Collection<Film> getAll() {
        log.debug("Получение всех фильмов");

        String sql = "SELECT f.*, m.name AS mpa_name, m.description AS mpa_description " +
                "FROM films f " +
                "LEFT JOIN mpa m ON f.mpa_id = m.id";
        return jdbcTemplate.query(sql, filmRowMapper);
    }

    @Override
    public void delete(int id) {
        String sql = "DELETE FROM films WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }

    @Override
    public void addLike(int filmId, int userId) {
        log.info("Добавление лайка фильму ID {} от пользователя {}", filmId, userId);

        String sql = "INSERT INTO film_likes (film_id, user_id) VALUES (?, ?)";
        jdbcTemplate.update(sql, filmId, userId);
    }

    @Override
    public void removeLike(int filmId, int userId) {
        log.info("Удаление лайка фильму ID {} от пользователя {}", filmId, userId);

        String sql = "DELETE FROM film_likes WHERE film_id = ? AND user_id = ?";
        jdbcTemplate.update(sql, filmId, userId);
    }

    @Override
    public List<Film> getPopularFilms(int count) {
        String sql = "SELECT f.*, m.name AS mpa_name, m.description AS mpa_description, " +
                "COUNT(fl.user_id) AS likes_count " +
                "FROM films f " +
                "LEFT JOIN mpa m ON f.mpa_id = m.id " +
                "LEFT JOIN film_likes fl ON f.id = fl.film_id " +
                "GROUP BY f.id, m.id " +
                "ORDER BY likes_count DESC, f.id DESC " +
                "LIMIT ?";
        return jdbcTemplate.query(sql, filmRowMapper, count);
    }

}
