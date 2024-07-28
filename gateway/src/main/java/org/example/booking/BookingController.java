package org.example.booking;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import org.example.booking.dto.BookingPostRequestDto;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
@Validated
public class BookingController {
    private final BookingClient bookingClient;
    private static final String USER_ID_IN_HEADER = "X-Sharer-User-Id";

    @PostMapping
    public ResponseEntity<Object> createBooking(@RequestHeader(USER_ID_IN_HEADER) @Positive long userId,
                                                @RequestBody @Valid BookingPostRequestDto requestDto) {
        return bookingClient.createBooking(userId, requestDto);
    }

    @PatchMapping("/{bookingId}")
    public ResponseEntity<Object> approveBooking(@RequestHeader(USER_ID_IN_HEADER) @Positive long ownerId,
                                                 @PathVariable @Positive long bookingId,
                                                 @RequestParam(value = "approved", required = false) boolean approved) {
        return bookingClient.approveBooking(bookingId, ownerId, approved);
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<Object> getBooking(@RequestHeader(USER_ID_IN_HEADER) @Positive long userId,
                                             @PathVariable @Positive long bookingId) {
        return bookingClient.getBooking(userId, bookingId);
    }

    @GetMapping
    public ResponseEntity<Object> getBookingsOfBooker(@RequestHeader(USER_ID_IN_HEADER) long bookerId,
                                                      @RequestParam(name = "state", defaultValue = "ALL")
                                                      String stateParam,
                                                      @RequestParam(name = "from", defaultValue = "0") @PositiveOrZero
                                                      int from,
                                                      @RequestParam(name = "size", defaultValue = "10") @Positive
                                                      int size) {
        return bookingClient.getBookingsOfBooker(bookerId, stateParam, from, size);
    }

    @GetMapping("/owner")
    public ResponseEntity<Object> getBookingsOfOwner(@RequestHeader(USER_ID_IN_HEADER) long ownerId,
                                                     @RequestParam(name = "state", defaultValue = "ALL")
                                                     String stateParam,
                                                     @PositiveOrZero @RequestParam(name = "from", defaultValue = "0")
                                                     int from,
                                                     @Positive @RequestParam(name = "size", defaultValue = "10")
                                                     int size) {
        return bookingClient.getBookingsOfOwner(ownerId, stateParam, from, size);
    }
}