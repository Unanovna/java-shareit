package ru.practicum.shareit.booking.dto;

import lombok.*;

import javax.validation.constraints.Future;
import javax.validation.constraints.FutureOrPresent;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor(force = true)
public class InputBookingDto {
    @NotNull
    private Long itemId;
    @NotNull(message = "Date must be filled!")
    @FutureOrPresent(message = "Start must be current or future!")
    private LocalDateTime start;
    @NotNull(message = "Date must be filled!")
    @Future(message = "End must be future!")
    private LocalDateTime end;
}
