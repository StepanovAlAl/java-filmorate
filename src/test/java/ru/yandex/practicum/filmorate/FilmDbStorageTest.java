package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;
import ru.yandex.practicum.filmorate.storage.film.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;
import ru.yandex.practicum.filmorate.storage.mappers.FilmRowMapper;
import ru.yandex.practicum.filmorate.storage.mappers.UserRowMapper;
import ru.yandex.practicum.filmorate.storage.film.GenreDbStorage;
import ru.yandex.practicum.filmorate.storage.mappers.GenreRowMapper;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({FilmDbStorage.class, FilmRowMapper.class, UserDbStorage.class,
        UserRowMapper.class, GenreDbStorage.class, GenreRowMapper.class})
class FilmDbStorageTest {

    @Autowired
    private final FilmStorage filmStorage;

    @Autowired
    private final UserStorage userStorage;

    private Film createDefaultFilm() {
        return Film.builder()
                .name("Начало")
                .description("Отличный фильм Кристофера Нолана")
                .releaseDate(LocalDate.of(2010, 7, 16))
                .duration(148)
                .mpa(Mpa.builder().id(1).name("PG-13").build())
                .build();
    }

    private User createDefaultUser() {
        return User.builder()
                .email("user@mail.com")
                .login("userlogin")
                .name("username")
                .birthday(LocalDate.of(1999, 12, 7))
                .build();
    }

    @Test
    @DisplayName("Добавление фильма в базу данных")
    void testCreateFilm() {
        Film createdFilm = filmStorage.create(createDefaultFilm());

        assertThat(createdFilm).isNotNull();
        //assertThat(createdFilm.getId()).isPositive();
        //assertThat(createdFilm.getName()).isEqualTo("Начало");
    }

    @Test
    @DisplayName("Получение фильма по ID")
    void testGetFilmById() {
        Film film = filmStorage.create(createDefaultFilm());
        Film foundFilm = filmStorage.getById(film.getId());

        assertThat(foundFilm).isNotNull();
        assertThat(foundFilm.getId()).isEqualTo(film.getId());
        assertThat(foundFilm.getName()).isEqualTo(film.getName());
    }

    @Test
    @DisplayName("Получение всех фильмов")
    void testGetAllFilms() {
        filmStorage.create(createDefaultFilm());

        Film secondFilm = Film.builder()
                .name("Темный рыцарь")
                .description("Бэтмен сталкивается с новой угрозой")
                .releaseDate(LocalDate.of(2008, 7, 18))
                .duration(152)
                .mpa(Mpa.builder().id(1).name("PG-13").build())
                .build();

        filmStorage.create(secondFilm);

        List<Film> films = (List<Film>) filmStorage.getAll();

        assertThat(films).isNotNull();
        assertThat(films.size()).isEqualTo(2);
    }

    @Test
    @DisplayName("Обновление фильма")
    void testUpdateFilm() {
        Film film = filmStorage.create(createDefaultFilm());

        Film updatedFilm = Film.builder()
                .id(film.getId())
                .name("Начало обновлено")
                .description("Обновленное описание")
                .releaseDate(LocalDate.of(2011, 7, 16))
                .duration(150)
                .mpa(Mpa.builder().id(2).name("R").build())
                .build();

        filmStorage.update(updatedFilm);

        Film retrievedFilm = filmStorage.getById(film.getId());

        assertThat(retrievedFilm.getName()).isEqualTo("Начало обновлено");
        assertThat(retrievedFilm.getDescription()).isEqualTo("Обновленное описание");
    }

    @Test
    @DisplayName("Удаление фильма по ID")
    void testDeleteFilm() {
        Film film = filmStorage.create(createDefaultFilm());
        filmStorage.delete(film.getId());

        List<Film> films = (List<Film>) filmStorage.getAll();
        assertThat(films).doesNotContain(film);
    }

    @Test
    @DisplayName("Добавление лайка к фильму")
    void testAddLike() {
        User user = userStorage.create(createDefaultUser());
        Film film = filmStorage.create(createDefaultFilm());

        filmStorage.addLike(film.getId(), user.getId());

        Film updatedFilm = filmStorage.getById(film.getId());
        assertThat(updatedFilm).isNotNull();
    }

    @Test
    @DisplayName("Удаление лайка с фильма")
    void testDeleteLike() {
        User user = userStorage.create(createDefaultUser());
        Film film = filmStorage.create(createDefaultFilm());

        filmStorage.addLike(film.getId(), user.getId());
        filmStorage.removeLike(film.getId(), user.getId());

        Film updatedFilm = filmStorage.getById(film.getId());
        assertThat(updatedFilm).isNotNull();
    }

    @Test
    @DisplayName("Получение популярных фильмов")
    void testGetPopularFilms() {
        filmStorage.create(createDefaultFilm());

        Film secondFilm = Film.builder()
                .name("Темный рыцарь")
                .description("Бэтмен сталкивается с новой угрозой")
                .releaseDate(LocalDate.of(2008, 7, 18))
                .duration(152)
                .mpa(Mpa.builder().id(1).name("PG-13").build())
                .build();

        filmStorage.create(secondFilm);

        List<Film> popularFilms = filmStorage.getPopularFilms(5);

        assertThat(popularFilms).isNotNull();
        assertThat(popularFilms.size()).isGreaterThan(0);
    }
}