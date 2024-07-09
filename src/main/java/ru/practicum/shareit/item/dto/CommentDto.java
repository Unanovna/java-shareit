package ru.practicum.shareit.item.dto;

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;

@Data
@Builder
public class CommentDto {
    private long id;
    @NotBlank(message = "Comment not be empty")
    private String text;
    private String authorName;
    private String itemName;
    private LocalDateTime created;
}
