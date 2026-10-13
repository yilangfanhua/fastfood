package com.sky.employee;

import com.sky.SkyApplication;
import com.sky.support.DatabaseTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes=SkyApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class EmployeeManagementIntegrationTest extends DatabaseTestSupport {
    private String createBody(String username) throws Exception {
        return json.writeValueAsString(java.util.Map.of("username",username,"name","Employee", "password",PASSWORD,"phone","13800000001"));
    }
    @Test void createReadUpdateDeleteAndAudit() throws Exception {
        String auth = login("admin");
        mvc.perform(post("/admin/employee").header("Authorization",auth).contentType("application/json").content(createBody("new_user")))
                .andExpect(status().isOk()).andExpect(jsonPath("$.code").value(1));
        long id=jdbc.queryForObject("SELECT id FROM employee WHERE username='new_user'",Long.class);
        assertNotEquals(PASSWORD,jdbc.queryForObject("SELECT password FROM employee WHERE id=?",String.class,id));
        assertEquals(adminId,jdbc.queryForObject("SELECT create_user FROM employee WHERE id=?",Long.class,id));
        mvc.perform(get("/admin/employee/"+id).header("Authorization",auth)).andExpect(status().isOk())
                .andExpect(jsonPath("$.data.password").doesNotExist()).andExpect(jsonPath("$.data.status").value(1));
        mvc.perform(put("/admin/employee").header("Authorization",auth).contentType("application/json")
                        .content(json.writeValueAsString(java.util.Map.of("id",id,"username","new_user","name","Changed","phone","13800000002","createUser",999))))
                .andExpect(status().isOk());
        assertEquals(adminId,jdbc.queryForObject("SELECT create_user FROM employee WHERE id=?",Long.class,id));
        mvc.perform(delete("/admin/employee/"+id).header("Authorization",auth)).andExpect(status().isOk());
        mvc.perform(get("/admin/employee/"+id).header("Authorization",auth)).andExpect(status().isNotFound());
    }
    @Test void duplicateUsernameReturnsConflict() throws Exception {
        seed("taken",1,false);
        mvc.perform(post("/admin/employee").header("Authorization",login("admin")).contentType("application/json").content(createBody("taken")))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.code").value(0));
    }
    @Test void pageCountsAndFiltersWithoutPassword() throws Exception {
        seed("employee_one",1,false); seed("employee_two",1,false);
        mvc.perform(get("/admin/employee/page").param("name","employee").param("pageSize","1").header("Authorization",login("admin")))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.total").value(2))
                .andExpect(jsonPath("$.data.records.length()").value(1)).andExpect(jsonPath("$.data.records[0].password").doesNotExist());
    }
    @Test void invalidPhoneAndPaginationAreRejected() throws Exception {
        String auth=login("admin");
        mvc.perform(post("/admin/employee").header("Authorization",auth).contentType("application/json").content(createBody("valid_user").replace("13800000001","123")))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/admin/employee/page").param("pageSize","101").header("Authorization",auth)).andExpect(status().isBadRequest());
        mvc.perform(get("/admin/employee/page").param("page","0").header("Authorization",auth)).andExpect(status().isBadRequest());
    }
    @Test void bootstrapAdminAndSelfCannotBeRemovedOrDisabled() throws Exception {
        long id=seed("ordinary",1,false); String ordinary=login("ordinary");
        mvc.perform(delete("/admin/employee/"+adminId).header("Authorization",ordinary)).andExpect(status().isForbidden());
        mvc.perform(delete("/admin/employee/"+id).header("Authorization",ordinary)).andExpect(status().isForbidden());
        mvc.perform(post("/admin/employee/status/0").param("id",""+adminId).header("Authorization",ordinary)).andExpect(status().isForbidden());
        mvc.perform(post("/admin/employee/status/0").param("id",""+id).header("Authorization",ordinary)).andExpect(status().isForbidden());
    }
    @Test void disabledAndDeletedEmployeesLoseAccessImmediately() throws Exception {
        long id=seed("ordinary",1,false); String token=login("ordinary"), admin=login("admin");
        mvc.perform(post("/admin/employee/status/0").param("id",""+id).header("Authorization",admin)).andExpect(status().isOk());
        mvc.perform(get("/admin/employee/page").header("Authorization",token)).andExpect(status().isForbidden());
        mvc.perform(post("/admin/employee/status/1").param("id",""+id).header("Authorization",admin)).andExpect(status().isOk());
        mvc.perform(delete("/admin/employee/"+id).header("Authorization",admin)).andExpect(status().isOk());
        mvc.perform(get("/admin/employee/page").header("Authorization",token)).andExpect(status().isUnauthorized());
    }
    @Test void invalidStatusAndMissingEmployeeAreRejected() throws Exception {
        String auth=login("admin");
        mvc.perform(post("/admin/employee/status/2").param("id",""+adminId).header("Authorization",auth)).andExpect(status().isBadRequest());
        mvc.perform(delete("/admin/employee/999999999").header("Authorization",auth)).andExpect(status().isNotFound());
    }
}
