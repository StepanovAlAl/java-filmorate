package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;
import java.util.*;

@Service
public class UserService {
    private final UserStorage userStorage;
    private final Map<Integer, Set<Integer>> friends = new HashMap<>();

    @Autowired
    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

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
        if (user == null || friend == null) {
            throw new NotFoundException("Пользователь не найден");
        }
        friends.computeIfAbsent(userId, k -> new HashSet<>()).add(friendId);
        friends.computeIfAbsent(friendId, k -> new HashSet<>()).add(userId);
    }

    public void removeFriend(int userId, int friendId) {
        if (friends.containsKey(userId)) {
            friends.get(userId).remove(friendId);
        }
        if (friends.containsKey(friendId)) {
            friends.get(friendId).remove(userId);
        }
    }

    public List<User> getFriends(int userId) {
        return friends.getOrDefault(userId, Collections.emptySet()).stream()
                .map(userStorage::getById)
                .filter(Objects::nonNull)
                .toList();
    }

    public List<User> getCommonFriends(int userId, int otherId) {
        Set<Integer> userFriends = friends.getOrDefault(userId, Collections.emptySet());
        Set<Integer> otherFriends = friends.getOrDefault(otherId, Collections.emptySet());

        return userFriends.stream()
                .filter(otherFriends::contains)
                .map(userStorage::getById)
                .filter(Objects::nonNull)
                .toList();
    }
}