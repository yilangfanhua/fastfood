package com.sky.support;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public abstract class DatabaseTestSupport {
    @Autowired protected MockMvc mvc;
    @Autowired protected JdbcTemplate jdbc;
    @Autowired protected ObjectMapper json;
    protected long adminId;
    protected static final String PASSWORD = "Test-password-123!";

    @BeforeEach
    void resetDedicatedDatabase() {
        // Refuse to clean a schema not explicitly configured as the test database.
        String catalog = jdbc.execute((java.sql.Connection connection) -> connection.getCatalog());
        if (catalog == null || !catalog.endsWith("_test")) throw new IllegalStateException("Use a dedicated *_test database");
        new ResourceDatabasePopulator(new ClassPathResource("sql/sky_stage1.sql"),new ClassPathResource("sql/reset.sql"))
                .execute(jdbc.getDataSource());
        adminId = seed("admin", 1, true);
    }

    protected long seed(String username, int status, boolean bootstrap) {
        jdbc.update("INSERT INTO employee(username,name,password,phone,status,bootstrap_admin,create_time,update_time,create_user,update_user) VALUES(?,?,?,?,?,?,NOW(6),NOW(6),0,0)",
                username, username, new BCryptPasswordEncoder().encode(PASSWORD), "13800000000", status, bootstrap);
        return jdbc.queryForObject("SELECT id FROM employee WHERE username=?", Long.class, username);
    }

    protected String login(String username) throws Exception {
        String response = mvc.perform(post("/admin/employee/login").contentType("application/json")
                .content(json.writeValueAsString(java.util.Map.of("username", username, "password", PASSWORD))))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        return "Bearer " + json.readTree(response).path("data").path("token").asText();
    }
}
