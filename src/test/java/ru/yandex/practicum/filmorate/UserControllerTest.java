package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.controller.UserController;
import org.junit.jupiter.api.BeforeEach;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class UserControllerTest {
    private UserController userController;

    @BeforeEach
    void setUp() {
        userController = new UserController();
    }

    @Test
    void invalidUserEmailFail() {
        User user = new User();
        user.setEmail("InvalidUserEmail");
        user.setLogin("login");
        assertThrows(ValidationException.class, () -> userController.createUser(user));
    }

    @Test
    void emptyUserLoginFail() {
        User user = new User();
        user.setEmail("test@yandex.ru");
        user.setLogin("");
        assertThrows(ValidationException.class, () -> userController.createUser(user));
    }

    @Test
    void loginContainingSpacesFail() {
        User user = new User();
        user.setEmail("test@yandex.ru");
        user.setLogin("login with spaces");
        assertThrows(ValidationException.class, () -> userController.createUser(user));
    }

    @Test
    void futureBirthdayFail() {
        User user = new User();
        user.setEmail("test@yandex.ru");
        user.setLogin("login");
        user.setBirthday(LocalDate.now().plusDays(1));
        assertThrows(ValidationException.class, () -> userController.createUser(user));
    }

    @Test
    void emptyNameReplacedByLogin() {
        User user = new User();
        user.setEmail("test@yandex.ru");
        user.setLogin("login");
        User createdUser = userController.createUser(user);
        assertEquals("login", createdUser.getName());
    }

    @Test
    void updatePartDataPass() {
        User user = new User();
        user.setEmail("test@yandex.ru");
        user.setLogin("login");
        User createdUser = userController.createUser(user);

        User update = new User();
        update.setId(createdUser.getId());
        update.setLogin("newlogin");
        assertDoesNotThrow(() -> userController.updateUser(update));
    }
}
