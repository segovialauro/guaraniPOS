INSERT INTO empresa (id, codigo, nombre, estado, licencia_estado, licencia_vencimiento, ruc)
VALUES (1, 'principal', 'Mi Empresa', 'ACTIVA', 'ACTIVA', CURRENT_DATE + 365, NULL);

-- contrasena bcrypt de Admin123*
INSERT INTO usuario (id, empresa_id, cedula, nombre_completo, password_hash, quick_pin, rol_codigo, estado)
VALUES (
    1,
    1,
    '1000001',
    'Administrador Principal',
    '$2a$10$n5EQTro.2d/o9ghuAAJCAe09hGhCe7ZQPgFBt1.Isjq3DrWAYYK8C',
    '1234',
    'ADMIN_EMPRESA',
    'ACTIVO'
);

INSERT INTO parametro_general (group_code, code, label, description, sort_order, active, system_defined)
VALUES
('PRODUCT_CATEGORY', 'Calzados', 'Calzados', 'Categoria para calzados y zapatillas.', 10, TRUE, TRUE),
('PRODUCT_CATEGORY', 'Accesorios', 'Accesorios', 'Categoria para medias, cinturones y complementos.', 20, TRUE, TRUE),
('PRODUCT_CATEGORY', 'Liquidos', 'Liquidos', 'Categoria para bebidas y combustibles.', 30, TRUE, TRUE),
('UNIT_MEASURE', 'UNIDAD', 'Unidad', 'Unidad individual.', 10, TRUE, TRUE),
('UNIT_MEASURE', 'PAR', 'Par', 'Unidad de venta en pares.', 20, TRUE, TRUE),
('UNIT_MEASURE', 'CAJA', 'Caja', 'Unidad de venta en cajas.', 30, TRUE, TRUE),
('UNIT_MEASURE', 'LITRO', 'Litro', 'Unidad de volumen.', 40, TRUE, TRUE),
('CUSTOMER_DOCUMENT_TYPE', 'CI', 'Cedula', 'Documento de identidad.', 10, TRUE, TRUE),
('CUSTOMER_DOCUMENT_TYPE', 'RUC', 'RUC', 'Registro Unico del Contribuyente.', 20, TRUE, TRUE),
('CUSTOMER_DOCUMENT_TYPE', 'PASAPORTE', 'Pasaporte', 'Documento para clientes extranjeros.', 30, TRUE, TRUE),
('CUSTOMER_GENDER', 'FEMENINO', 'Femenino', 'Genero femenino.', 10, TRUE, TRUE),
('CUSTOMER_GENDER', 'MASCULINO', 'Masculino', 'Genero masculino.', 20, TRUE, TRUE),
('CUSTOMER_GENDER', 'OTRO', 'Otro', 'Genero no especificado.', 30, TRUE, TRUE),
('CUSTOMER_SEGMENT', 'MINORISTA', 'Minorista', 'Cliente de venta al detalle.', 10, TRUE, TRUE),
('CUSTOMER_SEGMENT', 'MAYORISTA', 'Mayorista', 'Cliente de venta por volumen.', 20, TRUE, TRUE),
('CUSTOMER_SEGMENT', 'VIP', 'VIP', 'Cliente de atencion preferencial.', 30, TRUE, TRUE),
('CUSTOMER_TAX_PROFILE', 'CONTRIBUYENTE', 'Contribuyente', 'Cliente con tratamiento fiscal normal.', 10, TRUE, TRUE),
('CUSTOMER_TAX_PROFILE', 'CONSUMIDOR_FINAL', 'Consumidor final', 'Cliente consumidor final.', 20, TRUE, TRUE),
('CUSTOMER_TAX_PROFILE', 'EXTERIOR', 'Exterior', 'Cliente del exterior.', 30, TRUE, TRUE),
('INVENTORY_ADJUSTMENT_REASON', 'CONTEO_FISICO', 'Conteo fisico', 'Ajuste por conteo fisico de inventario.', 10, TRUE, TRUE),
('INVENTORY_ADJUSTMENT_REASON', 'MERCADERIA_ENCONTRADA', 'Mercaderia encontrada', 'Ingreso por mercaderia localizada posteriormente.', 20, TRUE, TRUE),
('INVENTORY_ADJUSTMENT_REASON', 'MERMA', 'Merma o danio', 'Salida por merma, vencimiento o danio.', 30, TRUE, TRUE),
('INVENTORY_ADJUSTMENT_REASON', 'CORRECCION_SISTEMA', 'Correccion de sistema', 'Correccion administrativa de stock.', 40, TRUE, TRUE),
('POS_DISCOUNT_POLICY', 'CAJERO_MAX_PERCENT', '10', 'Porcentaje maximo de descuento permitido para cajero.', 10, TRUE, TRUE),
('POS_DISCOUNT_POLICY', 'SUPERVISOR_MAX_PERCENT', '25', 'Porcentaje maximo de descuento permitido para supervisor.', 20, TRUE, TRUE),
('POS_DISCOUNT_POLICY', 'ADMIN_MAX_PERCENT', '100', 'Porcentaje maximo de descuento permitido para administrador.', 30, TRUE, TRUE);

INSERT INTO suscripcion_plan (
    id, code, name, description, price_monthly,
    max_open_cash_sessions, max_users, max_branches, max_monthly_purchases,
    allow_fiscal_printer, allow_electronic_invoice,
    allow_internal_ticket, allow_bancard_qr, active
) VALUES
(
    1,
    'BASIC',
    'Basico',
    'Sistema completo para una caja y una sucursal.',
    0,
    1, 2, 1, NULL,
    TRUE, FALSE,
    TRUE, TRUE, TRUE
),
(
    2,
    'PRO',
    'Pro',
    'Sistema completo con mayor capacidad operativa para crecer.',
    0,
    2, 5, 2, NULL,
    TRUE, FALSE,
    TRUE, TRUE, TRUE
),
(
    3,
    'PREMIUM',
    'Premium',
    'Sistema completo con mayor capacidad y factura electronica.',
    0,
    5, 20, 5, NULL,
    TRUE, TRUE,
    TRUE, TRUE, TRUE
);

INSERT INTO suscripcion_empresa (
    id, company_id, plan_id, status, start_date, end_date, trial_ends_at, notes
) VALUES (
    1, 1, 2, 'ACTIVE', CURRENT_DATE, NULL, NULL, 'Suscripcion inicial de instalacion'
);

SELECT setval(pg_get_serial_sequence('empresa', 'id'), COALESCE((SELECT MAX(id) FROM empresa), 1), true);
SELECT setval(pg_get_serial_sequence('usuario', 'id'), COALESCE((SELECT MAX(id) FROM usuario), 1), true);
SELECT setval(pg_get_serial_sequence('parametro_general', 'id'), COALESCE((SELECT MAX(id) FROM parametro_general), 1), true);
SELECT setval(pg_get_serial_sequence('suscripcion_plan', 'id'), COALESCE((SELECT MAX(id) FROM suscripcion_plan), 1), true);
SELECT setval(pg_get_serial_sequence('suscripcion_empresa', 'id'), COALESCE((SELECT MAX(id) FROM suscripcion_empresa), 1), true);
