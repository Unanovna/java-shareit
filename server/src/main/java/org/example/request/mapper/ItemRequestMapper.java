package org.example.request.mapper;

import org.example.item.mapper.ItemMapper;
import org.example.item.model.Item;
import org.example.request.ItemRequestDto;
import org.example.request.model.ItemRequest;

import java.util.List;
import java.util.stream.Collectors;

public class ItemRequestMapper {

    public static ItemRequestDto toItemRequestDto(ItemRequest itemRequest) {
        return ItemRequestDto.builder()
                .id(itemRequest.getId())
                .description(itemRequest.getDescription())
                .created(itemRequest.getCreated())
                .build();
    }

    public static ItemRequestDto toItemRequestDto(ItemRequest itemRequest, List<Item> items) {
        ItemRequestDto itemRequestDto = toItemRequestDto(itemRequest);
        itemRequestDto.setItems(items
                .stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList()));
        return itemRequestDto;
    }
}
