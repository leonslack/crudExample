package com.clever.example.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/** Actualización parcial (PATCH): los campos nulos no se modifican. */
public record UserUpdateRequest(
        @Size(min = 1, max = 100) String name,
        @Size(min = 1, max = 100) String lastName,
        @Email @Size(max = 255) String email,
        @Pattern(regexp = "^\\+?[0-9 ()-]{7,20}$", message = "must be a valid phone number") String cellphone,
        @Past LocalDate birthdate,
        @Size(min = 8, max = 72) String password
) {
}
