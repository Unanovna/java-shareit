package ru.practicum.shareit.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import javax.validation.ValidationException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    @Override
    public ItemDto add(long ownerId, Item item) {
        User owner = userRepository.getUserById(ownerId);
        item.setOwner(owner);
        return ItemMapper.toItemDto(itemRepository.add(item));
    }

    @Override
    public ItemDto update(long ownerId, long itemId, Map<String, String> updates) {
        Item item = itemRepository.getAllByOwner(ownerId).stream()
                .filter(i -> i.getId() == itemId)
                .findFirst()
                .orElseThrow(() -> new NotFoundException(String.format("Item id: %s owner id: %s is not found.", itemId,
                        ownerId)));

        if (updates.containsKey("name")) {
            String value = updates.get("name");
            checkString(value, "Name");
            log.info("Change name item {} owner {}", itemId, ownerId);
            item.setName(value);
        }
        if (updates.containsKey("description")) {
            String value = updates.get("description");
            checkString(value, "Name");
            item.setDescription(value);
        }
        if (updates.containsKey("available")) {
            item.setAvailable(Boolean.valueOf(updates.get("available")));
        }
        return ItemMapper.toItemDto(item);
    }

    private void checkString(String value, String name) {
        if (value == null || value.isBlank()) {
            log.info("{} item is empty!", name);
            throw new ValidationException(String.format("%s item is empty!", name));
        }
    }

    @Override
    public ItemDto get(long itemId) {
        return ItemMapper.toItemDto(itemRepository.get(itemId));
    }

    @Override
    public List<ItemDto> getAll(long ownerId) {
        return ItemMapper.toItemDtoList(itemRepository.getAllByOwner(ownerId));
    }

    @Override
    public void delete(long ownerId, long itemId) {
        itemRepository.getAllByOwner(ownerId).stream()
                .filter(i -> i.getId() == itemId)
                .findFirst()
                .orElseThrow(() -> new NotFoundException(String.format("Item id: %s owner id: %s is not found.", itemId,
                        ownerId)));
        itemRepository.delete(itemId);
    }

    @Override
    public void deleteAll() {
        itemRepository.deleteAll();
    }

    @Override
    public List<ItemDto> searchItems(String query) {
        if (query == null || query.isBlank()) {
            return Collections.emptyList();
        }
        String searchLine = query.toLowerCase();
        return itemRepository.getAll()
                .stream()
                .filter(i -> (i.getDescription().toLowerCase().contains(searchLine)
                        || i.getName().toLowerCase().contains(searchLine))
                        && i.getAvailable())
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }
}
