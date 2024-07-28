package org.example.item.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.example.booking.ShortBookingDto;
import org.example.user.model.User;

import java.util.List;

@Data
@AllArgsConstructor
@Builder
public class ItemDto {
    private Long id;
    private String name;
    private String description;
    private Boolean available;
    @JsonIgnore
    private User owner;
    private Long requestId;
    private ShortBookingDto lastBooking;
    private ShortBookingDto nextBooking;
    private List<CommentDto> comments;
}
