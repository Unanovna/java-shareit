package org.example.item;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.groups.Default;
import lombok.RequiredArgsConstructor;
import org.example.item.dto.CommentDto;
import org.example.item.dto.ItemDto;
import org.example.validationGroup.Create;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping(path = "/items")
@RequiredArgsConstructor
@Validated
public class ItemController {
    private static final String USER_ID_IN_HEADER = "X-Sharer-User-Id";
    private final ItemClient itemClient;

    @PostMapping
    public ResponseEntity<Object> createItem(@RequestHeader(USER_ID_IN_HEADER) @Positive long userId,
                                             @RequestBody @Validated({Create.class, Default.class}) @NotNull
                                             ItemDto itemDto) {
        return itemClient.createItem(userId, itemDto);
    }

    @PatchMapping("/{itemId}")
    public ResponseEntity<Object> updateItem(@RequestHeader(USER_ID_IN_HEADER) @Positive Long userId,
                                             @PathVariable @Positive long itemId,
                                             @RequestBody @Valid @NotNull ItemDto itemDto) {
        return itemClient.updateItem(userId, itemId, itemDto);
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<Object> getItemById(@RequestHeader(USER_ID_IN_HEADER) @Positive long userId,
                                              @PathVariable("itemId") @Positive long itemId) {
        return itemClient.getItemById(userId, itemId);
    }

    @GetMapping
    public ResponseEntity<Object> getAllUserItems(@RequestHeader(USER_ID_IN_HEADER) @Positive long userId,
                                                  @RequestParam(defaultValue = "0") @PositiveOrZero int from,
                                                  @RequestParam(defaultValue = "10") @Positive int size) {

        return itemClient.getAllUserItems(userId, from, size);
    }

    @GetMapping("/search")
    public ResponseEntity<Object> searchItems(@RequestHeader(USER_ID_IN_HEADER) @Positive long userId,
                                              @RequestParam(name = "text") String text,
                                              @RequestParam(defaultValue = "0") @PositiveOrZero int from,
                                              @RequestParam(defaultValue = "10") @Positive int size) {
        if (text.isBlank()) {
            return new ResponseEntity<>(List.of(), HttpStatus.OK);
        }
        return itemClient.searchItems(userId, text, from, size);
    }

    @PostMapping("/{itemId}/comment")
    public ResponseEntity<Object> addComment(@RequestHeader(USER_ID_IN_HEADER) @Positive long userId,
                                             @PathVariable @Positive long itemId,
                                             @RequestBody @Valid @NotNull CommentDto commentDto) {
        return itemClient.addComment(userId, itemId, commentDto);
    }

    @DeleteMapping("/{itemId}")
    ResponseEntity<Object> delete(@RequestHeader(USER_ID_IN_HEADER) @Positive long ownerId,
                                  @PathVariable @Positive long itemId) {
        return itemClient.deleteItem(ownerId, itemId);
    }
}
