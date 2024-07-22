package ru.practicum.shareit.request.service;

import org.springframework.data.domain.Page;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.util.List;

public interface ItemRequestService {
    ItemRequestDto add(ItemRequestDto itemRequestDto, Long userId);

    List<ItemRequestDto> getUserRequests(Long userId, int from, int size);

    Page<ItemRequestDto> getOtherUserRequests(Long userId, int from, int size);

    ItemRequestDto getItemRequestById(Long userId, Long requestId);
}