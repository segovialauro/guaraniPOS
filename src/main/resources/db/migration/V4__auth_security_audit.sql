CREATE TABLE IF NOT EXISTS seguridad_autenticacion_evento (
    id BIGSERIAL PRIMARY KEY,
    empresa_id BIGINT,
    tenant_code VARCHAR(50) NOT NULL,
    event_type VARCHAR(40) NOT NULL,
    access_channel VARCHAR(20) NOT NULL,
    subject_identifier VARCHAR(100),
    client_ip VARCHAR(100),
    failure_count INTEGER,
    blocked_until TIMESTAMP,
    detail VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_seguridad_autenticacion_evento_empresa
        FOREIGN KEY (empresa_id) REFERENCES empresa(id)
);

CREATE INDEX IF NOT EXISTS ix_seguridad_auth_evento_empresa_fecha
    ON seguridad_autenticacion_evento (empresa_id, created_at DESC);

CREATE INDEX IF NOT EXISTS ix_seguridad_auth_evento_tenant_fecha
    ON seguridad_autenticacion_evento (tenant_code, created_at DESC);
