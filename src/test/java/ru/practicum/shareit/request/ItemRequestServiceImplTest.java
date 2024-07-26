package ru.practicum.shareit.request;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.mapper.ItemRequestMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.request.service.ItemRequestServiceImpl;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;
import ru.practicum.shareit.util.PageUtil;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemRequestServiceImplTest {
    @Mock
    UserRepository userRepository;
    @Mock
    ItemRequestRepository itemRequestRepository;
    @Mock
    ItemRepository itemRepository;
    @InjectMocks
    ItemRequestServiceImpl itemRequestService;

    User user;
    Item item;
    ItemRequest itemRequest;
    ItemRequestDto itemRequestDto;
    ItemRequestDto inputItemRequestDto;

    @BeforeEach
    void beforeEach() {
        user = User.builder().id(1L).name("user1").email("user1@mail.ru").build();
        item = Item.builder().name("item1").description("item description1").available(true).owner(user).build();
        inputItemRequestDto = ItemRequestDto.builder().description("Input item Request description").build();
        itemRequest = ItemRequest.builder().id(1L).created(LocalDateTime.now())
                .description("Item Request description")
                .requester(user).items(List.of(item)).build();
        itemRequestDto = ItemRequestMapper.toItemRequestDto(itemRequest);
        item.setRequest(itemRequest);
    }

    @Test
    void addWithOk() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        when(itemRequestRepository.save(any())).thenReturn(itemRequest);
        itemRequest.setDescription(inputItemRequestDto.getDescription());
        ItemRequestDto actual = itemRequestService.add(inputItemRequestDto, user.getId());
        assertEquals(1L, actual.getId());
        assertEquals(inputItemRequestDto.getDescription(), actual.getDescription());
        assertNotNull(actual.getCreated());
        verify(userRepository).findById(anyLong());
        verify(itemRequestRepository).save(any(ItemRequest.class));
    }

    @Test
    void addWithNotFoundUser() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> itemRequestService.add(inputItemRequestDto, user.getId()));
        verify(itemRequestRepository, never()).save(any(ItemRequest.class));
    }

    @Test
    void getUserRequestsWithOk() {
        when(userRepository.existsUserById(anyLong())).thenReturn(true);
        when(itemRequestRepository.findAllByRequesterIdOrderByCreatedDesc(anyLong(), any()))
                .thenReturn(List.of(itemRequest));
        when(itemRepository.findAllByRequestIdIn(any())).thenReturn(List.of());
        List<ItemRequestDto> actual = itemRequestService.getUserRequests(user.getId(), 1, 1);
        assertFalse(actual.isEmpty());
        assertEquals(1, actual.size());
        assertEquals(itemRequest.getId(), actual.get(0).getId());
        assertEquals(itemRequest.getDescription(), actual.get(0).getDescription());
        assertEquals(List.of(), actual.get(0).getItems());
    }

    @Test
    void getOtherUserRequestsWithOk() {
        when(userRepository.existsUserById(anyLong())).thenReturn(true);
        when(itemRequestRepository.findAllByRequesterIdNot(anyLong(), any())).thenReturn(
                new PageImpl<>(List.of(itemRequest), PageUtil.getPageRequest(0, 1), 1));
        when(itemRepository.findAllByRequestIdIn(any())).thenReturn(List.of());
        List<ItemRequestDto> actual = itemRequestService.getOtherUserRequests(user.getId(), 0, 1)
                .getContent();
        assertFalse(actual.isEmpty());
        assertEquals(1, actual.size());
        assertEquals(itemRequest.getId(), actual.get(0).getId());
        assertEquals(itemRequest.getDescription(), actual.get(0).getDescription());
        assertEquals(List.of(), actual.get(0).getItems());
    }

    @Test
    void findByIdWithOk() {
        when(userRepository.existsUserById(anyLong())).thenReturn(true);
        when(itemRequestRepository.findById(anyLong())).thenReturn(Optional.of(itemRequest));
        ItemRequestDto actual = itemRequestService.getItemRequestById(user.getId(), itemRequest.getId());
        assertEquals(1L, actual.getId());
        assertEquals(itemRequest.getDescription(), actual.getDescription());
        assertNotNull(actual.getCreated());
        assertEquals(List.of(), actual.getItems());
    }
}