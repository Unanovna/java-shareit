package org.example.booking.service;

import org.example.booking.InputBookingDto;
import org.example.booking.OutputBookingDto;
import org.example.booking.model.Booking;
import org.springframework.data.domain.Page;

public interface BookingService {

    OutputBookingDto create(InputBookingDto bookingDto, Long userId);

    OutputBookingDto approveBooking(Long bookingId, Long userId, Boolean approve);

    Booking getBookingById(Long bookingId, Long userId);

    OutputBookingDto getBookingDtoById(Long bookingId, Long userId);

    Page<OutputBookingDto> getBookingsOfBooker(String state, Long bookerId, int from, int size);

    Page<OutputBookingDto> getBookingsOfOwner(String state, Long ownerId, int from, int size);
}
