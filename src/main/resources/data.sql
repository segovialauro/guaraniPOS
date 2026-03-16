INSERT INTO empresa (id, codigo, nombre, estado, licencia_estado, licencia_vencimiento)
VALUES (1, 'demo', 'Empresa Demo Guaraní POS', 'ACTIVA', 'ACTIVA', '2030-12-31');

-- contraseña bcrypt de Admin123*
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

INSERT INTO producto
(id, empresa_id, codigo, nombre, descripcion, categoria, precio_costo, precio_venta, stock_actual, stock_minimo, unidad_medida, activo)
VALUES
(1, 1, 'ZAP-001', 'Zapato deportivo negro', 'Calzado deportivo urbano', 'Calzados', 120000, 185000, 8, 3, 'PAR', TRUE),
(2, 1, 'OJT-001', 'Ojota clásica azul', 'Ojota liviana de verano', 'Calzados', 25000, 45000, 20, 5, 'PAR', TRUE),
(3, 1, 'MED-001', 'Media deportiva blanca', 'Media de algodón', 'Accesorios', 8000, 15000, 50, 10, 'PAR', TRUE);

INSERT INTO cliente
(id, empresa_id, nombre, documento, ruc, telefono, email, direccion, observacion, activo)
VALUES
(1, 1, 'Juan Pérez', '1234567', NULL, '0981123456', 'juanperez@gmail.com', 'San Lorenzo', 'Cliente frecuente', TRUE),
(2, 1, 'María Gómez', '2345678', NULL, '0991456789', 'mariagomez@gmail.com', 'Fernando de la Mora', '', TRUE),
(3, 1, 'Comercial Central SRL', NULL, '80012345-6', '021555444', 'ventas@comercialcentral.com', 'Asunción', 'Compra con factura', TRUE);

INSERT INTO stock (id, empresa_id, producto_nombre, current_stock, min_stock)
VALUES
(1, 1, 'Zapato deportivo negro', 2, 5),
(2, 1, 'Ojota clásica azul', 20, 5);


INSERT INTO venta (
    id,
    empresa_id,
    cliente_id,
    numero_operacion,
    fecha,
    total,
    metodo_pago,
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
    'EFECTIVO',
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
    'TRANSFERENCIA',
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
    subtotal
)
VALUES
(1, 1, 1, 'ZAP-001', 'Zapato deportivo negro', 1, 150000, 150000),
(2, 2, 2, 'OJT-001', 'Ojota clásica azul', 1, 85000, 85000);

SELECT setval(pg_get_serial_sequence('empresa', 'id'), COALESCE((SELECT MAX(id) FROM empresa), 1), true);
SELECT setval(pg_get_serial_sequence('usuario', 'id'), COALESCE((SELECT MAX(id) FROM usuario), 1), true);
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
    'Pagaré demo',
    1
);

SELECT setval(pg_get_serial_sequence('pagare', 'id'), COALESCE((SELECT MAX(id) FROM pagare), 1), true);
SELECT setval(pg_get_serial_sequence('pagare_pago', 'id'), COALESCE((SELECT MAX(id) FROM pagare_pago), 1), true);

UPDATE empresa
SET ruc = '80012345-6'
WHERE id = 1;

