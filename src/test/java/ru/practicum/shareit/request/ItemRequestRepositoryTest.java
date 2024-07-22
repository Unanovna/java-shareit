package ru.practicum.shareit.request;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
class ItemRequestRepositoryTest {
    @Autowired
    ItemRequestRepository itemRequestRepository;

    @Autowired
    UserRepository userRepository;
    final Sort sort = Sort.by(Sort.Direction.DESC, "created");

    User user1, user2;
    ItemRequest itemRequest1, itemRequest2, itemRequest3, itemRequest4;

    @BeforeEach
    void fillTable() {
        LocalDateTime now = LocalDateTime.now();
        user1 = userRepository.save(User.builder().id(1L).name("user1").email("user1@mail.ru").build());
        user2 = userRepository.save(User.builder().id(2L).name("user2").email("user2@mail.ru").build());
        itemRequest1 = ItemRequest.builder()
                .description("description1").requester(user1).created(now.plusSeconds(10)).build();
        itemRequest2 = ItemRequest.builder()
                .description("description2").requester(user1).created(now.plusSeconds(20)).build();
        itemRequest3 = ItemRequest.builder()
                .description("description3").requester(user2).created(now.plusSeconds(30)).build();
        itemRequest4 = ItemRequest.builder()
                .description("description4").requester(user1).created(now.minusSeconds(40)).build();
        itemRequestRepository.save(itemRequest1);
        itemRequestRepository.save(itemRequest2);
        itemRequestRepository.save(itemRequest3);
        itemRequestRepository.save(itemRequest4);
    }

    @Test
    void findAllByRequesterIdOrderByCreatedDescIsOk() {
        PageRequest page = PageRequest.of(0, 10, sort);
        List<ItemRequest> itemRequests = itemRequestRepository.findAllByRequesterIdOrderByCreatedDesc(user1.getId(),
                page);
        assertEquals(3, itemRequests.size());
        assertEquals(itemRequest2.getDescription(), itemRequests.get(0).getDescription());
        assertEquals(4, itemRequestRepository.findAll().size());
    }

    @Test
    void findAllByRequesterIdOrderByCreatedDesc_testPage() {
        PageRequest page = PageRequest.of(0, 2, sort);
        List<ItemRequest> itemRequests = itemRequestRepository.findAllByRequesterIdOrderByCreatedDesc(user1.getId(),
                page);
        assertEquals(2, itemRequests.size());
        assertEquals(itemRequest2.getDescription(), itemRequests.get(0).getDescription());
        assertEquals(4, itemRequestRepository.findAll().size());
    }

    @Test
    void findAllByRequesterIdNotIsOk() {
        PageRequest page = PageRequest.of(0, 10, sort);
        List<ItemRequest> itemRequests = itemRequestRepository.findAllByRequesterIdNot(user1.getId(), page).getContent();
        assertEquals(1, itemRequests.size());
        assertEquals(itemRequest3.getDescription(), itemRequests.get(0).getDescription());
        assertEquals(4, itemRequestRepository.findAll().size());
    }
}