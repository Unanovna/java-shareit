package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.service.UserService;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

/**
 * TODO Sprint add-controllers.
 */
@RestController
@RequestMapping(path = "/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PostMapping
    public UserDto create(@Valid @RequestBody @NotNull UserDto user) {
        return userService.add(UserMapper.toUser(user));
    }

    @PutMapping
    public UserDto updateUser(@Valid @RequestBody @NotNull UserDto user) {
        return userService.update(UserMapper.toUser(user));
    }

    @PatchMapping("{id}")
    public UserDto patchUpdate(@PathVariable long id, @RequestBody Map<String, String> updates) {
        return userService.patchUpdate(id, updates);
    }

    @GetMapping("{id}")
    public UserDto getUser(@PathVariable(required = false) long id) {
        return userService.getUserById(id);
    }

    @GetMapping
    public List<UserDto> getAllUsers() {
        return userService.getAll();
    }

    @DeleteMapping("{id}")
    public void deleteUser(@PathVariable long id) {
        userService.delete(id);
    }
}