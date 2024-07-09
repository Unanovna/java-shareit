package ru.practicum.shareit.user.service;

import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;
import java.util.Map;

public interface UserService {
    UserDto add(User user);

    UserDto update(User user);

    UserDto patchUpdate(long id, Map<String, String> updates);

    UserDto getUserById(long id);

    List<UserDto> getAll();

    void delete(long userId);

    void deleteAll();

    void checkEmail(String email, long id);

    void checkName(String name, long id);

}
