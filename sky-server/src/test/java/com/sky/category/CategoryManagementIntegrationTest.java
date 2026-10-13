package com.sky.category;

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
class CategoryManagementIntegrationTest extends DatabaseTestSupport {
    private long create(String auth,String name,int type,int sort) throws Exception {
        mvc.perform(post("/admin/category").header("Authorization",auth).contentType("application/json")
                        .content(json.writeValueAsString(java.util.Map.of("name",name,"type",type,"sort",sort))))
                .andExpect(status().isOk());
        return jdbc.queryForObject("SELECT id FROM category WHERE name=?",Long.class,name);
    }
    @Test void createDefaultsDisabledAndPersistsAudit() throws Exception {
        long id=create(login("admin"),"Meals",1,0);
        assertEquals(0,jdbc.queryForObject("SELECT status FROM category WHERE id=?",Integer.class,id));
        assertEquals(adminId,jdbc.queryForObject("SELECT create_user FROM category WHERE id=?",Long.class,id));
    }
    @Test void enabledListFiltersTypeAndUsesStableSort() throws Exception {
        String auth=login("admin");
        long first=create(auth,"First",1,2), second=create(auth,"Second",1,1);
        create(auth,"Hidden",1,0); long combo=create(auth,"Combo",2,0);
        for(long id:new long[]{first,second,combo}) mvc.perform(post("/admin/category/status/1").param("id",""+id).header("Authorization",auth)).andExpect(status().isOk());
        mvc.perform(get("/admin/category/list").param("type","1").header("Authorization",auth)).andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(2)).andExpect(jsonPath("$.data[0].id").value(second));
    }
    @Test void updatePageAndDelete() throws Exception {
        String auth=login("admin");long id=create(auth,"Original",1,0);
        mvc.perform(put("/admin/category").header("Authorization",auth).contentType("application/json")
                        .content(json.writeValueAsString(java.util.Map.of("id",id,"name","Changed","type",2,"sort",3))))
                .andExpect(status().isOk());
        mvc.perform(get("/admin/category/"+id).header("Authorization",auth)).andExpect(status().isOk()).andExpect(jsonPath("$.data.type").value(2));
        mvc.perform(get("/admin/category/page").param("name","Changed").param("type","2").header("Authorization",auth))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.total").value(1));
        mvc.perform(delete("/admin/category/"+id).header("Authorization",auth)).andExpect(status().isOk());
        mvc.perform(get("/admin/category/"+id).header("Authorization",auth)).andExpect(status().isNotFound());
    }
    @Test void duplicateInvalidTypeAndNegativeSortAreRejected() throws Exception {
        String auth=login("admin");create(auth,"Taken",1,0);
        mvc.perform(post("/admin/category").header("Authorization",auth).contentType("application/json").content("{\"name\":\"Taken\",\"type\":2,\"sort\":0}"))
                .andExpect(status().isConflict());
        mvc.perform(post("/admin/category").header("Authorization",auth).contentType("application/json").content("{\"name\":\"Other\",\"type\":3,\"sort\":0}"))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/admin/category").header("Authorization",auth).contentType("application/json").content("{\"name\":\"Other\",\"type\":1,\"sort\":-1}"))
                .andExpect(status().isBadRequest());
    }
}
