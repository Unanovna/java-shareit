package org.example.user.repository;


import org.example.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {
    List<User> findUsersByEmailEqualsIgnoreCase(String email);

    List<User> findUsersByNameEqualsIgnoreCase(String name);

    boolean existsUserById(Long id);
}
