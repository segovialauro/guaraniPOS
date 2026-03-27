package com.guarani.pos.product.dto;

import java.math.BigDecimal;

public record ProductResponse(
        Long id,
        String codigo,
        String nombre,
        String descripcion,
        String categoria,
        BigDecimal precioCosto,
        BigDecimal precioVenta,
        BigDecimal precioVentaMayorista,
        BigDecimal cantidadMayoristaMinima,
        BigDecimal stockActual,
        BigDecimal stockMinimo,
        String unidadMedida,
        String vatType,
        boolean activo,
        String qrContenido,
        String codigoBarras
) {


}
