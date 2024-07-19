package ru.practicum.shareit.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;
import ru.practicum.shareit.user.service.UserServiceImpl;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    UserRepository userRepository;
    @InjectMocks
    UserServiceImpl userService;
    User user;
    UserDto userDto;

    @BeforeEach
    void beforeEach() {
        user = User.builder().id(1L).name("user1").email("user1@mail.ru").build();
        userDto = UserMapper.toUserDto(user);
    }

    @Test
    void addIsOk() {
        when(userRepository.save(any())).thenReturn(user);
        assertEquals(UserMapper.toUserDto(user), userService.add(user));
        verify(userRepository).save(any());
    }

    @Test
    void updateWithCorrectId() {
        User newUser = User.builder().id(user.getId()).name("updateName").email("upd@mail.ru").build();
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(newUser);
        UserDto updUser = userService.update(user.getId(), newUser);
        assertEquals(newUser.getName(), updUser.getName());
        assertEquals(newUser.getEmail(), updUser.getEmail());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void updateWithIncorrectId() {
        User newUser = User.builder().id(20L).name("updateName").email("upd@mail.ru").build();
        when(userRepository.findById(anyLong())).thenThrow(new NotFoundException(""));
        assertThrows(NotFoundException.class, () -> userService.update(anyLong(), newUser));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void patchUpdateWithIncorrectField() {
//        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        Map<String, String> fieldsUpdate = Map.of("surname", "Pedro");
        assertThrows(ValidationException.class, () -> userService.patchUpdate(user.getId(), fieldsUpdate));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void getUserDtoByIdIsOk() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        UserDto userDto = userService.getUserDtoById(user.getId());
        assertEquals(userDto.getName(), user.getName());
        verify(userRepository).findById(anyLong());
    }

    @Test
    void getUserDtoByIdWithIncorrectId() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> userService.getUserDtoById(0L));
        verify(userRepository).findById(anyLong());
    }

    @Test
    void getByCorrectId() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        User checkUser = userService.getUserById(anyLong());
        assertEquals(user, checkUser);
        verify(userRepository).findById(anyLong());
    }

    @Test
    void getByIncorrectId() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> userService.getUserById(anyLong()));
    }

    @Test
    void getAllWithCollectionUser() {
        when(userRepository.findAll()).thenReturn(List.of(user));
        List<UserDto> users = userService.getAll();
        assertEquals(1, users.size());
        assertEquals(user.getId(), users.get(0).getId());
        assertEquals(user.getName(), users.get(0).getName());
        verify(userRepository).findAll();
    }

    @Test
    void getAllWithEmptyCollection() {
        when(userRepository.findAll()).thenReturn(Collections.emptyList());
        List<UserDto> users = userService.getAll();
        assertTrue(users.isEmpty());
        verify(userRepository).findAll();
    }

    @Test
    void deleteIsOk() {
        userService.delete(anyLong());
        verify(userRepository).deleteById(anyLong());
    }

}
