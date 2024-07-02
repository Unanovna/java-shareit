package ru.practicum.shareit.service;

import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;

import java.util.List;
import java.util.Map;

public interface ItemService {
    ItemDto add(long ownerId, Item item);

    ItemDto update(long ownerId, long itemId, Map<String, String> updates);

    ItemDto get(long id);

    List<ItemDto> getAll(long ownerId);

    List<ItemDto> searchItems(String query);

    void delete(long ownerId, long itemId);

    void deleteAll();
}
