package com.clever.example.service;

import com.clever.example.dto.user.UserRequest;
import com.clever.example.dto.user.UserResponse;
import com.clever.example.dto.user.UserUpdateRequest;
import com.clever.example.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

public interface UserService {

    UserResponse createUser(UserRequest request);

    UserResponse findById(long userId);

    UserResponse updateUser(long userId, UserUpdateRequest request);

    void deleteUser(long userId);

    Page<UserResponse> findAllUsers(Specification<User> specification, Pageable pageable);
}
