package ru.yandex.practicum.filmorate.storage.film;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.mappers.FilmRowMapper;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Repository
@Primary
@RequiredArgsConstructor
public class FilmDbStorage implements FilmStorage {
    private final JdbcTemplate jdbcTemplate;
    private final FilmRowMapper filmRowMapper;

    @Override
    public Film create(Film film) {
        log.info("Создание фильма: {}", film);
        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("films")
                .usingGeneratedKeyColumns("id");

        int filmId = simpleJdbcInsert.executeAndReturnKey(filmToMap(film)).intValue();
        film.setId(filmId);
        log.debug("Фильму присвоен ID: {}", filmId);

        return film;
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

        return film;
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

        // 1 запрос
        String filmsSql = """
                SELECT f.*, m.name AS mpa_name, m.description AS mpa_description,
                       COUNT(fl.user_id) AS likes_count
                FROM films f
                LEFT JOIN mpa m ON f.mpa_id = m.id
                LEFT JOIN film_likes fl ON f.id = fl.film_id
                GROUP BY f.id, m.id
                """;

        List<Film> films = jdbcTemplate.query(filmsSql, filmRowMapper);

        // 1 запрос
        String genresSql = """
                SELECT fg.film_id, g.id, g.name
                FROM film_genres fg
                JOIN genres g ON fg.genre_id = g.id
                WHERE fg.film_id IN (
                    SELECT id FROM films
                )
                ORDER BY fg.film_id, g.id
                """;

        Map<Integer, Set<Genre>> filmGenres = jdbcTemplate.query(genresSql, rs -> {
            Map<Integer, Set<Genre>> result = new HashMap<>();
            while (rs.next()) {
                int filmId = rs.getInt("film_id");
                Genre genre = new Genre(
                        rs.getInt("id"),
                        rs.getString("name")
                );
                result.computeIfAbsent(filmId, k -> new LinkedHashSet<>()).add(genre);
            }
            return result;
        });

        // Устанавливаем жанры для каждого фильма
        films.forEach(f -> f.setGenres(filmGenres.getOrDefault(f.getId(), new LinkedHashSet<>())));

        return films;
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
        log.info("Получение {} популярных фильмов с жанрами", count);

        // 1 Запрос
        String filmsSql = """
                SELECT f.*, m.name AS mpa_name, m.description AS mpa_description,
                       COUNT(fl.user_id) AS likes_count
                FROM films f
                LEFT JOIN mpa m ON f.mpa_id = m.id
                LEFT JOIN film_likes fl ON f.id = fl.film_id
                GROUP BY f.id, m.id
                ORDER BY likes_count DESC, f.id DESC
                LIMIT ?
                """;

        List<Film> films = jdbcTemplate.query(filmsSql, filmRowMapper, count);

        if (films.isEmpty()) {
            return films;
        }

        // 1 Запрос
        String genresSql = """
                SELECT fg.film_id, g.id, g.name
                FROM film_genres fg
                JOIN genres g ON fg.genre_id = g.id
                WHERE fg.film_id IN (%s)
                ORDER BY fg.film_id, g.id
                """.formatted(
                films.stream()
                        .map(f -> String.valueOf(f.getId()))
                        .collect(Collectors.joining(","))
        );

        Map<Integer, Set<Genre>> filmGenres = jdbcTemplate.query(genresSql, rs -> {
            Map<Integer, Set<Genre>> result = new HashMap<>();
            while (rs.next()) {
                int filmId = rs.getInt("film_id");
                Genre genre = new Genre(
                        rs.getInt("id"),
                        rs.getString("name")
                );
                result.computeIfAbsent(filmId, k -> new LinkedHashSet<>()).add(genre);
            }
            return result;
        });

        // Устанавливаем жанры для каждого фильма
        films.forEach(f -> f.setGenres(filmGenres.getOrDefault(f.getId(), new LinkedHashSet<>())));

        return films;
    }

    @Override
    public Set<Integer> getFilmLikes(int filmId) {
        String sql = "SELECT user_id FROM film_likes WHERE film_id = ?";
        return new HashSet<>(jdbcTemplate.queryForList(sql, Integer.class, filmId));
    }

    private Map<String, Object> filmToMap(Film film) {
        Map<String, Object> values = new HashMap<>();
        values.put("name", film.getName());
        values.put("description", film.getDescription());
        values.put("release_date", film.getReleaseDate());
        values.put("duration", film.getDuration());
        values.put("mpa_id", film.getMpa() != null ? film.getMpa().getId() : null);
        return values;
    }

}
