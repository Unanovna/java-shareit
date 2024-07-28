package org.example.item.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
public class CommentDto {
    private long id;
    private String text;
    private String authorName;
    private String itemName;
    private LocalDateTime created;
}