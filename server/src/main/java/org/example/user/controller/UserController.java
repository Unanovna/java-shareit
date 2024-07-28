package org.example.user.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.user.UserDto;
import org.example.user.mapper.UserMapper;
import org.example.user.service.UserService;
import org.jetbrains.annotations.NotNull;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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
        return userService.update(user.getId(), user);
    }

    @PatchMapping("{id}")
    public UserDto patchUpdateUser(@PathVariable(required = false) long id,
                                   @Valid @RequestBody @NotNull UserDto user) {
        return userService.update(id, user);
    }

    @GetMapping("{id}")
    public UserDto getUser(@PathVariable(required = false) long id) {
        return userService.getUserDtoById(id);
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
