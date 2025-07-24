package ru.yandex.practicum.filmorate.storage.film;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@Repository
@Primary
@RequiredArgsConstructor
public class FilmDbStorage implements FilmStorage {
    private final JdbcTemplate jdbcTemplate;
    private final GenreDbStorage genreDbStorage;
    private final MpaDbStorage mpaDbStorage;

    @Override
    public Film create(Film film) {
        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("films")
                .usingGeneratedKeyColumns("id");

        int filmId = simpleJdbcInsert.executeAndReturnKey(film.toMap()).intValue();
        film.setId(filmId);

        if (film.getGenres() != null) {
            genreDbStorage.addFilmGenres(film.getId(), film.getGenres());
        }

        return getById(filmId);
    }

    @Override
    public Film update(Film film) {
        String sql = "UPDATE films SET name = ?, description = ?, release_date = ?, duration = ?, mpa_id = ? WHERE id = ?";
        jdbcTemplate.update(sql,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                film.getMpa().getId(),
                film.getId());

        genreDbStorage.removeFilmGenres(film.getId());
        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            genreDbStorage.addFilmGenres(film.getId(), film.getGenres());
        }

        return getById(film.getId());
    }

    @Override
    public Film getById(int id) {
        String sql = "SELECT f.*, m.name AS mpa_name, m.description AS mpa_description " +
                "FROM films f " +
                "LEFT JOIN mpa m ON f.mpa_id = m.id " +
                "WHERE f.id = ?";
        Film film = jdbcTemplate.queryForObject(sql, this::mapRowToFilm, id);

        if (film.getGenres() != null) {
            Set<Genre> sortedGenres = new TreeSet<>(Comparator.comparingInt(Genre::getId));
            sortedGenres.addAll(film.getGenres());
            film.setGenres(sortedGenres);
        }

        return film;
    }

    @Override
    public Collection<Film> getAll() {
        String sql = "SELECT f.*, m.name AS mpa_name, m.description AS mpa_description " +
                "FROM films f " +
                "LEFT JOIN mpa m ON f.mpa_id = m.id";
        return jdbcTemplate.query(sql, this::mapRowToFilm);
    }

    @Override
    public void delete(int id) {
        String sql = "DELETE FROM films WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }

    private Film mapRowToFilm(ResultSet rs, int rowNum) throws SQLException {
        Film film = new Film();
        film.setId(rs.getInt("id"));
        film.setName(rs.getString("name"));
        film.setDescription(rs.getString("description"));
        film.setReleaseDate(rs.getDate("release_date").toLocalDate());
        film.setDuration(rs.getInt("duration"));

        Mpa mpa = new Mpa(
                rs.getInt("mpa_id"),
                rs.getString("mpa_name"),
                rs.getString("mpa_description")
        );
        film.setMpa(mpa);

        film.setGenres(new HashSet<>(genreDbStorage.getFilmGenres(film.getId())));
        Set<Integer> likes = new HashSet<>(getFilmLikes(film.getId()));
        likes.forEach(userId -> film.addLike(userId));

        return film;
    }

    private List<Integer> getFilmLikes(int filmId) {
        String sql = "SELECT user_id FROM film_likes WHERE film_id = ?";
        return jdbcTemplate.queryForList(sql, Integer.class, filmId);
    }

    @Override
    public List<Film> getPopularFilms(int count) {
        String sql = "SELECT f.*, m.name AS mpa_name, m.description AS mpa_description, " +
                "COUNT(fl.user_id) AS likes_count " +
                "FROM films f " +
                "LEFT JOIN mpa m ON f.mpa_id = m.id " +
                "LEFT JOIN film_likes fl ON f.id = fl.film_id " +
                "GROUP BY f.id, m.id " +
                "ORDER BY likes_count DESC " +
                "LIMIT ?";

        return jdbcTemplate.query(sql, this::mapRowToFilmWithLikes, count);
    }


    private Film mapRowToFilmWithLikes(ResultSet rs, int rowNum) throws SQLException {
        Film film = mapRowToFilm(rs, rowNum);

        int likesCount = rs.getInt("likes_count");

        return film;
    }
}