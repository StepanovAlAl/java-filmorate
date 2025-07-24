package ru.yandex.practicum.filmorate.storage.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

//@Disabled("Тест отключен, не могу победить ошибку: [ERROR]   FilmDbStorageTest.shouldCreateAndGetFilm » IllegalState ApplicationContext failure threshold (1) exceeded: skipping repeated attempt to load context ..")
@JdbcTest
@ActiveProfiles("dev")
@AutoConfigureTestDatabase
@Import(UserDbStorage.class)
class UserDbStorageTest {
    @Autowired
    private UserDbStorage userDbStorage;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setEmail("test@example.com");
        testUser.setLogin("testLogin");
        testUser.setName("Test Name");
        testUser.setBirthday(LocalDate.of(1990, 1, 1));
    }

    @Test
    void createAndGetUser() {
        User createdUser = userDbStorage.create(testUser);

        User retrievedUser = userDbStorage.getById(createdUser.getId());

        assertThat(retrievedUser)
                .isNotNull()
                .usingRecursiveComparison()
                .ignoringFields("id")
                .isEqualTo(testUser);
    }

    @Test
    void updateUser() {
        User createdUser = userDbStorage.create(testUser);

        createdUser.setName("Updated Name");
        userDbStorage.update(createdUser);

        User updatedUser = userDbStorage.getById(createdUser.getId());

        assertThat(updatedUser.getName()).isEqualTo("Updated Name");
    }

    @Test
    void getAllUsers() {
        userDbStorage.create(testUser);

        List<User> users = (List<User>) userDbStorage.getAll();

        assertThat(users).isNotEmpty();
        assertThat(users.get(0).getLogin()).isEqualTo("testLogin");
    }
}