package cl.duoc.gaselvolcan;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import static org.hamcrest.Matchers.greaterThan;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ApiIntegrationTest {
    @Autowired MockMvc http;

    @Test void publicCatalogIsAvailable() throws Exception {
        http.perform(get("/api/v1/products")).andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").exists());
    }

    @Test void anonymousCannotModifyProducts() throws Exception {
        http.perform(post("/api/v1/products").contentType(MediaType.APPLICATION_JSON)
            .content("{}" )).andExpect(status().isUnauthorized());
    }

    @Test void clientRegistrationDoesNotGrantAdminPermissions() throws Exception {
        String body = "{\"name\":\"Cliente de prueba\",\"email\":\"cliente-unico@test.cl\",\"password\":\"PruebaSegura123!\"}";
        String jwt = new com.fasterxml.jackson.databind.ObjectMapper()
            .readTree(http.perform(post("/api/v1/auth/register").contentType(MediaType.APPLICATION_JSON)
                .content(body)).andExpect(status().isCreated()).andReturn().getResponse().getContentAsString())
            .get("accessToken").asText();
        http.perform(post("/api/v1/products").header("Authorization", "Bearer " + jwt)
            .contentType(MediaType.APPLICATION_JSON).content("{}"))
            .andExpect(status().isForbidden());
    }

    @Test void publicCategoriesAreAvailable() throws Exception {
        http.perform(get("/api/v1/categories"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()", greaterThan(0)));
    }

    @Test void catalogGetAllowsConfiguredCorsOrigin() throws Exception {
        http.perform(get("/api/v1/products")
                .header("Origin", "http://localhost:5173"))
            .andExpect(status().isOk())
            .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5173"))
            .andExpect(header().string("Access-Control-Allow-Credentials", "true"));
    }

    @Test void catalogPreflightAllowsAuthorizationHeader() throws Exception {
        http.perform(options("/api/v1/products")
                .header("Origin", "http://localhost:5173")
                .header("Access-Control-Request-Method", "GET")
                .header("Access-Control-Request-Headers", "authorization,content-type"))
            .andExpect(status().isOk())
            .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5173"))
            .andExpect(header().string("Access-Control-Allow-Credentials", "true"));
    }

}
