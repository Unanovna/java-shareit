package org.example.request.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import org.example.request.ItemRequestDto;
import org.example.request.service.ItemRequestService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/requests")
@Validated
public class ItemRequestController {

    private final ItemRequestService itemRequestService;
    private static final String USER_ID_IN_HEADER = "X-Sharer-User-Id";


    @PostMapping
    public ItemRequestDto create(@RequestHeader(USER_ID_IN_HEADER) Long userId,
                                 @RequestBody @Valid ItemRequestDto itemRequestDto) {
        return itemRequestService.add(itemRequestDto, userId);
    }

    @GetMapping
    public List<ItemRequestDto> getUserRequests(@RequestHeader(USER_ID_IN_HEADER) Long userId,
                                                @RequestParam(defaultValue = "0") @PositiveOrZero int from,
                                                @RequestParam(defaultValue = "10") @Positive int size) {
        return itemRequestService.getUserRequests(userId, from, size);
    }

    @GetMapping("/all")
    public List<ItemRequestDto> getOtherUsersRequests(@RequestHeader(USER_ID_IN_HEADER) Long userId,
                                                      @RequestParam(defaultValue = "0") @PositiveOrZero Integer from,
                                                      @RequestParam(defaultValue = "10") @Positive Integer size) {
        return itemRequestService.getOtherUserRequests(userId, from, size).getContent();
    }

    @GetMapping("/{requestId}")
    public ItemRequestDto getRequestById(@RequestHeader(USER_ID_IN_HEADER) Long userId,
                                         @PathVariable Long requestId) {
        return itemRequestService.getItemRequestById(userId, requestId);
    }

}
