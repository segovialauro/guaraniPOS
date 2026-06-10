ALTER TABLE configuracion_facturacion
    ADD COLUMN IF NOT EXISTS taxpayer_type VARCHAR(1),
    ADD COLUMN IF NOT EXISTS tax_regime_code VARCHAR(1),
    ADD COLUMN IF NOT EXISTS economic_activity_code VARCHAR(20),
    ADD COLUMN IF NOT EXISTS qr_security_code_id VARCHAR(4),
    ADD COLUMN IF NOT EXISTS qr_security_code VARCHAR(32),
    ADD COLUMN IF NOT EXISTS electronic_series VARCHAR(2);

CREATE TABLE IF NOT EXISTS factura_electronica (
    id BIGSERIAL PRIMARY KEY,
    empresa_id BIGINT NOT NULL,
    venta_id BIGINT NOT NULL,
    configuracion_facturacion_id BIGINT NOT NULL,
    version_formato VARCHAR(10) NOT NULL DEFAULT '150',
    document_type_code VARCHAR(2) NOT NULL,
    document_type_description VARCHAR(60) NOT NULL,
    emission_type_code VARCHAR(1) NOT NULL DEFAULT '1',
    emission_type_description VARCHAR(20) NOT NULL DEFAULT 'Normal',
    taxpayer_type VARCHAR(1) NOT NULL,
    series VARCHAR(2),
    document_number VARCHAR(7) NOT NULL,
    cdc VARCHAR(44) NOT NULL,
    cdc_check_digit VARCHAR(1) NOT NULL,
    security_code VARCHAR(9) NOT NULL,
    digest_value VARCHAR(500),
    qr_payload TEXT,
    qr_url TEXT,
    unsigned_xml TEXT NOT NULL,
    signed_xml TEXT,
    status VARCHAR(40) NOT NULL,
    status_detail VARCHAR(500),
    environment VARCHAR(20) NOT NULL,
    batch_number VARCHAR(20),
    transaction_number VARCHAR(20),
    reception_status_code VARCHAR(10),
    reception_status_message VARCHAR(255),
    processing_status_code VARCHAR(10),
    processing_status_message VARCHAR(255),
    submitted_at TIMESTAMP,
    approved_at TIMESTAMP,
    rejected_at TIMESTAMP,
    canceled_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_factura_electronica_empresa
        FOREIGN KEY (empresa_id) REFERENCES empresa(id),
    CONSTRAINT fk_factura_electronica_venta
        FOREIGN KEY (venta_id) REFERENCES venta(id),
    CONSTRAINT fk_factura_electronica_configuracion
        FOREIGN KEY (configuracion_facturacion_id) REFERENCES configuracion_facturacion(id),
    CONSTRAINT uq_factura_electronica_venta UNIQUE (venta_id),
    CONSTRAINT uq_factura_electronica_cdc UNIQUE (cdc)
);

CREATE INDEX IF NOT EXISTS ix_factura_electronica_empresa_estado
    ON factura_electronica (empresa_id, status, created_at DESC);

CREATE TABLE IF NOT EXISTS factura_electronica_evento (
    id BIGSERIAL PRIMARY KEY,
    factura_electronica_id BIGINT NOT NULL,
    event_type VARCHAR(40) NOT NULL,
    event_status VARCHAR(30) NOT NULL,
    event_payload TEXT,
    event_xml TEXT,
    response_code VARCHAR(10),
    response_message VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    processed_at TIMESTAMP,
    CONSTRAINT fk_factura_electronica_evento_factura
        FOREIGN KEY (factura_electronica_id) REFERENCES factura_electronica(id)
);

CREATE INDEX IF NOT EXISTS ix_factura_electronica_evento_factura
    ON factura_electronica_evento (factura_electronica_id, created_at DESC);
