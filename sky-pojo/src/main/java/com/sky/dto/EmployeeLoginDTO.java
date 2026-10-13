package com.sky.dto;

import jakarta.validation.constraints.*;

public record EmployeeLoginDTO(@NotBlank @Size(max=32) String username, @NotBlank @Size(max=72) String password) {
    @Override public String toString() { return "EmployeeLoginDTO[username=" + username + ", password=[REDACTED]]"; }
}
