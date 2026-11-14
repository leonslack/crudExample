package com.clever.example.auth;

import com.clever.example.TestcontainersConfiguration;
import com.clever.example.repository.UserRepository;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Flujo completo con tokens reales: registro, login, uso del token y control de roles. */
@SpringBootTest(properties = {
        "app.security.admin.email=admin@test.com",
        "app.security.admin.password=admin-pass-123"
})
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class AuthControllerTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    UserRepository repository;

    @BeforeEach
    void removeNonAdminUsers() {
        repository.findAll().stream()
                .filter(u -> !u.getEmail().equals("admin@test.com"))
                .forEach(u -> repository.deleteById(u.getId()));
    }

    private String login(String email, String password) throws Exception {
        String body = mvc.perform(post("/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"%s\",\"password\":\"%s\"}".formatted(email, password)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresIn").value(3600))
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(body, "$.accessToken");
    }

    private void register(String email) throws Exception {
        mvc.perform(post("/v1/auth/register").contentType(MediaType.APPLICATION_JSON).content("""
                        {"name":"Luis","lastName":"Gómez","email":"%s","password":"user-pass-123"}
                        """.formatted(email)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.role").value("USER"))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void registerLoginAndCallProtectedEndpoint() throws Exception {
        register("luis@example.com");
        String token = login("luis@example.com", "user-pass-123");

        mvc.perform(get("/v1/auth/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("luis@example.com"));
        mvc.perform(get("/v1/users").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void wrongPasswordReturns401() throws Exception {
        register("wrong@example.com");

        mvc.perform(post("/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"wrong@example.com\",\"password\":\"incorrect\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.detail").value("Invalid email or password"));
        mvc.perform(post("/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"nobody@example.com\",\"password\":\"incorrect\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void invalidTokenReturns401() throws Exception {
        mvc.perform(get("/v1/auth/me").header("Authorization", "Bearer not.a.jwt"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void userTokenCannotDeleteButAdminTokenCan() throws Exception {
        register("target@example.com");
        String userToken = login("target@example.com", "user-pass-123");
        String adminToken = login("admin@test.com", "admin-pass-123");
        long id = repository.findByEmailIgnoreCase("target@example.com").orElseThrow().getId();

        mvc.perform(delete("/v1/users/{id}", id).header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
        mvc.perform(delete("/v1/users/{id}", id).header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());

        // Un usuario borrado ya no puede iniciar sesion.
        mvc.perform(post("/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"target@example.com\",\"password\":\"user-pass-123\"}"))
                .andExpect(status().isUnauthorized());
    }
}
