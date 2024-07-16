package ru.practicum.shareit.item.repository;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.shareit.item.model.Item;

import java.awt.print.Pageable;
import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Long> {
    List<Item> findAllByOwnerId(Long ownerId, PageRequest pageable);

    @Query(value = "select i from Item i " +
            "where( lower(i.name) like lower(concat('%',?1,'%')) "
            + " or lower(i.description) like lower(concat('%',?1,'%')))"
            + " and i.available=true")
    List<Item> searchAvailableItems(String text, Pageable pageable);

    List<Item> findAllByRequestIdIn(List<Long> requestIds);

    List<Item> findAllByRequestId(Long requestId);
}