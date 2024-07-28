package org.example.item.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ItemResponseDto {
    List<ItemDto> item;
}
