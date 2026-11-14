package com.clever.example.controller;

import com.clever.example.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;
import java.time.Period;

import static org.hamcrest.Matchers.endsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
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
                {"name":"Ana","lastName":"Pérez","email":"%s","cellphone":"+57 300 123 4567","birthdate":"1990-05-20"}
                """.formatted(email);
    }

    private long createUser(String email) throws Exception {
        MvcResult result = mvc.perform(post("/v1/users").contentType(MediaType.APPLICATION_JSON).content(userJson(email)))
                .andExpect(status().isCreated())
                .andReturn();
        String location = result.getResponse().getHeader("Location");
        return Long.parseLong(location.substring(location.lastIndexOf('/') + 1));
    }

    @Test
    void createReturns201WithLocationAndComputedAge() throws Exception {
        int expectedAge = Period.between(LocalDate.of(1990, 5, 20), LocalDate.now()).getYears();

        mvc.perform(post("/v1/users").contentType(MediaType.APPLICATION_JSON).content(userJson("ana@example.com")))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", endsWith("/v1/users/" + repository.findAll().getFirst().getId())))
                .andExpect(jsonPath("$.email").value("ana@example.com"))
                .andExpect(jsonPath("$.age").value(expectedAge))
                .andExpect(jsonPath("$.createdAt").exists());
    }

    @Test
    void createWithInvalidDataReturns400WithFieldErrors() throws Exception {
        mvc.perform(post("/v1/users").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"\",\"email\":\"not-an-email\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.name").exists())
                .andExpect(jsonPath("$.errors.lastName").exists())
                .andExpect(jsonPath("$.errors.email").exists());
    }

    @Test
    void createWithDuplicatedEmailReturns409() throws Exception {
        createUser("dup@example.com");

        mvc.perform(post("/v1/users").contentType(MediaType.APPLICATION_JSON).content(userJson("DUP@example.com")))
                .andExpect(status().isConflict());
    }

    @Test
    void getUnknownUserReturns404() throws Exception {
        mvc.perform(get("/v1/users/9999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("User not found with id 9999"));
    }

    @Test
    void patchUpdatesOnlyProvidedFields() throws Exception {
        long id = createUser("patch@example.com");

        mvc.perform(patch("/v1/users/{id}", id).contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"María\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("María"))
                .andExpect(jsonPath("$.lastName").value("Pérez"))
                .andExpect(jsonPath("$.email").value("patch@example.com"));
    }

    @Test
    void deleteIsSoftAndHidesUser() throws Exception {
        long id = createUser("delete@example.com");

        mvc.perform(delete("/v1/users/{id}", id)).andExpect(status().isNoContent());
        mvc.perform(get("/v1/users/{id}", id)).andExpect(status().isNotFound());
        mvc.perform(delete("/v1/users/{id}", id)).andExpect(status().isNotFound());
    }

    @Test
    void listSupportsPaginationAndFilters() throws Exception {
        createUser("one@example.com");
        createUser("two@example.com");
        createUser("three@other.com");

        mvc.perform(get("/v1/users").param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.page.totalElements").value(3));

        mvc.perform(get("/v1/users").param("email", "example.com"))
                .andExpect(jsonPath("$.page.totalElements").value(2));
    }
}
