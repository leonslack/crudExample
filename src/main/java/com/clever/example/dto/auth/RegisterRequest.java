package com.clever.example.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/** Registro publico: siempre crea un usuario con rol USER. */
public record RegisterRequest(
        @NotBlank @Size(max = 100) String name,
        @NotBlank @Size(max = 100) String lastName,
        @NotBlank @Email @Size(max = 255) String email,
        @Pattern(regexp = "^\\+?[0-9 ()-]{7,20}$", message = "must be a valid phone number") String cellphone,
        @Past LocalDate birthdate,
        @NotBlank @Size(min = 8, max = 72) String password
) {
}
