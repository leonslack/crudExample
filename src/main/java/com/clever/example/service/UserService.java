package com.clever.example.service;

import com.clever.example.dto.user.UserDto;
import com.clever.example.dto.user.UserRequest;
import com.clever.example.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

public interface UserService {
    UserDto createUser(UserRequest request);

    UserDto findById(long userId);

    UserDto updateUser(long userId,UserRequest request);

    void deleteUser(long userId);

    Page<UserDto> findAllUsers(Pageable pageable, Specification<User> specification);
}
