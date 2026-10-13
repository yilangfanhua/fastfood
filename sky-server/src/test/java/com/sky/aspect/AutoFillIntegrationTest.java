package com.sky.aspect;

import com.sky.SkyApplication;
import com.sky.context.BaseContext;
import com.sky.entity.Employee;
import com.sky.exception.BusinessException;
import com.sky.mapper.EmployeeMapper;
import com.sky.support.DatabaseTestSupport;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes=SkyApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AutoFillIntegrationTest extends DatabaseTestSupport {
    @Autowired EmployeeMapper mapper;
    @AfterEach void clear() { BaseContext.clear(); }

    private Employee employee() {
        Employee e = new Employee();
        e.setUsername("audit_user"); e.setName("Audit user"); e.setPassword("stored-hash"); e.setStatus(1);
        return e;
    }

    @Test void realMapperProxyFillsAndPersistsInsertAuditFields() {
        BaseContext.setCurrentId(adminId);
        Employee e = employee(); e.setCreateUser(999L); e.setUpdateUser(999L);
        e.setCreateTime(LocalDateTime.of(2000,1,1,0,0));
        mapper.insert(e);
        Employee stored = mapper.findById(e.getId());
        assertEquals(adminId, stored.getCreateUser()); assertEquals(adminId, stored.getUpdateUser());
        assertEquals(stored.getCreateTime(), stored.getUpdateTime());
        assertTrue(stored.getCreateTime().isAfter(LocalDateTime.of(2026,1,1,0,0)));
    }
    @Test void updatePreservesOriginalCreator() {
        BaseContext.setCurrentId(adminId);
        Employee e = employee(); mapper.insert(e);
        Employee before = mapper.findById(e.getId());
        long otherId = seed("other_auditor",1,false); BaseContext.setCurrentId(otherId);
        e.setName("Changed"); e.setCreateUser(999L); mapper.update(e);
        Employee after = mapper.findById(e.getId());
        assertEquals(before.getCreateUser(),after.getCreateUser());
        assertEquals(before.getCreateTime(),after.getCreateTime());
        assertEquals(otherId,after.getUpdateUser());
    }
    @Test void writeWithoutCurrentEmployeeFailsClosed() {
        assertThrows(BusinessException.class, () -> mapper.insert(employee()));
    }
}
