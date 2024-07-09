package ru.practicum.shareit.item.repository;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import javax.xml.stream.events.Comment;
import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<ru.practicum.shareit.item.model.Comment> findAllByItemId(long itemId, Sort sort);

    List<ru.practicum.shareit.item.model.Comment> findAllByItemIdIn(List<Long> items, Sort sort);
}
