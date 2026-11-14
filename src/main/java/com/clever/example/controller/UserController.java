package com.clever.example.controller;

import com.clever.example.dto.user.UserRequest;
import com.clever.example.dto.user.UserResponse;
import com.clever.example.dto.user.UserUpdateRequest;
import com.clever.example.repository.UserSpecifications;
import com.clever.example.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/v1/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "CRUD de usuarios")
public class UserController {

    private final UserService userService;

    @PostMapping
    @Operation(summary = "Crear un usuario")
    ResponseEntity<UserResponse> createUser(@Valid @RequestBody UserRequest request) {
        UserResponse created = userService.createUser(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(created.id()).toUri();
        return ResponseEntity.created(location).body(created);
    }

    @GetMapping("/{userId}")
    @Operation(summary = "Obtener un usuario por id")
    UserResponse getUserById(@PathVariable long userId) {
        return userService.findById(userId);
    }

    @PatchMapping("/{userId}")
    @Operation(summary = "Actualizar parcialmente un usuario")
    UserResponse updateUser(@PathVariable long userId, @Valid @RequestBody UserUpdateRequest request) {
        return userService.updateUser(userId, request);
    }

    @DeleteMapping("/{userId}")
    @Operation(summary = "Eliminar (borrado lógico) un usuario")
    ResponseEntity<Void> deleteUser(@PathVariable long userId) {
        userService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @Operation(summary = "Listar usuarios con paginación y filtros opcionales")
    PagedModel<UserResponse> findAllUsers(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String lastName,
            @RequestParam(required = false) String email,
            @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.ASC) Pageable pageable) {
        return new PagedModel<>(userService.findAllUsers(
                UserSpecifications.withFilters(name, lastName, email), pageable));
    }
}
