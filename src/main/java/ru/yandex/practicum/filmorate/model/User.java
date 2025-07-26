package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;
import java.util.*;

@Getter
@Setter
@EqualsAndHashCode
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    private Integer id;

    @Email(message = "Email должен быть корректным")
    @NotBlank(message = "Email не может быть пустым")
    private String email;

    @NotBlank(message = "Логин не может быть пустым")
    @Pattern(regexp = "^\\S+$", message = "Логин не может содержать пробелы")
    private String login;

    private String name;

    @Past(message = "Дата рождения должна быть в прошлом")
    private LocalDate birthday;

    private final Set<Integer> friends = new HashSet<>();

    public void addFriend(int friendId) {
        friends.add(friendId);
    }

    public void removeFriend(int friendId) {
        friends.remove(friendId);
    }

    public Set<Integer> getFriends() {
        return new HashSet<>(friends);
    }

    public Map<String, Object> toMap() {
        Map<String, Object> values = new HashMap<>();
        values.put("email", email);
        values.put("login", login);
        values.put("name", name);
        values.put("birthday", birthday);
        return values;
    }
}
