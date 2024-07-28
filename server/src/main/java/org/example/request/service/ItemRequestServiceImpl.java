package org.example.request.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.exception.NotFoundException;
import org.example.item.model.Item;
import org.example.item.reposiory.ItemRepository;
import org.example.request.ItemRequestDto;
import org.example.request.mapper.ItemRequestMapper;
import org.example.request.model.ItemRequest;
import org.example.request.repository.ItemRequestRepository;
import org.example.user.model.User;
import org.example.user.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.example.util.PageUtil.getPageRequest;

@Service
@RequiredArgsConstructor
public class ItemRequestServiceImpl implements ItemRequestService {
    private final ItemRequestRepository itemRequestRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    @Transactional
    @Override
    public ItemRequestDto add(ItemRequestDto itemRequestDto, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(String.format("User with id %d not found", userId)));
        ItemRequest itemRequest = itemRequestRepository.save(ItemRequest.builder()
                .description(itemRequestDto.getDescription())
                .created(LocalDateTime.now())
                .requester(user)
                .build());
        return ItemRequestMapper.toItemRequestDto(itemRequest);
    }

    @Override
    public List<ItemRequestDto> getUserRequests(Long userId, int from, int size) {
        existsUserById(userId);
        return itemRequestsToDto(itemRequestRepository.findAllByRequesterIdOrderByCreatedDesc(userId,
                getPageRequest(from, size)));
    }

    @Override
    public Page<ItemRequestDto> getOtherUserRequests(Long userId, int from, int size) {
        existsUserById(userId);
        Pageable pageRequest = getPageRequest(from, size);
        Page<ItemRequest> allByRequesterIdNot = itemRequestRepository
                .findAllByRequesterIdNot(userId, pageRequest);
        return new PageImpl<>(itemRequestsToDto(allByRequesterIdNot.getContent()), pageRequest,
                allByRequesterIdNot.getTotalElements());
    }

    @Override
    public ItemRequestDto getItemRequestById(Long userId, Long requestId) {
        existsUserById(userId);
        ItemRequest itemRequest = getItemRequestById(requestId);
        return ItemRequestMapper.toItemRequestDto(itemRequest, itemRepository.findAllByRequestId(requestId));
    }

    private List<ItemRequestDto> itemRequestsToDto(List<ItemRequest> itemRequests) {
        List<Long> itemRequestsIds = itemRequests
                .stream()
                .map(ItemRequest::getId)
                .collect(Collectors.toList());
        Map<Long, List<Item>> itemsByRequest = itemRepository.findAllByRequestIdIn(itemRequestsIds)
                .stream()
                .collect(Collectors.groupingBy(item -> item.getRequest().getId()));
        return itemRequests
                .stream()
                .map(r -> ItemRequestMapper.toItemRequestDto(r, itemsByRequest.getOrDefault(r.getId(), List.of())))
                .collect(Collectors.toList());
    }

    private void existsUserById(Long userId) {
        if (!(userRepository.existsUserById(userId))) {
            throw new NotFoundException(String.format("User with id: %d not found", userId));
        }
    }

    private ItemRequest getItemRequestById(Long id) {
        return itemRequestRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(String.format("ItemRequest with id: %d is not found", id)));
    }
}
