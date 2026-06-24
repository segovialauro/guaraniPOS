CREATE TABLE empresa_licencia_historial (
    id BIGSERIAL PRIMARY KEY,
    empresa_id BIGINT NOT NULL REFERENCES empresa(id),
    usuario_id BIGINT NULL REFERENCES usuario(id),
    vencimiento_anterior DATE NOT NULL,
    vencimiento_nuevo DATE NOT NULL,
    fecha_cambio TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_empresa_licencia_historial_empresa_fecha
    ON empresa_licencia_historial (empresa_id, fecha_cambio DESC);
