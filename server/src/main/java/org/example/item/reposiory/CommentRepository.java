package org.example.item.reposiory;

import org.example.item.model.Comment;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findAllByItemId(long itemId, Sort sort);

    List<Comment> findAllByItemIdIn(List<Long> items, Sort sort);
}