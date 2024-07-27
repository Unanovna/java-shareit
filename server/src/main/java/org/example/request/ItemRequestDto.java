package org.example.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;
import org.example.item.dto.ItemDto;

import java.time.LocalDateTime;
import java.util.List;

@Builder
@Data
public class ItemRequestDto {
    private Long id;
    private String description;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime created;
    private List<ItemDto> items;
}
