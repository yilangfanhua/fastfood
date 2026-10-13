package com.sky.dto;

import jakarta.validation.constraints.*;

public record CategoryCreateDTO(@NotNull @Min(1) @Max(2) Integer type, @NotBlank @Size(max=64) String name, @NotNull @Min(0) Integer sort) {}
