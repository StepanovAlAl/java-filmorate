package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;
import ru.yandex.practicum.filmorate.storage.mappers.UserRowMapper;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({UserDbStorage.class, UserRowMapper.class})
class UserDbStorageTest {

    @Autowired
    private UserStorage userStorage;

    private User defaultUser;

    @BeforeEach
    void setUp() {
        defaultUser = User.builder()
                .email("user@email.com")
                .login("login1")
                .name("UserName")
                .birthday(LocalDate.of(1999, 12, 7))
                .build();
    }

    @Test
    @DisplayName("Создание пользователя")
    void testCreateUserShouldReturnUserWithId() {
        User createdUser = userStorage.create(defaultUser);

        assertNotNull(createdUser.getId(), "ID пользователя не должен быть null");
        assertTrue(createdUser.getId() > 0, "ID пользователя должен быть положительным");
        assertEquals("user@email.com", createdUser.getEmail());
        assertEquals("login1", createdUser.getLogin());
        assertEquals("UserName", createdUser.getName());
        assertEquals(LocalDate.of(1999, 12, 7), createdUser.getBirthday());
    }

    @Test
    @DisplayName("Обновление пользователя")
    void testUpdateUserShouldUpdateUserData() {
        User originalUser = userStorage.create(defaultUser);
        User updatedUser = User.builder()
                .id(originalUser.getId())
                .email("updated@email.com")
                .login("login1")
                .name("UpdatedName")
                .birthday(LocalDate.of(1999, 12, 7))
                .build();

        User result = userStorage.update(updatedUser);

        assertThat(result).usingRecursiveComparison().isEqualTo(updatedUser);
        assertEquals("updated@email.com", result.getEmail());
        assertEquals("login1", result.getLogin());
        assertEquals("UpdatedName", result.getName());
        assertEquals(LocalDate.of(1999, 12, 7), result.getBirthday());
    }

    @Test
    @DisplayName("Обновление пользователя должно вызвать исключение, если пользователь не найден")
    void testUpdateUserShouldThrowWhenUserNotFound() {
        User nonExistentUser = User.builder()
                .id(9999)
                .email("user@email.com")
                .login("login1")
                .name("UserName")
                .birthday(LocalDate.of(1999, 12, 7))
                .build();

        assertThrows(NotFoundException.class, () -> userStorage.update(nonExistentUser));
    }

    @Test
    @DisplayName("Получение пользователя по ID")
    void testGetUserByIdShouldReturnUser() {
        User expectedUser = userStorage.create(defaultUser);
        User actualUser = userStorage.getById(expectedUser.getId());

        assertThat(actualUser).usingRecursiveComparison().isEqualTo(expectedUser);
        assertEquals("user@email.com", actualUser.getEmail());
        assertEquals("login1", actualUser.getLogin());
        assertEquals("UserName", actualUser.getName());
        assertEquals(LocalDate.of(1999, 12, 7), actualUser.getBirthday());
    }

    @Test
    @DisplayName("Получение несуществующего пользователя по ID")
    void testGetUserByIdShouldThrowWhenUserNotFound() {
        assertThrows(NotFoundException.class, () -> userStorage.getById(9999));
    }

    @Test
    @DisplayName("Получение всех пользователей")
    void testGetAllUsersShouldReturnAllUsers() {
        User user1 = userStorage.create(defaultUser);
        User user2 = userStorage.create(User.builder()
                .email("user2@email.com")
                .login("login2")
                .name("UserName")
                .birthday(LocalDate.of(1999, 12, 7))
                .build());

        List<User> users = (List<User>) userStorage.getAll();

        assertThat(users).hasSize(2);
        assertThat(users).extracting(User::getId).containsExactlyInAnyOrder(user1.getId(), user2.getId());
        assertThat(users).extracting(User::getEmail)
                .containsExactlyInAnyOrder("user@email.com", "user2@email.com");
    }

    @Test
    @DisplayName("Удаление пользователя по ID")
    void testDeleteUserByIdShouldRemoveUser() {
        User user = userStorage.create(defaultUser);
        userStorage.delete(user.getId());

        assertThrows(NotFoundException.class, () -> userStorage.getById(user.getId()));
    }

    @Test
    @DisplayName("Удаление друга")
    void testDeleteFriendShouldRemoveFriendship() {
        User user1 = userStorage.create(defaultUser);
        User user2 = userStorage.create(User.builder()
                .email("friend@email.com")
                .login("friendLogin")
                .name("FriendName")
                .birthday(LocalDate.of(1999, 12, 7))
                .build());

        userStorage.addFriend(user1.getId(), user2.getId());
        userStorage.removeFriend(user1.getId(), user2.getId());
        List<User> friends = userStorage.getFriends(user1.getId());

        assertThat(friends).isEmpty();
    }

    @Test
    @DisplayName("Получение общих друзей")
    void testGetCommonUsersFriendsShouldReturnCommonFriends() {
        User user1 = userStorage.create(defaultUser);
        User user2 = userStorage.create(User.builder()
                .email("user2@email.com")
                .login("login2")
                .name("UserName2")
                .birthday(LocalDate.of(1999, 12, 7))
                .build());
        User commonFriend = userStorage.create(User.builder()
                .email("common@email.com")
                .login("commonFriend")
                .name("CommonName")
                .birthday(LocalDate.of(1999, 12, 7))
                .build());

        userStorage.addFriend(user1.getId(), commonFriend.getId());
        userStorage.addFriend(user2.getId(), commonFriend.getId());

        List<User> commonFriends = userStorage.getCommonFriends(user1.getId(), user2.getId());

        assertThat(commonFriends).hasSize(1);
        assertThat(commonFriends.getFirst().getEmail()).isEqualTo("common@email.com");
        assertThat(commonFriends.getFirst().getLogin()).isEqualTo("commonFriend");
    }

    @Test
    @DisplayName("Получение друзей пользователя")
    void testGetUsersFriendsShouldReturnUserFriends() {
        User user = userStorage.create(defaultUser);
        User friend1 = userStorage.create(User.builder()
                .email("friend1@email.com")
                .login("friend1")
                .name("Friend1")
                .birthday(LocalDate.of(1999, 12, 7))
                .build());
        User friend2 = userStorage.create(User.builder()
                .email("friend2@email.com")
                .login("friend2")
                .name("Friend2")
                .birthday(LocalDate.of(1999, 12, 7))
                .build());

        userStorage.addFriend(user.getId(), friend1.getId());
        userStorage.addFriend(user.getId(), friend2.getId());

        List<User> friends = userStorage.getFriends(user.getId());

        assertThat(friends).hasSize(2);
        assertThat(friends).extracting(User::getEmail)
                .containsExactlyInAnyOrder("friend1@email.com", "friend2@email.com");
    }
}
