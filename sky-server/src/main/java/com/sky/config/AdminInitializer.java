package com.sky.config;

import com.sky.entity.Employee;
import com.sky.mapper.EmployeeMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import java.time.Clock;
import java.time.LocalDateTime;
import java.nio.charset.StandardCharsets;

@Component
public class AdminInitializer implements ApplicationRunner {
    private final EmployeeMapper employees;
    private final PasswordEncoder passwords;
    private final Clock clock;
    private final boolean enabled;
    private final String initialPassword;

    public AdminInitializer(EmployeeMapper employees, PasswordEncoder passwords, Clock clock,
                            @Value("${sky.bootstrap.enabled:false}") boolean enabled,
                            @Value("${sky.bootstrap.password:}") String initialPassword) {
        this.employees = employees; this.passwords = passwords; this.clock = clock;
        this.enabled = enabled; this.initialPassword = initialPassword;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!enabled || employees.findBootstrapAdmin() != null) return;
        if (initialPassword == null || initialPassword.isBlank() || initialPassword.length() < 8
                || initialPassword.getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new IllegalStateException("Provide ADMIN_INITIAL_PASSWORD (8 characters minimum, 72 UTF-8 bytes maximum)");
        }
        Employee employee = new Employee();
        employee.setUsername("admin"); employee.setName("管理员");
        employee.setPassword(passwords.encode(initialPassword));
        employee.setCreateTime(LocalDateTime.now(clock)); employee.setUpdateTime(employee.getCreateTime());
        // System initialization uses actor ID 0; normal writes must use an authenticated employee.
        employee.setCreateUser(0L); employee.setUpdateUser(0L);
        try { employees.insertBootstrap(employee); }
        catch (org.springframework.dao.DuplicateKeyException e) {
            if (employees.findBootstrapAdmin() == null) throw new IllegalStateException("Reserved admin username already exists");
        }
    }
}
