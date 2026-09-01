CREATE TABLE registro_de_auditoria (
    id                BIGSERIAL PRIMARY KEY,
    evento            VARCHAR(60)  NOT NULL,
    tramite_id        BIGINT       NOT NULL,
    rut_contribuyente VARCHAR(20)  NOT NULL,
    trace_id          VARCHAR(40),
    recibido_en       TIMESTAMPTZ  NOT NULL
);
