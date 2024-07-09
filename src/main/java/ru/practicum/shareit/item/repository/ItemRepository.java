package ru.practicum.shareit.item.repository;

import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemRepository {
    Item add(Item item);

    Item update(Item item);

    List<Item> getAll();

    List<Item> getAllByOwner(long ownerId);

    Item get(long id);

    void delete(long userId);

    void deleteAll();
}
