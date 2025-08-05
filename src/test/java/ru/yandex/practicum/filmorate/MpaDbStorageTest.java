package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.film.MpaStorage;
import ru.yandex.practicum.filmorate.storage.film.MpaDbStorage;
import ru.yandex.practicum.filmorate.storage.mappers.MpaRowMapper;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({MpaDbStorage.class, MpaRowMapper.class})
class MpaDbStorageTest {

    @Autowired
    private final MpaStorage mpaStorage;

    @Test
    @DisplayName("Получение всех MPA")
    void testGetAllMpa() {
        List<Mpa> mpas = mpaStorage.getAllMpa();

        assertThat(mpas).isNotNull();
        assertThat(mpas.size()).isGreaterThan(0);
    }

    @Test
    @DisplayName("Получение MPA по ID")
    void testGetMpaById() {
        Mpa createdMpa = mpaStorage.getAllMpa().getFirst();
        Mpa foundMpa = mpaStorage.getMpaById(createdMpa.getId());

        assertThat(foundMpa).isNotNull();
        assertThat(foundMpa.getId()).isEqualTo(createdMpa.getId());
        assertThat(foundMpa.getName()).isEqualTo(createdMpa.getName());
    }

    @Test
    @DisplayName("Получение MPA по несуществующему ID")
    void testGetMpaByIdNotFound() {
        try {
            mpaStorage.getMpaById(999);
        } catch (NotFoundException e) {
            assertThat(e.getMessage()).isEqualTo("MPA с id=999 не найден");
        }
    }
}
