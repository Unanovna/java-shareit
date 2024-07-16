package ru.practicum.shareit.user;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.user.dto.UserDto;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class UserDtoTest {
    @Autowired
    private JacksonTester<UserDto> jacksonTester;
    UserDto userDto = UserDto.builder().id(1L).name("userDto1").email("userDto1@mail.ru").build();

    @Test
    void testMapper() throws Exception {
        JsonContent<UserDto> check = jacksonTester.write(userDto);
        assertThat(check).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(check).extractingJsonPathStringValue("$.name").isEqualTo("userDto1");
        assertThat(check).extractingJsonPathStringValue("$.email").isEqualTo("userDto1@mail.ru");
    }
}