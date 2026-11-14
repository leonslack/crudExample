package com.clever.example.service;

import com.clever.example.dto.auth.LoginRequest;
import com.clever.example.dto.auth.RegisterRequest;
import com.clever.example.dto.auth.TokenResponse;
import com.clever.example.dto.user.UserRequest;
import com.clever.example.dto.user.UserResponse;
import com.clever.example.model.Role;
import com.clever.example.repository.UserRepository;
import com.clever.example.security.TokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository repository;
    private final UserService userService;
    private final TokenService tokenService;

    public UserResponse register(RegisterRequest request) {
        return userService.createUser(new UserRequest(request.name(), request.lastName(), request.email(),
                request.cellphone(), request.birthdate(), request.password(), Role.USER));
    }

    public TokenResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(request.email(), request.password()));
        return repository.findByEmailIgnoreCase(request.email())
                .map(tokenService::issueToken)
                .orElseThrow(() -> new BadCredentialsException("Bad credentials"));
    }
}
