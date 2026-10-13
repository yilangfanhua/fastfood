package com.sky.dto;

import jakarta.validation.constraints.*;

public record EmployeeUpdateDTO(@NotNull @Positive Long id, @NotBlank @Pattern(regexp="[A-Za-z0-9_]{3,32}") String username, @NotBlank @Size(max=64) String name, @NotBlank @Pattern(regexp="1[3-9][0-9]{9}") String phone, @Pattern(regexp="[01]") String sex, @Pattern(regexp="[0-9]{17}[0-9Xx]") String idNumber) {}
