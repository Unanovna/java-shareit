package ru.practicum.shareit.booking;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.booking.dto.ShortBookingDto;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class ShortBookingDtoTest {
    @Autowired
    private JacksonTester<ShortBookingDto> jacksonTester;

    @SneakyThrows
    @Test
    void testShortBookingDtoTest() {
        User user = User.builder().id(1L).name("user1").email("user1@mail.ru").build();
        User user2 = User.builder().id(2L).name("user2").email("user2@mail.ru").build();
        Item item = Item.builder().id(1L).name("item1").description("itemDescription1").available(true)
                .owner(user).request(null).build();
        Booking booking = Booking.builder().id(1L).item(item).booker(user2).status(BookingStatus.APPROVED)
                .start(LocalDateTime.now()).end(LocalDateTime.now().plusHours(1)).build();
        ShortBookingDto shortBookingDto = BookingMapper.toShortBookingDto(booking);
        JsonContent<ShortBookingDto> result = jacksonTester.write(shortBookingDto);
        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.start").isNotBlank();
        assertThat(result).extractingJsonPathStringValue("$.end").isNotBlank();
        assertThat(result).extractingJsonPathNumberValue("$.item.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.item.name").isEqualTo("item1");
        assertThat(result).extractingJsonPathNumberValue("$.bookerId").isEqualTo(2);
    }
}
