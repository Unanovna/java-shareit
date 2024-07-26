package ru.practicum.shareit.user.service;

import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;
import java.util.Map;

public interface UserService {
    UserDto add(User user);

    UserDto update(Long userId, User user);

    UserDto patchUpdate(long id, Map<String, String> updates);

    User getUserById(long id);

    UserDto getUserDtoById(long id);

    List<UserDto> getAll();

    void delete(long userId);

    void deleteAll();

}