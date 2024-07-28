package org.example.user.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.exception.NotFoundException;
import org.example.user.UserDto;
import org.example.user.mapper.UserMapper;
import org.example.user.model.User;
import org.example.user.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Override
    public UserDto add(User user) {
        return UserMapper.toUserDto(userRepository.save(user));
    }

    @Transactional
    @Override
    public UserDto update(Long userId, UserDto userDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(String.format("User with id %d not found", userId)));
        int i = 0;
        if ((userDto.getName() != null) && (!userDto.getName().isBlank())) {
            i++;
            user.setName(userDto.getName());
        }
        if ((userDto.getEmail() != null) && (!userDto.getEmail().isBlank())) {
            i++;
            user.setEmail(userDto.getEmail());
        }
        if (i > 0) {
            userRepository.save(user);
        }
        return UserMapper.toUserDto(user);
    }

    @Transactional
    @Override
    public User getUserById(long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(String.format("User with id %d not found", id)));
    }

    @Override
    public UserDto getUserDtoById(long id) {
        return UserMapper.toUserDto(getUserById(id));
    }

    @Transactional
    @Override
    public List<UserDto> getAll() {
        return UserMapper.toUserDtoList(userRepository.findAll());
    }

    @Transactional
    @Override
    public void delete(long userId) {
        userRepository.deleteById(userId);
    }

    @Transactional
    @Override
    public void deleteAll() {
        userRepository.deleteAll();
    }
}
