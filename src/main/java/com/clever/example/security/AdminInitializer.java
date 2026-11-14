package com.clever.example.security;

import com.clever.example.model.Role;
import com.clever.example.model.User;
import com.clever.example.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/** Crea el primer administrador a partir de ADMIN_EMAIL / ADMIN_PASSWORD si aun no existe. */
@Slf4j
@Component
@RequiredArgsConstructor
public class AdminInitializer implements ApplicationRunner {

    private final SecurityProperties properties;
    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        SecurityProperties.Admin admin = properties.admin();
        if (admin == null || !StringUtils.hasText(admin.password())) {
            log.info("ADMIN_PASSWORD not set: skipping admin user creation");
            return;
        }
        if (repository.existsByEmailIgnoreCase(admin.email())) {
            return;
        }
        User user = new User();
        user.setName("Admin");
        user.setLastName("Admin");
        user.setEmail(admin.email());
        user.setPassword(passwordEncoder.encode(admin.password()));
        user.setRole(Role.ADMIN);
        repository.save(user);
        log.info("Admin user {} created", admin.email());
    }
}
