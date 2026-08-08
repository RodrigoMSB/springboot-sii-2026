-- =============================================================================
--  V4 — El cierre nocturno (Módulo 10)
-- -----------------------------------------------------------------------------
--  Cada noche, la DGT consolida los trámites del día: cuántos hubo y cuánto se
--  declaró. Es el resumen que Carolina mira a primera hora.
--
--  AUSENCIA DELIBERADA: `fecha` NO es UNIQUE.
--
--  La tentación es obvia —"un cierre por día, pon un UNIQUE y listo"— y sería
--  tapar el crimen del laboratorio con una constraint. Con UNIQUE, la segunda
--  instancia reventaría con `duplicate key` y el alumno vería un error de base
--  de datos, no una DOBLE EJECUCIÓN. Aprendería a leer un stacktrace en vez de
--  aprender que su tarea programada corre tantas veces como instancias tenga.
--
--  El crimen tiene que ser VISIBLE, no capturado: dos filas para el mismo día,
--  dos correos al mismo contribuyente. La base no miente ni disimula.
--
--  (Y ojo con la lección de fondo: un UNIQUE aquí protegería la tabla, no el
--  trabajo. Las notificaciones ya habrían salido dos veces antes del INSERT.
--  Una constraint no es un candado.)
-- =============================================================================

CREATE TABLE cierre_diario (
    id               BIGSERIAL PRIMARY KEY,
    fecha            DATE        NOT NULL,
    tramites         INTEGER     NOT NULL,
    total_declarado  BIGINT      NOT NULL,
    ejecutado_en     TIMESTAMP   NOT NULL,
    -- Quién lo ejecutó. En una sola instancia es ruido; con dos, es la prueba
    -- del delito: dos filas del mismo día con dos instancias distintas.
    instancia        VARCHAR(80) NOT NULL
);

CREATE INDEX ix_cierre_diario_fecha ON cierre_diario (fecha);
