package org.example.booking;

import jakarta.validation.Valid;
import org.example.booking.dto.BookingPostRequestDto;
import org.example.client.BaseClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;

import java.util.Map;

@Service
public class BookingClient extends BaseClient {
    private static final String API_PREFIX = "/bookings";

    @Autowired
    public BookingClient(@Value("${shareit-server.url}") String serverUrl,
                         RestTemplateBuilder builder) {
        super(
                builder.uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl + API_PREFIX))
                        .build()
        );
    }

    public ResponseEntity<Object> createBooking(long userId, @Valid BookingPostRequestDto requestDto) {
        return post("", userId, requestDto);
    }

    public ResponseEntity<Object> approveBooking(long bookingId, long ownerId, boolean approved) {
        return patch("/" + bookingId + "?approved=" + approved, ownerId);
    }

    public ResponseEntity<Object> getBooking(long userId, long bookingId) {
        return get("/" + bookingId, userId);
    }

    public ResponseEntity<Object> getBookingsOfBooker(long bookerId, String state, int from, int size) {
        Map<String, Object> parameters = Map.of(
                "state", state,
                "from", from,
                "size", size
        );

        return get("?state={state}&from={from}&size={size}", bookerId, parameters);
    }

    public ResponseEntity<Object> getBookingsOfOwner(long ownerId, String state, int from, int size) {
        Map<String, Object> parameters = Map.of(
                "state", state,
                "from", from,
                "size", size
        );
        return get("/owner?state={state}&from={from}&size={size}", ownerId, parameters);
    }
}
