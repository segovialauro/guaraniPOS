package com.guarani.pos.cash.security;

public final class CashPermission {
    private CashPermission() {}

    public static final String CAJA_ABRIR = "CAJA_ABRIR";
    public static final String CAJA_CERRAR = "CAJA_CERRAR";
    public static final String CAJA_MOVIMIENTO_CREAR = "CAJA_MOVIMIENTO_CREAR";
    public static final String CAJA_MOVIMIENTO_EDITAR = "CAJA_MOVIMIENTO_EDITAR";
    public static final String CAJA_MOVIMIENTO_ANULAR = "CAJA_MOVIMIENTO_ANULAR";
    public static final String CAJA_REPORTE_VER = "CAJA_REPORTE_VER";
    public static final String CAJA_REPORTE_DESCARGAR = "CAJA_REPORTE_DESCARGAR";
}
