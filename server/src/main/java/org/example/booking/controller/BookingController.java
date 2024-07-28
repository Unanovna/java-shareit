package org.example.booking.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import org.example.booking.InputBookingDto;
import org.example.booking.OutputBookingDto;
import org.example.booking.service.BookingService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
@Validated
public class BookingController {

    private final BookingService bookingService;
    private static final String USER_ID_IN_HEADER = "X-Sharer-User-Id";

    @PostMapping
    public OutputBookingDto create(@RequestHeader(USER_ID_IN_HEADER) long userId,
                                   @Valid @RequestBody InputBookingDto booking) {
        return bookingService.create(booking, userId);
    }

    @PatchMapping("/{bookingId}")
    public OutputBookingDto approveBooking(@RequestHeader(USER_ID_IN_HEADER) Long userId,
                                           @PathVariable Long bookingId,
                                           @RequestParam boolean approved) {
        return bookingService.approveBooking(bookingId, userId, approved);
    }

    @GetMapping("/{bookingId}")
    public OutputBookingDto getById(@RequestHeader(USER_ID_IN_HEADER) Long userId, @PathVariable Long bookingId) {
        return bookingService.getBookingDtoById(bookingId, userId);
    }

    @GetMapping
    public List<OutputBookingDto> getBookingsOfBooker(@RequestHeader(USER_ID_IN_HEADER) Long userId,
                                                      @RequestParam(required = false, defaultValue = "ALL") String state,
                                                      @RequestParam(defaultValue = "0") @PositiveOrZero int from,
                                                      @RequestParam(defaultValue = "30") @Positive int size) {
        return bookingService.getBookingsOfBooker(state, userId, from, size).getContent();
    }

    @GetMapping("/owner")
    public List<OutputBookingDto> getBookingsOfOwner(@RequestHeader(USER_ID_IN_HEADER) Long userId,
                                                     @RequestParam(defaultValue = "ALL") String state,
                                                     @RequestParam(defaultValue = "0") @PositiveOrZero int from,
                                                     @RequestParam(defaultValue = "20") @Positive int size) {
        return bookingService.getBookingsOfOwner(state, userId, from, size).getContent();
    }
}
