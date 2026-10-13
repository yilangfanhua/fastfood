package com.sky.auth;

import com.sky.SkyApplication;
import com.sky.support.DatabaseTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = SkyApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class EmployeeLoginTest extends DatabaseTestSupport {

    @Test
    void unknownAccountReturnsUniformUnauthorizedResponse() throws Exception {
        mvc.perform(post("/admin/employee/login").contentType("application/json")
                        .content("{\"username\":\"not-an-account\",\"password\":\"wrong-password\"}"))
                .andExpect(status().isUnauthorized()).andExpect(jsonPath("$.code").value(0));
    }

    @Test void successfulLoginNeverReturnsPassword() throws Exception {
        mvc.perform(post("/admin/employee/login").contentType("application/json")
                        .content(json.writeValueAsString(java.util.Map.of("username", "admin", "password", PASSWORD))))
                .andExpect(status().isOk()).andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data.id").value(adminId)).andExpect(jsonPath("$.data.token").isString())
                .andExpect(jsonPath("$.data.password").doesNotExist());
    }
    @Test void wrongPasswordIsRejected() throws Exception {
        mvc.perform(post("/admin/employee/login").contentType("application/json")
                        .content("{\"username\":\"admin\",\"password\":\"incorrect\"}"))
                .andExpect(status().isUnauthorized()).andExpect(jsonPath("$.code").value(0));
    }
    @Test void disabledAccountCannotLogin() throws Exception {
        seed("disabled", 0, false);
        mvc.perform(post("/admin/employee/login").contentType("application/json")
                        .content(json.writeValueAsString(java.util.Map.of("username", "disabled", "password", PASSWORD))))
                .andExpect(status().isForbidden());
    }
    @Test void unauthenticatedAdminRequestIsRejected() throws Exception {
        mvc.perform(get("/admin/employee/page")).andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(0));
    }
    @Test void invalidInputIsRejected() throws Exception {
        mvc.perform(post("/admin/employee/login").contentType("application/json").content("{}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value(0));
    }
}
