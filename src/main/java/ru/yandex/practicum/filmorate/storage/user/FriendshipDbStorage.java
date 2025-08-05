package ru.yandex.practicum.filmorate.storage.user;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.mappers.UserRowMapper;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class FriendshipDbStorage implements FriendshipStorage {
    private final JdbcTemplate jdbcTemplate;
    private final UserDbStorage userDbStorage;
    private final UserRowMapper userRowMapper;

    @Override
    public void addFriendship(int userId, int friendId) {
        if (userDbStorage.getById(userId) == null || userDbStorage.getById(friendId) == null) {
            throw new NotFoundException("Пользователь не найден");
        }

        if (!friendshipExists(userId, friendId)) {
            String sql = "INSERT INTO friendship (user_id, friend_id, status) VALUES (?, ?, false)";
            jdbcTemplate.update(sql, userId, friendId);
        }
    }

    @Override
    public void removeFriendship(int userId, int friendId) {
        String sql = "DELETE FROM friendship WHERE user_id = ? AND friend_id = ?";
        int rows = jdbcTemplate.update(sql, userId, friendId);
    }

    @Override
    public boolean friendshipExists(int userId, int friendId) {
        String sql = "SELECT COUNT(*) FROM friendship WHERE user_id = ? AND friend_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, userId, friendId);
        return count != null && count > 0;
    }

    @Override
    public List<User> getFriends(int userId) {
        String sql = "SELECT u.* FROM users u " +
                "JOIN friendship f ON u.id = f.friend_id " +
                "WHERE f.user_id = ?";
        return jdbcTemplate.query(sql, userRowMapper, userId);
    }

    @Override
    public List<User> getCommonFriends(int userId1, int userId2) {
        String sql = "SELECT u.* FROM users u " +
                "JOIN friendship f1 ON u.id = f1.friend_id AND f1.user_id = ? " +
                "JOIN friendship f2 ON u.id = f2.friend_id AND f2.user_id = ?";
        return jdbcTemplate.query(sql, userRowMapper, userId1, userId2);
    }
}