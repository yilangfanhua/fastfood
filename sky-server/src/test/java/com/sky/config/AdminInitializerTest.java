package com.sky.config;

import com.sky.SkyApplication;
import com.sky.mapper.EmployeeMapper;
import com.sky.support.DatabaseTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import java.time.Clock;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes=SkyApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AdminInitializerTest extends DatabaseTestSupport {
    @Autowired EmployeeMapper mapper;
    @Autowired PasswordEncoder passwords;
    @Autowired Clock clock;

    @Test void bootstrapCreatesHashedPasswordOnlyOnce() {
        jdbc.update("DELETE FROM employee");
        AdminInitializer first=new AdminInitializer(mapper,passwords,clock,true,PASSWORD);
        first.run(new DefaultApplicationArguments());
        var before=mapper.findBootstrapAdmin();
        assertEquals("admin",before.getUsername()); assertTrue(passwords.matches(PASSWORD,before.getPassword()));
        assertEquals(0L,before.getCreateUser());
        new AdminInitializer(mapper,passwords,clock,true,"different-password").run(new DefaultApplicationArguments());
        assertEquals(before.getPassword(),mapper.findBootstrapAdmin().getPassword());
        assertEquals(1,jdbc.queryForObject("SELECT COUNT(*) FROM employee",Integer.class));
    }
    @Test void missingInitialPasswordFailsWithoutCreatingAccount() {
        jdbc.update("DELETE FROM employee");
        assertThrows(IllegalStateException.class, () -> new AdminInitializer(mapper,passwords,clock,true,"").run(new DefaultApplicationArguments()));
        assertNull(mapper.findBootstrapAdmin());
    }
    @Test void disabledBootstrapDoesNotCreateAccount() {
        jdbc.update("DELETE FROM employee");
        new AdminInitializer(mapper,passwords,clock,false,"").run(new DefaultApplicationArguments());
        assertNull(mapper.findBootstrapAdmin());
    }
    @Test void blankInitialPasswordCannotCreateAnUnusableAdmin() {
        jdbc.update("DELETE FROM employee");
        assertThrows(IllegalStateException.class, () -> new AdminInitializer(mapper,passwords,clock,true,"        ").run(new DefaultApplicationArguments()));
        assertNull(mapper.findBootstrapAdmin());
    }
}
