package ru.yandex.practicum.filmorate.controller;

import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;

@RestController
@RequestMapping("/users")
@Slf4j
public class UserController {
    private final HashMap<Integer, User> users = new HashMap<>();
    private int idCounter = 1;

    @PostMapping
    public User createUser(@RequestBody User user) {
        validateUser(user);
        user.setId(idCounter++);
        users.put(user.getId(), user);
        log.info("Создание пользователя {}", user);
        return user;
    }

    @PutMapping
    public User updateUser(@RequestBody User user) {
        validateUser(user);
        if (!users.containsKey(user.getId())) {
            throw new ValidationException("Пользователь не найден! id=" + user.getId());
        }
        users.put(user.getId(), user);
        log.info("Обновление пользователя {}", user);
        return user;
    }

    @GetMapping
    public Collection<User> getAllUsers() {
        return users.values();
    }

    private void validateUser(User user) {
        if (user.getEmail() == null || user.getEmail().isBlank() || !user.getEmail().contains("@")) {
            throw new ValidationException("Электронная почта не может быть пустой и должна содержать символ @");
        }
        if (user.getLogin() == null || user.getLogin().isBlank() || user.getLogin().contains(" ")) {
            throw new ValidationException("Логин не может быть пустым и содержать пробелы");
        }
        if (user.getBirthday() != null && user.getBirthday().isAfter(LocalDate.now())) {
            throw new ValidationException("Дата рождения не может быть в будущем");
        }
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
    }
}