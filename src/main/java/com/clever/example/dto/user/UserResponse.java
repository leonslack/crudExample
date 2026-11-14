package com.clever.example.dto.user;

import com.clever.example.model.Role;

import java.time.Instant;
import java.time.LocalDate;
import java.time.Period;

public record UserResponse(
        Long id,
        String name,
        String lastName,
        String email,
        String cellphone,
        LocalDate birthdate,
        Integer age,
        Role role,
        Instant createdAt,
        Instant updatedAt
) {
    public static Integer ageFrom(LocalDate birthdate) {
        return birthdate == null ? null : Period.between(birthdate, LocalDate.now()).getYears();
    }
}
