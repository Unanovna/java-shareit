package org.example.item.service;

import org.example.item.dto.CommentDto;
import org.example.item.dto.ItemDto;
import org.example.item.model.Item;
import org.springframework.data.domain.Page;

import java.util.Map;

public interface ItemService {
    ItemDto add(long ownerId, ItemDto itemDto);

    ItemDto update(Long ownerId, Long itemId, Map<String, String> updates);

    Item getItemById(Long itemId);

    Long getOwnerId(Long itemId);

    ItemDto getItemDtoById(Long itemId, Long userId);

    Page<ItemDto> getAllUserItems(Long userId, int from, int size);

    Page<ItemDto> searchItems(String query, int from, int size);

    void delete(Long ownerId, Long itemId);

    void deleteAll();

    CommentDto addComment(Long userId, Long itemId, CommentDto commentDto);
}
