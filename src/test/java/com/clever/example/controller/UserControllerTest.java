package com.clever.example.controller;

import com.clever.example.TestcontainersConfiguration;
import com.clever.example.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;

import static org.hamcrest.Matchers.endsWith;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class UserControllerTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    UserRepository repository;

    @BeforeEach
    void cleanDatabase() {
        repository.deleteAllInBatch();
    }

    private static String userJson(String email) {
        return """
                {"name":"Ana","lastName":"Pérez","email":"%s","cellphone":"+57 300 123 4567",
                 "birthdate":"1990-05-20","password":"s3cret-pass"}
                """.formatted(email);
    }

    private static JwtRequestPostProcessor admin() {
        return jwt().jwt(j -> j.subject("0").claim("roles", List.of("ADMIN")))
                .authorities(new SimpleGrantedAuthority("ROLE_ADMIN"));
    }

    private static JwtRequestPostProcessor user(long id) {
        return jwt().jwt(j -> j.subject(String.valueOf(id)))
                .authorities(new SimpleGrantedAuthority("ROLE_USER"));
    }

    private static MockHttpServletRequestBuilder json(MockHttpServletRequestBuilder builder, String body) {
        return builder.contentType(MediaType.APPLICATION_JSON).content(body);
    }

    private long createUser(String email) throws Exception {
        MvcResult result = mvc.perform(json(post("/v1/users"), userJson(email)).with(admin()))
                .andExpect(status().isCreated())
                .andReturn();
        String location = result.getResponse().getHeader("Location");
        return Long.parseLong(location.substring(location.lastIndexOf('/') + 1));
    }

    @Test
    void createReturns201WithLocationAndComputedAge() throws Exception {
        int expectedAge = Period.between(LocalDate.of(1990, 5, 20), LocalDate.now()).getYears();

        mvc.perform(json(post("/v1/users"), userJson("ana@example.com")).with(admin()))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", endsWith("/v1/users/" + repository.findAll().getFirst().getId())))
                .andExpect(jsonPath("$.email").value("ana@example.com"))
                .andExpect(jsonPath("$.age").value(expectedAge))
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.role").value("USER"))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void createWithInvalidDataReturns400WithFieldErrors() throws Exception {
        mvc.perform(json(post("/v1/users"), "{\"name\":\"\",\"email\":\"not-an-email\"}").with(admin()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.name").exists())
                .andExpect(jsonPath("$.errors.lastName").exists())
                .andExpect(jsonPath("$.errors.email").exists())
                .andExpect(jsonPath("$.errors.password").exists());
    }

    @Test
    void createWithDuplicatedEmailReturns409() throws Exception {
        createUser("dup@example.com");

        mvc.perform(json(post("/v1/users"), userJson("DUP@example.com")).with(admin()))
                .andExpect(status().isConflict());
    }

    @Test
    void getUnknownUserReturns404() throws Exception {
        mvc.perform(get("/v1/users/9999").with(admin()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("User not found with id 9999"));
    }

    @Test
    void patchUpdatesOnlyProvidedFields() throws Exception {
        long id = createUser("patch@example.com");

        mvc.perform(json(patch("/v1/users/{id}", id), "{\"name\":\"María\"}").with(admin()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("María"))
                .andExpect(jsonPath("$.lastName").value("Pérez"))
                .andExpect(jsonPath("$.email").value("patch@example.com"));
    }

    @Test
    void deleteIsSoftAndHidesUser() throws Exception {
        long id = createUser("delete@example.com");

        mvc.perform(delete("/v1/users/{id}", id).with(admin())).andExpect(status().isNoContent());
        mvc.perform(get("/v1/users/{id}", id).with(admin())).andExpect(status().isNotFound());
        mvc.perform(delete("/v1/users/{id}", id).with(admin())).andExpect(status().isNotFound());

        // El email de un usuario borrado se puede volver a usar (indice unico parcial).
        createUser("delete@example.com");
    }

    @Test
    void listSupportsPaginationAndFilters() throws Exception {
        createUser("one@example.com");
        createUser("two@example.com");
        createUser("three@other.com");

        mvc.perform(get("/v1/users").param("size", "2").with(admin()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.page.totalElements").value(3));

        mvc.perform(get("/v1/users").param("email", "example.com").with(admin()))
                .andExpect(jsonPath("$.page.totalElements").value(2));
    }

    @Test
    void requestsWithoutTokenReturn401() throws Exception {
        mvc.perform(get("/v1/users")).andExpect(status().isUnauthorized());
    }

    @Test
    void regularUserCannotCreateOrDelete() throws Exception {
        long id = createUser("regular@example.com");

        mvc.perform(json(post("/v1/users"), userJson("other@example.com")).with(user(id)))
                .andExpect(status().isForbidden());
        mvc.perform(delete("/v1/users/{id}", id).with(user(id)))
                .andExpect(status().isForbidden());
    }

    @Test
    void regularUserCanUpdateOnlyThemselves() throws Exception {
        long self = createUser("self@example.com");
        long other = createUser("someone@example.com");

        mvc.perform(json(patch("/v1/users/{id}", self), "{\"cellphone\":\"3001112233\"}").with(user(self)))
                .andExpect(status().isOk());
        mvc.perform(json(patch("/v1/users/{id}", other), "{\"cellphone\":\"3001112233\"}").with(user(self)))
                .andExpect(status().isForbidden());
    }
}
