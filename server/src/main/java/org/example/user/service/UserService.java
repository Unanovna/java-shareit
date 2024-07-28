package org.example.user.service;

import org.example.user.UserDto;
import org.example.user.model.User;

import java.util.List;

public interface UserService {
    UserDto add(User user);

    UserDto update(Long userId, UserDto userDto);

    User getUserById(long id);

    UserDto getUserDtoById(long id);

    List<UserDto> getAll();

    void delete(long userId);

    void deleteAll();

}

