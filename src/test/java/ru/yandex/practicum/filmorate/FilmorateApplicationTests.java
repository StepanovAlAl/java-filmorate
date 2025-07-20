package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.controller.FilmController;
import ru.yandex.practicum.filmorate.controller.UserController;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class FilmorateApplicationTests {
	private final FilmController filmController = new FilmController();
	private final UserController userController = new UserController();

	@Test
	void contextLoads() {
	}

	@Test
	void EmptyFilmName() {
		Film film = new Film();
		film.setName("");
		assertThrows(ValidationException.class, () -> filmController.createFilm(film));
	}

	@Test
	void TooLongFilmDescription() {
		Film film = new Film();
		film.setName("Test");
		film.setDescription("a".repeat(201));
		assertThrows(ValidationException.class, () -> filmController.createFilm(film));
	}

	@Test
	void InvalidUserEmail() {
		User user = new User();
		user.setEmail("InvalidUserEmail");
		user.setLogin("login");
		assertThrows(ValidationException.class, () -> userController.createUser(user));
	}

	@Test
	void EmptyUserLogin() {
		User user = new User();
		user.setEmail("test@yandex.ru");
		user.setLogin("");
		assertThrows(ValidationException.class, () -> userController.createUser(user));
	}
}
