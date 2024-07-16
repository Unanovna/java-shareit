package ru.practicum.shareit.booking.mapper;

import ru.practicum.shareit.booking.dto.OutputBookingDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.dto.ShortBookingDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.user.mapper.UserMapper;

import java.util.List;
import java.util.stream.Collectors;

public class BookingMapper {
    private BookingMapper() {
    }

    public static OutputBookingDto toOutputBookingDto(Booking booking) {
        return booking == null ? null : OutputBookingDto.builder()
                .id(booking.getId())
                .start(booking.getStart())
                .end(booking.getEnd())
                .item(ItemMapper.toItemDto(booking.getItem()))
                .booker(UserMapper.toUserDto(booking.getBooker()))
                .status(booking.getStatus())
                .build();
    }

    public static List<OutputBookingDto> toOutputsBookingDtoList(List<Booking> bookings) {
        return bookings.stream()
                .map(BookingMapper::toOutputBookingDto)
                .collect(Collectors.toList());
    }

    public static ShortBookingDto toShortBookingDto(Booking booking) {
        return booking == null ? null : ShortBookingDto.builder()
                .id(booking.getId())
                .start(booking.getStart())
                .end(booking.getEnd())
                .item(ItemMapper.toItemDto(booking.getItem()))
                .bookerId(booking.getBooker().getId())
                .build();
    }
}
