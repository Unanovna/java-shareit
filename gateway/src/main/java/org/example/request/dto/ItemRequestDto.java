package org.example.request.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;
import org.example.item.dto.ItemDto;

import java.awt.*;
import java.time.LocalDateTime;

@Builder
@Data
public class ItemRequestDto {
    private Long id;
    @NotBlank
    private String description;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime created;
    private List items;
}
