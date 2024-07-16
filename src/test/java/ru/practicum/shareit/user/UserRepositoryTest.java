package ru.practicum.shareit.user;

import org.jeasy.random.EasyRandom;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@DirtiesContext
class UserRepositoryTest {
    @Autowired
    UserRepository userRepository;
    private final EasyRandom generator = new EasyRandom();

    @Test
    void findUsersByEmailEqualsIgnoreCaseIsOk() {
        String email1 = "user1@mail.ru";
        String name1 = "user1";
        userRepository.save(User.builder().email(email1).name(name1).build());
        userRepository.save(User.builder().email("user2@mail.ru").name("user2").build());
        User user = generator.nextObject(User.class);
        userRepository.save(user);
        List<User> users = userRepository.findUsersByEmailEqualsIgnoreCase(email1);
        assertEquals(1, users.size());
        assertEquals(name1, users.get(0).getName());
        assertEquals(1L, users.get(0).getId());
    }

    @Test
    @DirtiesContext
    void indUsersByNameEqualsIgnoreCaseIsOk() {
        String email1 = "user1@mail.ru";
        String name1 = "user1";
        userRepository.save(User.builder().email(email1).name(name1).build());
        userRepository.save(User.builder().email("user2@mail.ru").name("user2").build());
        List<User> users = userRepository.findUsersByNameEqualsIgnoreCase(name1);
        assertEquals(1, users.size());
        assertEquals(name1, users.get(0).getName());
    }
}
