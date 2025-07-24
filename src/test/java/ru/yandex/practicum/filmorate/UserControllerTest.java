package ru.yandex.practicum.filmorate;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.controller.UserController;
import org.junit.jupiter.api.BeforeEach;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.model.ValidationGroups;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.user.FriendshipStorage;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;

import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

@SpringBootTest
class UserControllerTest {
    private static Validator validator;
    private UserController userController;
    private UserService userService;

    @BeforeAll
    static void setup() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @BeforeEach
    void setUp() {
        InMemoryUserStorage userStorage = new InMemoryUserStorage();
        FriendshipStorage friendshipStorage = mock(FriendshipStorage.class); // Мок, так как не тестируем БД
        userService = new UserService(userStorage, friendshipStorage);
        userController = new UserController(userService);
    }

    @Test
    void invalidUserEmailFail() {
        User user = new User();
        user.setEmail("InvalidUserEmail");
        user.setLogin("login");

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("Электронная почта должна содержать символ @")));
    }

    @Test
    void emptyUserLoginFail() {
        User user = new User();
        user.setEmail("test@yandex.ru");
        user.setLogin("");

        Set<ConstraintViolation<User>> violations = validator.validate(user, ValidationGroups.Create.class);
        assertFalse(violations.isEmpty(), "Должны быть нарушения валидации для пустого логина");
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("Логин не может быть пустым")));
    }

    @Test
    void loginContainingSpacesFail() {
        User user = new User();
        user.setEmail("test@yandex.ru");
        user.setLogin("login with spaces");

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertFalse(violations.isEmpty(), "Должны быть нарушения валидации для логина с пробелами");
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("Логин не может содержать пробелы")));
    }

    @Test
    void futureBirthdayFail() {
        User user = new User();
        user.setEmail("test@yandex.ru");
        user.setLogin("login");
        user.setBirthday(LocalDate.now().plusDays(1));

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertFalse(violations.isEmpty(), "Должны быть нарушения валидации для даты рождения в будущем");
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("Дата рождения не может быть в будущем")));
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

    @Test
    void updateNonExistentUserReturn404() {
        User update = new User();
        update.setId(9999);
        update.setLogin("doloreUpdate");

        Exception exception = assertThrows(NotFoundException.class, () -> {
            userService.update(update);
        });

        assertEquals("Пользователь не найден! id=9999", exception.getMessage());
    }
}
