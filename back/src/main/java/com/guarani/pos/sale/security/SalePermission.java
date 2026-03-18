package com.guarani.pos.sale.security;

public final class SalePermission {
    private SalePermission() {}

    public static final String VENTA_CREAR = "VENTA_CREAR";
    public static final String VENTA_TICKET_VER = "VENTA_TICKET_VER";
    public static final String VENTA_TICKET_DESCARGAR = "VENTA_TICKET_DESCARGAR";
}