package com.sky.dto;

import jakarta.validation.constraints.*;

public record EmployeeCreateDTO(@NotBlank @Pattern(regexp="[A-Za-z0-9_]{3,32}") String username, @NotBlank @Size(max=64) String name, @NotBlank @Size(min=8,max=72) String password, @NotBlank @Pattern(regexp="1[3-9][0-9]{9}") String phone, @Pattern(regexp="[01]") String sex, @Pattern(regexp="[0-9]{17}[0-9Xx]") String idNumber) {
    @Override public String toString() { return "EmployeeCreateDTO[username=" + username + ", password=[REDACTED]]"; }
}
