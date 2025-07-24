package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.FriendshipStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.*;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserStorage userStorage;
    private final FriendshipStorage friendshipStorage;

    public User create(User user) {
        return userStorage.create(user);
    }

    public User update(User user) {
        User existingUser = userStorage.getById(user.getId());
        if (existingUser == null) {
            throw new NotFoundException("Пользователь не найден! id=" + user.getId());
        }
        return userStorage.update(user);
    }

    public User getById(int id) {
        return userStorage.getById(id);
    }

    public Collection<User> getAll() {
        return userStorage.getAll();
    }

    public void addFriend(int userId, int friendId) {
        User user = userStorage.getById(userId);
        User friend = userStorage.getById(friendId);

        if (user == null) {
            throw new NotFoundException("Пользователь не найден! id=" + userId);
        }
        if (friend == null) {
            throw new NotFoundException("Пользователь не найден! id=" + friendId);
        }
        if (userId == friendId) {
            throw new ValidationException("Пользователь не может добавить себя в друзья");
        }

        friendshipStorage.addFriendship(userId, friendId);
    }

    public void removeFriend(int userId, int friendId) {
        User user = userStorage.getById(userId);
        User friend = userStorage.getById(friendId);

        if (user == null) {
            throw new NotFoundException("Пользователь не найден! id=" + userId);
        }
        if (friend == null) {
            throw new NotFoundException("Пользователь не найден! id=" + friendId);
        }

        friendshipStorage.removeFriendship(userId, friendId);
    }

    public List<User> getFriends(int userId) {
        User user = userStorage.getById(userId);
        if (user == null) {
            throw new NotFoundException("Пользователь не найден! id=" + userId);
        }
        return friendshipStorage.getFriends(userId);
    }

    public List<User> getCommonFriends(int userId, int otherId) {
        User user = userStorage.getById(userId);
        User otherUser = userStorage.getById(otherId);

        if (user == null || otherUser == null) {
            throw new NotFoundException("Пользователь не найден");
        }

        return friendshipStorage.getCommonFriends(userId, otherId);
    }
}
