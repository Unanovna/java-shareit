package ru.practicum.shareit.booking;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.booking.dto.OutputBookingDto;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class OutputBookingDtoTest {
    @Autowired
    private JacksonTester<OutputBookingDto> jacksonTester;

    @SneakyThrows
    @Test
    void testBookingDto() {
        User user = User.builder().id(2L).name("user1").email("user1@mail.ru").build();
        Item item = Item.builder().id(5L).name("item5").description("itemDescription1").available(true)
                .owner(user).request(null).build();
        Booking booking = Booking.builder().id(1L).item(item).booker(user).status(BookingStatus.APPROVED)
                .start(LocalDateTime.now()).end(LocalDateTime.now().plusHours(1)).build();
        OutputBookingDto outputBookingDto = BookingMapper.toOutputBookingDto(booking);
        JsonContent<OutputBookingDto> result = jacksonTester.write(outputBookingDto);
        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.start").isNotBlank();
        assertThat(result).extractingJsonPathStringValue("$.end").isNotBlank();
        assertThat(result).extractingJsonPathNumberValue("$.booker.id").isEqualTo(2);
        assertThat(result).extractingJsonPathStringValue("$.booker.name").isEqualTo("user1");
        assertThat(result).extractingJsonPathNumberValue("$.item.id").isEqualTo(5);
        assertThat(result).extractingJsonPathStringValue("$.item.name").isEqualTo("item5");
    }
}
