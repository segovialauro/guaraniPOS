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
        BigDecimal stockActual,
        BigDecimal stockMinimo,
        String unidadMedida,
        boolean activo,
        String qrContenido,
        String codigoBarras
) {


}