package org.example.request.service;

import org.example.request.ItemRequestDto;
import org.springframework.data.domain.Page;

import java.util.List;

public interface ItemRequestService {
    ItemRequestDto add(ItemRequestDto itemRequestDto, Long userId);

    List<ItemRequestDto> getUserRequests(Long userId, int from, int size);

    Page<ItemRequestDto> getOtherUserRequests(Long userId, int from, int size);

    ItemRequestDto getItemRequestById(Long userId, Long requestId);
}
