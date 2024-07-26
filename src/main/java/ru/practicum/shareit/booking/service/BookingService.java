package ru.practicum.shareit.booking.service;

import org.springframework.data.domain.Page;
import ru.practicum.shareit.booking.dto.InputBookingDto;
import ru.practicum.shareit.booking.dto.OutputBookingDto;
import ru.practicum.shareit.booking.model.Booking;


public interface BookingService {

    OutputBookingDto create(InputBookingDto bookingDto, Long userId);

    OutputBookingDto approveBooking(Long bookingId, Long userId, Boolean approve);

    Booking getBookingById(Long bookingId, Long userId);

    OutputBookingDto getBookingDtoById(Long bookingId, Long userId);

    Page<OutputBookingDto> getBookingsOfBooker(String state, Long bookerId, int from, int size);

    Page<OutputBookingDto> getBookingsOfOwner(String state, Long ownerId, int from, int size);
}