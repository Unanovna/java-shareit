package ru.practicum.shareit.request.service;

import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.util.List;

public interface ItemRequestService {
    ItemRequestDto add(ItemRequestDto itemRequestDto, Long userId);

    List<ItemRequestDto> getUserRequests(Long userId, int from, int size);

    List<ItemRequestDto> getOtherUserRequests(Long userId, int from, int size);

    ItemRequestDto getItemRequestById(Long userId, Long requestId);
}