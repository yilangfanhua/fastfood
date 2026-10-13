package com.sky.dto;

import jakarta.validation.constraints.*;

public record CategoryUpdateDTO(@NotNull @Positive Long id, @NotNull @Min(1) @Max(2) Integer type, @NotBlank @Size(max=64) String name, @NotNull @Min(0) Integer sort) {}
