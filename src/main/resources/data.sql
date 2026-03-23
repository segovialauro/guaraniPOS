INSERT INTO empresa (id, codigo, nombre, estado, licencia_estado, licencia_vencimiento, ruc)
VALUES (1, 'demo', 'Empresa Demo Guarani POS', 'ACTIVA', 'ACTIVA', '2030-12-31', '80012345-6');

-- contrasena bcrypt de Admin123*
INSERT INTO usuario (id, empresa_id, cedula, nombre_completo, password_hash, quick_pin, rol_codigo, estado)
VALUES (
    1,
    1,
    '1234567',
    'Administrador Demo',
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

INSERT INTO producto
(id, empresa_id, codigo, nombre, descripcion, categoria, precio_costo, precio_venta, stock_actual, stock_minimo, unidad_medida, vat_type, activo)
VALUES
(1, 1, 'ZAP-001', 'Zapato deportivo negro', 'Calzado deportivo urbano', 'Calzados', 120000, 185000, 8, 3, 'PAR', 'IVA_10', TRUE),
(2, 1, 'OJT-001', 'Ojota clasica azul', 'Ojota liviana de verano', 'Calzados', 25000, 45000, 20, 5, 'PAR', 'IVA_10', TRUE),
(3, 1, 'MED-001', 'Media deportiva blanca', 'Media de algodon', 'Accesorios', 8000, 15000, 50, 10, 'PAR', 'IVA_5', TRUE);

INSERT INTO cliente
(id, empresa_id, nombre, documento, document_type, ruc, telefono, email, direccion, gender, segment, tax_profile, observacion, activo)
VALUES
(1, 1, 'Juan Perez', '1234567', 'CI', NULL, '0981123456', 'juanperez@gmail.com', 'San Lorenzo', 'MASCULINO', 'MINORISTA', 'CONSUMIDOR_FINAL', 'Cliente frecuente', TRUE),
(2, 1, 'Maria Gomez', '2345678', 'CI', NULL, '0991456789', 'mariagomez@gmail.com', 'Fernando de la Mora', 'FEMENINO', 'MINORISTA', 'CONSUMIDOR_FINAL', '', TRUE),
(3, 1, 'Comercial Central SRL', NULL, 'RUC', '80012345-6', '021555444', 'ventas@comercialcentral.com', 'Asuncion', NULL, 'MAYORISTA', 'CONTRIBUYENTE', 'Compra con factura', TRUE);

INSERT INTO stock (id, empresa_id, producto_nombre, current_stock, min_stock)
VALUES
(1, 1, 'Zapato deportivo negro', 2, 5),
(2, 1, 'Ojota clasica azul', 20, 5);


INSERT INTO venta (
    id,
    empresa_id,
    cliente_id,
    numero_operacion,
    fecha,
    subtotal,
    descuento_total,
    total,
    metodo_pago,
    monto_recibido,
    vuelto,
    estado,
    observacion,
    creado_por_usuario_id
)
VALUES
(
    1,
    1,
    1,
    'V-202603-000001',
    current_timestamp,
    150000,
    0,
    150000,
    'EFECTIVO',
    150000,
    0,
    'CONFIRMADA',
    'Venta demo 1',
    1
),
(
    2,
    1,
    2,
    'V-202603-000002',
    current_timestamp,
    85000,
    0,
    85000,
    'TRANSFERENCIA',
    NULL,
    0,
    'CONFIRMADA',
    'Venta demo 2',
    1
);

INSERT INTO venta_detalle (
    id,
    venta_id,
    producto_id,
    producto_codigo,
    producto_nombre,
    cantidad,
    precio_unitario,
    gross_subtotal,
    discount_amount,
    subtotal,
    vat_type
)
VALUES
(1, 1, 1, 'ZAP-001', 'Zapato deportivo negro', 1, 150000, 150000, 0, 150000, 'IVA_10'),
(2, 2, 2, 'OJT-001', 'Ojota clasica azul', 1, 85000, 85000, 0, 85000, 'IVA_10');

SELECT setval(pg_get_serial_sequence('empresa', 'id'), COALESCE((SELECT MAX(id) FROM empresa), 1), true);
SELECT setval(pg_get_serial_sequence('usuario', 'id'), COALESCE((SELECT MAX(id) FROM usuario), 1), true);
SELECT setval(pg_get_serial_sequence('parametro_general', 'id'), COALESCE((SELECT MAX(id) FROM parametro_general), 1), true);
SELECT setval(pg_get_serial_sequence('producto', 'id'), COALESCE((SELECT MAX(id) FROM producto), 1), true);
SELECT setval(pg_get_serial_sequence('cliente', 'id'), COALESCE((SELECT MAX(id) FROM cliente), 1), true);
SELECT setval(pg_get_serial_sequence('stock', 'id'), COALESCE((SELECT MAX(id) FROM stock), 1), true);
SELECT setval(pg_get_serial_sequence('presupuesto', 'id'), COALESCE((SELECT MAX(id) FROM presupuesto), 1), true);
SELECT setval(pg_get_serial_sequence('pagare', 'id'), COALESCE((SELECT MAX(id) FROM pagare), 1), true);
SELECT setval(pg_get_serial_sequence('venta', 'id'), COALESCE((SELECT MAX(id) FROM venta), 1), true);
SELECT setval(pg_get_serial_sequence('venta_detalle', 'id'), COALESCE((SELECT MAX(id) FROM venta_detalle), 1), true);

update producto
set qr_contenido = 'http://localhost:4200/productos/' || id || '/qr-view'
where qr_contenido is null or qr_contenido = '';

insert into presupuesto (
    id,
    empresa_id,
    cliente_id,
    numero_presupuesto,
    fecha,
    vigencia_hasta,
    total,
    estado,
    observacion,
    creado_por_usuario_id
) values
(
    1,
    1,
    1,
    'P-202603-000001',
    current_timestamp,
    current_date + 7,
    230000,
    'PENDIENTE',
    'Presupuesto demo',
    1
);

insert into presupuesto_detalle (
    id,
    presupuesto_id,
    producto_id,
    producto_codigo,
    producto_nombre,
    cantidad,
    precio_unitario,
    subtotal
) values
(1, 1, 1, 'ZAP-001', 'Zapato deportivo negro', 1, 185000, 185000),
(2, 1, 3, 'MED-001', 'Media deportiva blanca', 3, 15000, 45000);

SELECT setval(pg_get_serial_sequence('presupuesto', 'id'), COALESCE((SELECT MAX(id) FROM presupuesto), 1), true);
SELECT setval(pg_get_serial_sequence('presupuesto_detalle', 'id'), COALESCE((SELECT MAX(id) FROM presupuesto_detalle), 1), true);

insert into pagare (
    id,
    empresa_id,
    cliente_id,
    venta_id,
    numero_pagare,
    fecha_emision,
    fecha_vencimiento,
    monto,
    saldo,
    estado,
    observacion,
    creado_por_usuario_id
) values
(
    1,
    1,
    1,
    1,
    'PG-202603-000001',
    current_date,
    current_date + 30,
    300000,
    300000,
    'PENDIENTE',
    'Pagare demo',
    1
);

SELECT setval(pg_get_serial_sequence('pagare', 'id'), COALESCE((SELECT MAX(id) FROM pagare), 1), true);
SELECT setval(pg_get_serial_sequence('pagare_pago', 'id'), COALESCE((SELECT MAX(id) FROM pagare_pago), 1), true);




insert into caja_turno (
    id, empresa_id, usuario_id, fecha_apertura, monto_apertura, efectivo_sistema, transferencia_sistema,
    tarjeta_debito_sistema, tarjeta_credito_sistema, qr_sistema, total_sistema, estado, observacion
) values (
    1, 1, 1, current_timestamp, 200000, 150000, 85000, 0, 0, 0, 235000, 'ABIERTA', 'Caja demo abierta'
);

insert into venta_pago (id, venta_id, caja_turno_id, metodo_pago, monto, referencia) values
(1, 1, 1, 'EFECTIVO', 150000, null),
(2, 2, 1, 'TRANSFERENCIA', 85000, 'TRX-DEMO-002');

SELECT setval(pg_get_serial_sequence('caja_turno', 'id'), COALESCE((SELECT MAX(id) FROM caja_turno), 1), true);
SELECT setval(pg_get_serial_sequence('venta_pago', 'id'), COALESCE((SELECT MAX(id) FROM venta_pago), 1), true);

INSERT INTO suscripcion_plan (
    code, name, description, price_monthly,
    max_open_cash_sessions, max_users, max_branches, max_monthly_purchases,
    allow_fiscal_printer, allow_electronic_invoice,
    allow_internal_ticket, allow_bancard_qr, active
) VALUES
(
    'BASIC',
    'Básico',
    'Ticket interno sin valor fiscal. Ideal para negocio pequeño.',
    0,
    1, 2, 1, 30,
    FALSE, FALSE,
    TRUE, FALSE, TRUE
),
(
    'PRO',
    'Pro',
    'Incluye ticket interno y soporte para autoimpresor fiscal/legal.',
    0,
    2, 5, 2, 300,
    TRUE, FALSE,
    TRUE, TRUE, TRUE
),
(
    'PREMIUM',
    'Premium',
    'Incluye ticket interno, autoimpresor y facturación electrónica.',
    0,
    5, 20, 5, NULL,
    TRUE, TRUE,
    TRUE, TRUE, TRUE
);

INSERT INTO suscripcion_empresa (
    company_id, plan_id, status, start_date, notes
)
SELECT
    1,
    sp.id,
    'ACTIVE',
    CURRENT_DATE,
    'Plan inicial'
FROM suscripcion_plan sp
WHERE sp.code = 'PRO';


INSERT INTO usuario (
    empresa_id,
    cedula,
    nombre_completo,
    password_hash,
    quick_pin,
    rol_codigo,
    estado
)
SELECT
    empresa_id,
    '9999999',
    'Cajero Demo',
    password_hash,
    '5678',
    'CAJERO',
    'ACTIVO'
FROM usuario
WHERE id = 1;
select id, codigo, estado, licencia_estado, licencia_vencimiento
from empresa;


