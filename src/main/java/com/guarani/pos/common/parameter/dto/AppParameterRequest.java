package com.guarani.pos.common.parameter.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AppParameterRequest(
        @NotBlank
        @Size(max = 50)
        String groupCode,

        @NotBlank
        @Size(max = 50)
        String code,

        @NotBlank
        @Size(max = 150)
        String label,

        @Size(max = 300)
        String description,

        Integer sortOrder,

        boolean active
) {
}
