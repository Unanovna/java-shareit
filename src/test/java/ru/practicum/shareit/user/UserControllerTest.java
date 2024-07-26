package ru.practicum.shareit.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.controller.UserController;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import javax.validation.ValidationException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UserController.class)
@FieldDefaults(level = AccessLevel.PRIVATE)
class UserControllerTest {
    @MockBean
    UserService userService;
    @Autowired
    ObjectMapper mapper;
    @Autowired
    MockMvc mvc;
    UserDto userDto;
    User user;

    @BeforeEach
    void beforeEach() {
        user = User.builder().id(1L).name("user1").email("user1@mail.ru").build();
        userDto = UserDto.builder().id(1L).name("user1").email("user1@mail.ru").build();
    }

    @Test
    void createUserIsOk() throws Exception {
        when(userService.add(any())).thenReturn(userDto);
        mvc.perform(post("/users")
                        .content(mapper.writeValueAsString(userDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(user.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(user.getName())))
                .andExpect(jsonPath("$.email", is(user.getEmail())));
        verify(userService).add(any());
    }

    @Test
    void createUserErrorEmptyName() throws Exception {
        when(userService.add(any())).thenThrow(ValidationException.class);
        userDto.setName("");
        mvc.perform(post("/users")
                        .content(mapper.writeValueAsString(userDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createUserErrorBadEmail() throws Exception {
        when(userService.add(any())).thenThrow(ValidationException.class);
        userDto.setEmail("user_mail.ru");
        mvc.perform(post("/users")
                        .content(mapper.writeValueAsString(userDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateUserIsOk() throws Exception {
        UserDto updateUser = UserDto.builder()
                .id(userDto.getId()).name("updateName").email(userDto.getEmail()).build();
        when(userService.update(anyLong(), any())).thenReturn(updateUser);
        mvc.perform(put("/users")
                        .content(mapper.writeValueAsString(updateUser))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(updateUser.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(updateUser.getName())))
                .andExpect(jsonPath("$.email", is(updateUser.getEmail())));
        verify(userService).update(anyLong(), any());
    }

    @Test
    void patchUpdateIsOk() throws Exception {
        Map<String, String> updates = Map.of(
                "name", "nameUpdate",
                "email", "update@yandex.ru");
        userDto.setName(updates.get("name"));
        userDto.setEmail(updates.get("email"));
        when(userService.patchUpdate(anyLong(), anyMap())).thenReturn(userDto);
        mvc.perform(patch("/users/{userId}", 1L)
                        .content(mapper.writeValueAsString(userDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(userDto.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(updates.get("name"))))
                .andExpect(jsonPath("$.email", is(updates.get("email"))));
        verify(userService).patchUpdate(anyLong(), anyMap());
    }

    @Test
    void updateUserWithBadEmail() throws Exception {
        when(userService.update(anyLong(), any())).thenThrow(ValidationException.class);
        userDto = UserDto.builder()
                .id(1L)
                .email("user_email.ru")
                .build();
        mvc.perform(put("/users")
                        .content(mapper.writeValueAsString(userDto))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
        verify(userService, never()).update(anyLong(), any());
    }

    @Test
    void getUserIsOk() throws Exception {
        when(userService.getUserDtoById(anyLong())).thenReturn(userDto);
        mvc.perform(get("/users/{userId}", anyLong())
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userDto.getId()))
                .andExpect(jsonPath("$.email").value(userDto.getEmail()))
                .andExpect(jsonPath("$.name").value(userDto.getName()));
        verify(userService).getUserDtoById(anyLong());
    }

    @Test
    void getUserWithIncorrectId() throws Exception {
        when(userService.getUserDtoById(anyLong())).thenThrow(NotFoundException.class);
        mvc.perform(get("/users/{userId}", anyLong()))
                .andExpect(status().isNotFound());
        verify(userService).getUserDtoById(anyLong());
    }

    @Test
    void getUserWithIncorrectId2() throws Exception {
        doThrow(NotFoundException.class).when(userService).getUserDtoById(anyLong());
        mvc.perform(get("/users/{userId}", anyLong()))
                .andExpect(status().isNotFound());
        verify(userService).getUserDtoById(anyLong());
    }

    @Test
    void getAllUsersIsOk() throws Exception {
        when(userService.getAll()).thenReturn(List.of(userDto));
        mvc.perform(get("/users")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[*]").exists())
                .andExpect(jsonPath("$.[*]").isNotEmpty())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$.[0].id").value(userDto.getId()))
                .andExpect(jsonPath("$.[0].email").value(userDto.getEmail()))
                .andExpect(jsonPath("$.[0].name").value(userDto.getName()));
        verify(userService).getAll();
    }

    @Test
    void getAllWithEmptyCollection() throws Exception {
        when(userService.getAll()).thenReturn(List.of());
        mvc.perform(get("/users")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[*]").isEmpty());
        verify(userService).getAll();
    }

    @Test
    void deleteIsOk() throws Exception {
        mvc.perform(delete("/users/1"))
                .andDo(print())
                .andExpectAll(
                        status().isOk());
        verify(userService).delete(1L);
    }
}
