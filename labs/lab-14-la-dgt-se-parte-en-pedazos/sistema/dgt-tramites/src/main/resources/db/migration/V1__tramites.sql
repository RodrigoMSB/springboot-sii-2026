-- =============================================================================
--  V1 — la tabla de trámites y su semilla
-- -----------------------------------------------------------------------------
--  Base PROPIA de este servicio (`dgt_tramites`), con su usuario propio. No hay
--  clave foránea hacia `contribuyente` y no puede haberla: esa tabla vive en
--  otra base, a la que este servicio no tiene acceso.
--
--  Esa ausencia de clave foránea es el precio real de partir un sistema. El
--  motor ya no puede garantizar que `rut_contribuyente` apunte a alguien que
--  existe. Ahora eso es responsabilidad de la aplicación — o de nadie.
-- =============================================================================

CREATE TABLE tramite (
    id                BIGSERIAL    PRIMARY KEY,
    tipo              VARCHAR(40)  NOT NULL,
    rut_contribuyente VARCHAR(12)  NOT NULL,
    estado            VARCHAR(20)  NOT NULL
);

INSERT INTO tramite (tipo, rut_contribuyente, estado) VALUES
    ('DECLARACION_F29', '11111111-1', 'EN_PROCESO'),
    ('DECLARACION_F29', '12345678-5', 'APROBADO'),
    ('SOLICITUD_TIMBRAJE', '12345678-5', 'EN_PROCESO');
