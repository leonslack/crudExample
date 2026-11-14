package com.clever.example.service.impl;

import com.clever.example.dto.user.UserMapper;
import com.clever.example.dto.user.UserRequest;
import com.clever.example.dto.user.UserResponse;
import com.clever.example.dto.user.UserUpdateRequest;
import com.clever.example.exception.ConflictException;
import com.clever.example.exception.ResourceNotFoundException;
import com.clever.example.model.User;
import com.clever.example.repository.UserRepository;
import com.clever.example.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository repository;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public UserResponse createUser(UserRequest request) {
        if (repository.existsByEmailIgnoreCase(request.email())) {
            throw new ConflictException("Email already in use: " + request.email());
        }
        return userMapper.toResponse(repository.save(userMapper.toEntity(request)));
    }

    @Override
    public UserResponse findById(long userId) {
        return userMapper.toResponse(findByIdOrThrow(userId));
    }

    @Override
    @Transactional
    public UserResponse updateUser(long userId, UserUpdateRequest request) {
        User user = findByIdOrThrow(userId);
        if (request.email() != null && repository.existsByEmailIgnoreCaseAndIdNot(request.email(), userId)) {
            throw new ConflictException("Email already in use: " + request.email());
        }
        userMapper.updateEntity(request, user);
        return userMapper.toResponse(repository.saveAndFlush(user));
    }

    @Override
    @Transactional
    public void deleteUser(long userId) {
        repository.delete(findByIdOrThrow(userId));
    }

    @Override
    public Page<UserResponse> findAllUsers(Specification<User> specification, Pageable pageable) {
        return repository.findAll(specification, pageable).map(userMapper::toResponse);
    }

    private User findByIdOrThrow(long userId) {
        return repository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id " + userId));
    }
}
