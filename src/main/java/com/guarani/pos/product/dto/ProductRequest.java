package com.guarani.pos.product.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record ProductRequest(
        @NotBlank @Size(max = 50)
        String codigo,

        @NotBlank @Size(max = 150)
        String nombre,

        @Size(max = 500)
        String descripcion,

        @Size(max = 100)
        String categoria,

        @NotNull @DecimalMin(value = "0.0", inclusive = true)
        BigDecimal precioCosto,

        @NotNull @DecimalMin(value = "0.0", inclusive = false)
        BigDecimal precioVenta,

        @NotNull @DecimalMin(value = "0.0", inclusive = true)
        BigDecimal stockActual,

        @NotNull @DecimalMin(value = "0.0", inclusive = true)
        BigDecimal stockMinimo,

        @NotBlank @Size(max = 30)
        String unidadMedida,

        @NotBlank @Size(max = 10)
        String vatType,
        
        @Size(max = 100)
        String codigoBarras,

        boolean activo
) {
}
