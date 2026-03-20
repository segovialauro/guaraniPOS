package com.guarani.pos.common.parameter.dto;

public record AppParameterResponse(
        Long id,
        String groupCode,
        String code,
        String label,
        String description,
        int sortOrder,
        boolean active,
        boolean systemDefined
) {
}
