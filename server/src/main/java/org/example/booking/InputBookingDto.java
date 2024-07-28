package org.example.booking;

import lombok.*;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InputBookingDto {
    private Long itemId;
    private LocalDateTime start;
    private LocalDateTime end;
}