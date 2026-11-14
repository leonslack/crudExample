package com.clever.example.repository;

import com.clever.example.model.User;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

/** Filtros opcionales para el listado de usuarios. */
public final class UserSpecifications {

    private UserSpecifications() {
    }

    public static Specification<User> withFilters(String name, String lastName, String email) {
        return Specification.allOf(
                containsIgnoreCase("name", name),
                containsIgnoreCase("lastName", lastName),
                containsIgnoreCase("email", email));
    }

    private static Specification<User> containsIgnoreCase(String attribute, String value) {
        if (!StringUtils.hasText(value)) {
            return Specification.unrestricted();
        }
        String pattern = "%" + value.trim().toLowerCase() + "%";
        return (root, query, cb) -> cb.like(cb.lower(root.get(attribute)), pattern);
    }
}
