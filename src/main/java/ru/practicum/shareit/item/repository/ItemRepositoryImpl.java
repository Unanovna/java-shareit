package ru.practicum.shareit.item.repository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.model.Item;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Repository
@Qualifier("itemRepository")
public class ItemRepositoryImpl implements ItemRepository {
    private final Map<Long, Item> items = new HashMap<>();
    private long lastId = 0L;

    @Override
    public Item add(Item item) {
        item.setId(++lastId);
        log.info("New item added: {}", item);
        items.put(lastId, item);
        return item;
    }

    @Override
    public Item update(Item item) {
        items.put(item.getId(), item);
        log.info("Item updated {}", item);
        return item;
    }

    @Override
    public List<Item> getAll() {
        return new ArrayList<>(items.values());
    }

    @Override
    public List<Item> getAllByOwner(long ownerId) {
        return new ArrayList<>(items.values().stream()
                .filter(i -> (i.getOwner() != null && i.getOwner().getId() == ownerId))
                .collect(Collectors.toList()));
    }

    @Override
    public Item get(long id) {
        Item item = items.get(id);
        if (item != null) {
            return item;
        }
        log.info("Item with id:{} not exists.", id);
        throw new NotFoundException(String.format("Item with id: %d is not exist", id));
    }

    @Override
    public void delete(long itemId) {
        get(itemId);
        items.remove(itemId);
    }

    @Override
    public void deleteAll() {
        lastId = 0;
        items.clear();
    }
}
