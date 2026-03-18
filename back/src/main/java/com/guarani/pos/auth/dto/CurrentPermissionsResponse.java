package com.guarani.pos.auth.dto;

import java.util.Set;

public record CurrentPermissionsResponse(Set<String> permissions) {
}
