package ru.yandex.practicum.filmorate.storage.user;

import ru.yandex.practicum.filmorate.model.User;

import java.util.List;

public interface FriendshipStorage {
    void addFriendship(int userId, int friendId);

    void removeFriendship(int userId, int friendId);

    boolean friendshipExists(int userId, int friendId);

    List<User> getFriends(int userId);

    List<User> getCommonFriends(int userId1, int userId2);
}