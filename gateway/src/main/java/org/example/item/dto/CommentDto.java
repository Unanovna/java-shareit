package org.example.item.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class CommentDto {
    private long id;
    @NotBlank(message = "Comment not be empty")
    private String text;
    private String authorName;
    private String itemName;
    private LocalDateTime created;
}