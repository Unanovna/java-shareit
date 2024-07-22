package ru.practicum.shareit.booking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repositories.BookingRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
class BookingRepositoryTest {
    @Autowired
    BookingRepository bookingRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    ItemRepository itemRepository;

    User user1, user2, user3;
    Item item11, item21, item32;
    Booking booking12, booking22, booking33, booking43;
    LocalDateTime start = LocalDateTime.now();
    LocalDateTime end = LocalDateTime.now().plusHours(1);
    Pageable pageable = PageRequest.of(0, 10, BookingRepository.SORT_BY_START_BY_DESC);

    @BeforeEach
    void beforeEach() {
        user1 = User.builder().id(1L).name("user1").email("user1@mail.ru").build();
        user2 = User.builder().id(2L).name("user2").email("user2@mail.ru").build();
        user3 = User.builder().id(3L).name("user3").email("user3@mail.ru").build();
        item11 = Item.builder().id(1L).name("item11").description("iDescr1").available(true)
                .owner(user1).request(null).build();
        item21 = Item.builder().id(2L).name("item21").description("iDescr2").available(true)
                .owner(user1).request(null).build();
        item32 = Item.builder().id(3L).name("item32").description("iDescr3").available(true)
                .owner(user2).request(null).build();
        booking12 = Booking.builder().id(1L).item(item11).booker(user2).status(BookingStatus.APPROVED)
                .start(start).end(end).build();
        booking22 = Booking.builder().id(2L).item(item21).booker(user2).status(BookingStatus.APPROVED)
                .start(start.plusMinutes(10)).end(end.plusMinutes(10)).build();
        booking33 = Booking.builder().id(3L).item(item21).booker(user3).status(BookingStatus.APPROVED)
                .start(start.plusMinutes(20)).end(end.plusMinutes(20)).build();
        booking43 = Booking.builder().id(4L).item(item32).booker(user3).status(BookingStatus.APPROVED)
                .start(start.plusMinutes(30)).end(end.plusMinutes(30)).build();
        userRepository.saveAll(List.of(user1, user2, user3));
        itemRepository.saveAll(List.of(item11, item21, item32));
        bookingRepository.saveAll(List.of(booking12, booking22, booking33, booking43));
    }

    @Test
    void testBookingRepositoryQuery() {
        LocalDateTime testTime = start.plusMinutes(30);
        List<Booking> bookings = bookingRepository.findAllByBookerIdAndStartBeforeAndEndAfter(user2.getId(), testTime);
        List<Booking> bookingsAll = bookingRepository.findAll();
        assertEquals(4, bookingsAll.size());
        assertEquals(2, bookings.size());
        assertEquals(booking12.getItem().getName(), bookings.get(0).getItem().getName());

        bookings = bookingRepository.findAllByBookerIdAndStartBeforeAndEndAfter(user2.getId(), testTime);
        assertEquals(2, bookings.size());
        assertEquals(booking12.getItem().getName(), bookings.get(0).getItem().getName());
        bookings = bookingRepository.findAllByOwnerId(user1.getId());
        assertEquals(3, bookings.size());
        assertEquals(booking12.getItem().getName(), bookings.get(0).getItem().getName());
        assertEquals(booking12.getId(), bookings.get(0).getId());
    }
}
