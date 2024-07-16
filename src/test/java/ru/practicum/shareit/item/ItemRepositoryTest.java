package ru.practicum.shareit.item;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.repository.UserRepository;

import java.awt.print.Pageable;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
class ItemRepositoryTest {
    @Autowired
    ItemRepository itemRepository;

    @Autowired
    UserRepository userRepository;

    @Test
    void searchAvailableItemsIsOk() {
        Item item1 = Item.builder().name("iteMs1").description("Description1").available(true)
                .owner(null).request(null).build();
        Item item2 = Item.builder().name("item2").description("Description2").available(false)
                .owner(null).request(null).build();
        Item item3 = Item.builder().name("iteM3").description("Description3").available(true)
                .owner(null).request(null).build();
        Item item4 = Item.builder().name("yTem4").description("Description4").available(true)
                .owner(null).request(null).build();
        Item item5 = Item.builder().name("Table5").description("Description itEm5").available(true)
                .owner(null).request(null).build();
        itemRepository.saveAll(List.of(item1, item2, item3, item4, item5));
        PageRequest page = PageRequest.of(0, 10);
        List<Item> itemAll = itemRepository.findAll();
        List<Item> items = itemRepository.searchAvailableItems("iTem", (Pageable) page);
        assertEquals(5, itemAll.size());
        assertEquals(3, items.size());
        assertEquals(item3.getDescription(), items.get(1).getDescription());
        assertEquals(item1.getId(), items.get(0).getId());
    }
}