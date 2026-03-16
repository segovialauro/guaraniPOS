DROP TABLE IF EXISTS venta_pago CASCADE;
DROP TABLE IF EXISTS venta_detalle CASCADE;
DROP TABLE IF EXISTS venta CASCADE;
DROP TABLE IF EXISTS producto CASCADE;
DROP TABLE IF EXISTS cliente CASCADE;
DROP TABLE IF EXISTS pagare_pago CASCADE;
DROP TABLE IF EXISTS pagare CASCADE;
DROP TABLE IF EXISTS presupuesto CASCADE;
DROP TABLE IF EXISTS stock CASCADE;
DROP TABLE IF EXISTS usuario CASCADE;
DROP TABLE IF EXISTS empresa CASCADE;
DROP TABLE IF EXISTS presupuesto_detalle CASCADE;
DROP TABLE IF EXISTS presupuesto CASCADE;
DROP TABLE IF EXISTS caja_turno CASCADE;
DROP TABLE IF EXISTS caja_movimiento CASCADE;


CREATE TABLE empresa (
    id BIGSERIAL PRIMARY KEY,
    codigo VARCHAR(50) NOT NULL UNIQUE,
    nombre VARCHAR(150) NOT NULL,
    estado VARCHAR(20) NOT NULL,
    licencia_estado VARCHAR(20) NOT NULL,
    licencia_vencimiento DATE NOT NULL,
    ruc VARCHAR(20) UNIQUE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE usuario (
    id BIGSERIAL PRIMARY KEY,
    empresa_id BIGINT NOT NULL REFERENCES empresa(id),
    cedula VARCHAR(20) NOT NULL,
    nombre_completo VARCHAR(150) NOT NULL,
    password_hash VARCHAR(120) NOT NULL,
    quick_pin VARCHAR(4),
    rol_codigo VARCHAR(50) NOT NULL,
    estado VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE (empresa_id, cedula),
    UNIQUE (empresa_id, quick_pin)
);

CREATE TABLE producto (
    id BIGSERIAL PRIMARY KEY,
    empresa_id BIGINT NOT NULL,
    codigo VARCHAR(50) NOT NULL,
    nombre VARCHAR(150) NOT NULL,
    descripcion VARCHAR(500),
    categoria VARCHAR(100),
    precio_costo NUMERIC(15,2) NOT NULL DEFAULT 0,
    precio_venta NUMERIC(15,2) NOT NULL,
    stock_actual NUMERIC(15,2) NOT NULL DEFAULT 0,
    stock_minimo NUMERIC(15,2) NOT NULL DEFAULT 0,
    unidad_medida VARCHAR(30) NOT NULL DEFAULT 'UNIDAD',
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    creado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    actualizado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_producto_empresa
        FOREIGN KEY (empresa_id) REFERENCES empresa(id)
);

CREATE UNIQUE INDEX uq_producto_empresa_codigo
    ON producto (empresa_id, codigo);

CREATE TABLE cliente (
    id BIGSERIAL PRIMARY KEY,
    empresa_id BIGINT NOT NULL,
    nombre VARCHAR(150) NOT NULL,
    documento VARCHAR(30),
    ruc VARCHAR(30),
    telefono VARCHAR(30),
    email VARCHAR(150),
    direccion VARCHAR(250),
    observacion VARCHAR(500),
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    creado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    actualizado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_cliente_empresa
        FOREIGN KEY (empresa_id) REFERENCES empresa(id)
);

CREATE INDEX ix_cliente_empresa_nombre
    ON cliente (empresa_id, nombre);

CREATE INDEX ix_cliente_empresa_documento
    ON cliente (empresa_id, documento);

CREATE INDEX ix_cliente_empresa_ruc
    ON cliente (empresa_id, ruc);

CREATE TABLE stock (
    id BIGSERIAL PRIMARY KEY,
    empresa_id BIGINT NOT NULL,
    producto_nombre VARCHAR(150) NOT NULL,
    current_stock NUMERIC(15,2) NOT NULL,
    min_stock NUMERIC(15,2) NOT NULL,
    CONSTRAINT fk_stock_empresa
        FOREIGN KEY (empresa_id) REFERENCES empresa(id)
);

CREATE TABLE venta (
    id BIGSERIAL PRIMARY KEY,
    empresa_id BIGINT NOT NULL,
    cliente_id BIGINT NULL,
    numero_operacion VARCHAR(30) NOT NULL,
    fecha TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    total NUMERIC(15,2) NOT NULL,
    metodo_pago VARCHAR(30) NOT NULL,
    monto_recibido NUMERIC(15,2),
    vuelto NUMERIC(15,2),
    estado VARCHAR(20) NOT NULL,
    observacion VARCHAR(500),
    creado_por_usuario_id BIGINT NULL,
    CONSTRAINT fk_venta_empresa
        FOREIGN KEY (empresa_id) REFERENCES empresa(id),
    CONSTRAINT fk_venta_cliente
        FOREIGN KEY (cliente_id) REFERENCES cliente(id),
    CONSTRAINT fk_venta_usuario
        FOREIGN KEY (creado_por_usuario_id) REFERENCES usuario(id)
);

CREATE UNIQUE INDEX uq_venta_empresa_numero_operacion
    ON venta (empresa_id, numero_operacion);

CREATE TABLE venta_detalle (
    id BIGSERIAL PRIMARY KEY,
    venta_id BIGINT NOT NULL,
    producto_id BIGINT NOT NULL,
    producto_codigo VARCHAR(50) NOT NULL,
    producto_nombre VARCHAR(150) NOT NULL,
    cantidad NUMERIC(15,2) NOT NULL,
    precio_unitario NUMERIC(15,2) NOT NULL,
    subtotal NUMERIC(15,2) NOT NULL,
    CONSTRAINT fk_venta_detalle_venta
        FOREIGN KEY (venta_id) REFERENCES venta(id),
    CONSTRAINT fk_venta_detalle_producto
        FOREIGN KEY (producto_id) REFERENCES producto(id)
);

alter table producto add column if not exists qr_contenido varchar(255);
ALTER TABLE producto
ADD COLUMN IF NOT EXISTS codigo_barras varchar(100);

create table if not exists presupuesto (
    id bigserial primary key,
    empresa_id bigint not null,
    cliente_id bigint null,
    numero_presupuesto varchar(30) not null,
    fecha timestamp not null default current_timestamp,
    vigencia_hasta date null,
    total numeric(15,2) not null,
    estado varchar(20) not null,
    observacion varchar(500),
    creado_por_usuario_id bigint null,
    constraint fk_presupuesto_empresa
        foreign key (empresa_id) references empresa(id),
    constraint fk_presupuesto_cliente
        foreign key (cliente_id) references cliente(id),
    constraint fk_presupuesto_usuario
        foreign key (creado_por_usuario_id) references usuario(id)
);

create unique index if not exists uq_presupuesto_empresa_numero
    on presupuesto (empresa_id, numero_presupuesto);

create table if not exists presupuesto_detalle (
    id bigserial primary key,
    presupuesto_id bigint not null,
    producto_id bigint not null,
    producto_codigo varchar(50) not null,
    producto_nombre varchar(150) not null,
    cantidad numeric(15,2) not null,
    precio_unitario numeric(15,2) not null,
    subtotal numeric(15,2) not null,
    constraint fk_presupuesto_detalle_presupuesto
        foreign key (presupuesto_id) references presupuesto(id),
    constraint fk_presupuesto_detalle_producto
        foreign key (producto_id) references producto(id)
);

create table if not exists pagare (
    id bigserial primary key,
    empresa_id bigint not null,
    cliente_id bigint not null,
    venta_id bigint null,
    numero_pagare varchar(30) not null,
    fecha_emision date not null,
    fecha_vencimiento date not null,
    monto numeric(15,2) not null,
    saldo numeric(15,2) not null,
    estado varchar(20) not null,
    observacion varchar(500),
    creado_por_usuario_id bigint null,
    constraint fk_pagare_empresa
        foreign key (empresa_id) references empresa(id),
    constraint fk_pagare_cliente
        foreign key (cliente_id) references cliente(id),
    constraint fk_pagare_venta
        foreign key (venta_id) references venta(id),
    constraint fk_pagare_usuario
        foreign key (creado_por_usuario_id) references usuario(id)
);

create unique index if not exists uq_pagare_empresa_numero
    on pagare (empresa_id, numero_pagare);

create table if not exists pagare_pago (
    id bigserial primary key,
    pagare_id bigint not null,
    fecha_pago timestamp not null default current_timestamp,
    monto numeric(15,2) not null,
    metodo_pago varchar(30) not null,
    observacion varchar(500),
    constraint fk_pagare_pago_pagare
        foreign key (pagare_id) references pagare(id)
);

create table if not exists caja_turno (
    id bigserial primary key,
    empresa_id bigint not null,
    usuario_id bigint not null,
    fecha_apertura timestamp not null default current_timestamp,
    fecha_cierre timestamp null,
    monto_apertura numeric(15,2) not null,
    efectivo_sistema numeric(15,2) not null default 0,
    transferencia_sistema numeric(15,2) not null default 0,
    tarjeta_debito_sistema numeric(15,2) not null default 0,
    tarjeta_credito_sistema numeric(15,2) not null default 0,
    qr_sistema numeric(15,2) not null default 0,
    total_sistema numeric(15,2) not null default 0,
    efectivo_contado numeric(15,2) null,
    difference numeric(15,2) null,
    estado varchar(20) not null,
    observacion varchar(500),
    constraint fk_caja_turno_empresa
        foreign key (empresa_id) references empresa(id),
    constraint fk_caja_turno_usuario
        foreign key (usuario_id) references usuario(id)
);

create table if not exists caja_movimiento (
    id bigserial primary key,
    caja_turno_id bigint not null,
    empresa_id bigint not null,
    usuario_id bigint not null,
    fecha timestamp not null default current_timestamp,
    tipo varchar(20) not null,
    monto numeric(15,2) not null,
    descripcion varchar(500),
    estado varchar(20) not null default 'ACTIVO',
    fecha_actualizacion timestamp,
    usuario_actualizacion_id bigint,
    fecha_anulacion timestamp,
    usuario_anulacion_id bigint,
    motivo_anulacion varchar(500),

    constraint fk_caja_movimiento_turno
        foreign key (caja_turno_id) references caja_turno(id),

    constraint fk_caja_movimiento_empresa
        foreign key (empresa_id) references empresa(id),

    constraint fk_caja_movimiento_usuario
        foreign key (usuario_id) references usuario(id),

    constraint fk_caja_movimiento_usuario_actualizacion
        foreign key (usuario_actualizacion_id) references usuario(id),

    constraint fk_caja_movimiento_usuario_anulacion
        foreign key (usuario_anulacion_id) references usuario(id)
);

create index if not exists idx_caja_movimiento_turno_fecha
    on caja_movimiento (caja_turno_id, fecha desc);

CREATE TABLE venta_pago (
    id BIGSERIAL PRIMARY KEY,
    venta_id BIGINT NOT NULL,
    caja_turno_id BIGINT NULL,
    metodo_pago VARCHAR(30) NOT NULL,
    monto NUMERIC(15,2) NOT NULL,
    referencia VARCHAR(100),
    CONSTRAINT fk_venta_pago_venta
        FOREIGN KEY (venta_id) REFERENCES venta(id),
    CONSTRAINT fk_venta_pago_caja_turno
        FOREIGN KEY (caja_turno_id) REFERENCES caja_turno(id)
);

    

