-- =============================================================================
--  V1 — La tabla del laboratorio
-- -----------------------------------------------------------------------------
--  Una sola tabla, sin relaciones con nadie. Las relaciones entre entidades son
--  el Lab 05; hoy el tema es más básico: cómo una fila y un objeto Java pasan a
--  ser la misma cosa.
--
--  La crea Flyway al arrancar. Tú no la tocas: lo que escribes es la clase Java
--  que se corresponde con ella.
-- =============================================================================

CREATE TABLE observacion (
    id     BIGSERIAL    PRIMARY KEY,
    texto  VARCHAR(500) NOT NULL,
    autor  VARCHAR(100) NOT NULL,
    fecha  DATE         NOT NULL
);
