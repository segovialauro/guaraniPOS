package com.guarani.pos.product.dto;

public record ProductImportResponse(
        int importedCount,
        int skippedCount,
        String message
) {
}
