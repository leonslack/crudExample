package com.clever.example.controller;

import com.clever.example.dto.user.UserDto;
import com.clever.example.dto.user.UserRequest;
import com.clever.example.model.User;
import com.clever.example.service.UserService;
import com.sipios.springsearch.anotation.SearchSpec;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    ResponseEntity<UserDto> createUser(@RequestBody UserRequest request) {
        return new ResponseEntity<>(userService.createUser(request), HttpStatus.OK);
    }

    @GetMapping("/{userId}")
    ResponseEntity<UserDto> getUserById(@PathVariable long userId) {
        return new ResponseEntity<>(userService.findById(userId), HttpStatus.OK);
    }

    @PatchMapping("/{userId}")
    ResponseEntity<UserDto> updateUser(@PathVariable long userId, @RequestBody UserRequest request){
        return new ResponseEntity<>(userService.updateUser(userId, request), HttpStatus.OK);
    }

    @DeleteMapping("/{userId}")
    void deleteUser(@PathVariable long userId){
        userService.deleteUser(userId);
    }

    @GetMapping
    Page<UserDto> findAllUsers(@SearchSpec Specification<User> specs, Pageable pageable){
        return userService.findAllUsers(pageable,specs);
    }
}
