package ru.practicum.shareit.request;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@JsonTest
class ItemRequestDtoJsonTest {
    @Autowired
    private JacksonTester<ItemRequestDto> json;

    @Test
    void testRequestShortDto() throws Exception {
        LocalDateTime now = LocalDateTime.now();
        ItemRequestDto requestShortDto = ItemRequestDto.builder()
                .id(1L)
                .description("Description1")
                .created(now)
                .build();
        JsonContent<ItemRequestDto> result = json.write(requestShortDto);
        Assertions.assertThat(result).hasJsonPath("$.id");
        Assertions.assertThat(result).hasJsonPath("$.items");

        assertThat(result).extractingJsonPathStringValue("$.description").isEqualTo("Description1");
        assertThat(result).extractingJsonPathStringValue("$.created").isEqualTo(now
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")));
    }
}
