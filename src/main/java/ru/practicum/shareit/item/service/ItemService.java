package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;


import java.util.List;
import java.util.Map;

public interface ItemService {
    ItemDto add(long ownerId, Item item);

    ItemDto update(Long ownerId, Long itemId, Map<String, String> updates);

    Item getItemById(Long itemId);

    Long getOwnerId(Long itemId);

    ItemDto getItemDtoById(Long itemId, Long userId);

    List<ItemDto> getAllUserItems(Long userId);

    List<ItemDto> searchItems(Long userId, String query);

    void delete(Long ownerId, Long itemId);

    void deleteAll();

    CommentDto addComment(Long userId, Long itemId, CommentDto commentDto);
}