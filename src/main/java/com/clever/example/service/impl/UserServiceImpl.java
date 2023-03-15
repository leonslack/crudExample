package com.clever.example.service.impl;

import com.clever.example.dto.user.UserDto;
import com.clever.example.dto.user.UserMapper;
import com.clever.example.dto.user.UserRequest;
import com.clever.example.model.User;
import com.clever.example.repository.UserRepository;
import com.clever.example.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository repository;
    private final UserMapper userMapper;

    @Override
    public UserDto createUser(UserRequest request) {
        Optional<User> user;
        user = repository.findById(request.getId());
        if (user.isPresent()) {
            return userMapper.toDto(user.get());
        }

        return userMapper.toDto(repository.save(userMapper.toEntity(request)));
    }

    @Override
    public UserDto findById(long userId) {
        User userEntity = findByIdOrThrow(userId);
        return userMapper.toDto(userEntity);
    }

    private User findByIdOrThrow(long userId) {
        return repository.findById(userId)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    @Override
    public UserDto updateUser(long userId, UserRequest request) {
        User userEntity = findByIdOrThrow(userId);

        userMapper.updateModel(request, userEntity);

        return userMapper.toDto(repository.save(userEntity));
    }

    @Override
    public void deleteUser(long userId) {
        User userEntity = findByIdOrThrow(userId);
        userEntity.setIsDeleted(true);
        repository.save(userEntity);
    }

    @Override
    public Page<UserDto> findAllUsers(Pageable pageable, Specification<User> specification) {
        return repository.findAll(specification, pageable).map(userMapper::toDto);
    }
}
