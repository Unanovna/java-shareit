package ru.practicum.shareit.item.controller;

import com.sun.istack.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.service.ItemService;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {
    private static final String USER_ID_IN_HEADER = "X-Sharer-User-Id";
    private final ItemService itemService;

    @PostMapping
    public ItemDto create(@RequestHeader(USER_ID_IN_HEADER) long ownerId, @Valid @RequestBody @NotNull ItemDto itemDto) {

        return itemService.add(ownerId, itemDto);
    }

    @PatchMapping("/{itemId}")
    public ItemDto update(@RequestHeader(USER_ID_IN_HEADER) long ownerId, @PathVariable long itemId,
                          @RequestBody @NotNull Map<String, String> updates) {
        return itemService.update(ownerId, itemId, updates);
    }

    @GetMapping("/{itemId}")
    public ItemDto getItem(@RequestHeader(USER_ID_IN_HEADER) long userId, @PathVariable long itemId) {
        return itemService.getItemDtoById(itemId, userId);
    }

    @GetMapping
    public List<ItemDto> getAllUserItems(@RequestHeader(USER_ID_IN_HEADER) long ownerId,
                                         @Valid @RequestParam(defaultValue = "0") @PositiveOrZero int from,
                                         @Valid @RequestParam(defaultValue = "10") @Positive int size) {

        return itemService.getAllUserItems(ownerId, from, size);
    }

    @GetMapping("/search")
    public List<ItemDto> searchItems(@RequestHeader(USER_ID_IN_HEADER) long userId,
                                     @RequestParam(name = "text") String text,
                                     @RequestParam(defaultValue = "0") @PositiveOrZero int from,
                                     @RequestParam(defaultValue = "10") @Positive int size) {
        if ((text == null) || (text.isBlank())) {
            return List.of();
        }
        return itemService.searchItems(text, from, size);
    }

    @DeleteMapping("/{itemId}")
    public void delete(@RequestHeader(USER_ID_IN_HEADER) long ownerId, @PathVariable long itemId) {
        itemService.delete(ownerId, itemId);
    }

    @PostMapping("/{itemId}/comment")
    public CommentDto addComment(@RequestHeader(USER_ID_IN_HEADER) long userId, @PathVariable long itemId,
                                 @Valid @RequestBody CommentDto commentDto) {
        return itemService.addComment(userId, itemId, commentDto);
    }
}