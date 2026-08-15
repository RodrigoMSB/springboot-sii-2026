-- =============================================================================
--  V1 — Esquema base de la DGT
-- -----------------------------------------------------------------------------
--  Las constraints que sostienen reglas de negocio nacen AQUÍ, no se agregan
--  cuando duelen. Dos ausencias son deliberadas y están explicadas abajo.
-- =============================================================================

CREATE TABLE contribuyente (
    id                      BIGSERIAL PRIMARY KEY,
    rut                     VARCHAR(12)  NOT NULL,
    razon_social            VARCHAR(200) NOT NULL,
    -- RN-03: este puntaje jamás sale por la API. Lo vigilan AU-02 (estático)
    -- y un test de contrato sobre el JSON serializado (dinámico).
    puntaje_riesgo_interno  INTEGER      NOT NULL DEFAULT 0,
    CONSTRAINT uk_contribuyente_rut UNIQUE (rut)
);

CREATE TABLE usuario (
    id          BIGSERIAL PRIMARY KEY,
    rut         VARCHAR(12)  NOT NULL,
    nombre      VARCHAR(200) NOT NULL,
    -- RN-03: el hash jamás sale por la API.
    clave_hash  VARCHAR(72)  NOT NULL,
    rol         VARCHAR(20)  NOT NULL,
    CONSTRAINT uk_usuario_rut UNIQUE (rut),
    CONSTRAINT ck_usuario_rol CHECK (rol IN ('CONTRIBUYENTE', 'FUNCIONARIO', 'FISCALIZADOR'))
);

CREATE TABLE tramite (
    id               BIGSERIAL PRIMARY KEY,
    contribuyente_id BIGINT      NOT NULL REFERENCES contribuyente (id),
    tipo             VARCHAR(50) NOT NULL,
    -- La máquina de estados es testeable: BORRADOR -> PRESENTADO -> PAGADO -> FOLIADO.
    estado           VARCHAR(20) NOT NULL,
    CONSTRAINT ck_tramite_estado CHECK (estado IN ('BORRADOR', 'PRESENTADO', 'PAGADO', 'FOLIADO'))
);

CREATE TABLE formulario29 (
    id         BIGSERIAL PRIMARY KEY,
    tramite_id BIGINT     NOT NULL REFERENCES tramite (id),
    periodo    VARCHAR(7) NOT NULL,
    CONSTRAINT uk_formulario29_tramite UNIQUE (tramite_id)
);
-- Ausencia deliberada nº1: NO hay columna `total`.
-- RN-06 es DERIVADO — Formulario29.total() suma sus líneas en el agregado.
-- Un invariante que necesita un test para sostenerse ya perdió; este no puede
-- violarse porque no existe un lugar donde escribir un total incorrecto.

CREATE TABLE linea_f29 (
    id              BIGSERIAL PRIMARY KEY,
    formulario29_id BIGINT      NOT NULL REFERENCES formulario29 (id),
    codigo          VARCHAR(10) NOT NULL,
    monto           BIGINT      NOT NULL
);
-- Ausencia deliberada nº2: NO hay CHECK (monto >= 0).
-- Ese contrato se agrega en el lab del Módulo 8 ("restricciones como
-- contratos", vía migración). No se regala aquí: la lección es ponerlo.

CREATE TABLE folio (
    numero     BIGINT PRIMARY KEY,          -- RN-01: irrepetible (es la PK)
    tramite_id BIGINT NOT NULL REFERENCES tramite (id),
    -- Un trámite, a lo más un folio. Es el suelo de RN-05 (idempotencia por
    -- tramiteId): sin este UNIQUE, un reintento crearía un segundo folio.
    CONSTRAINT uk_folio_tramite UNIQUE (tramite_id)
);

CREATE TABLE adjunto (
    id             BIGSERIAL PRIMARY KEY,
    tramite_id     BIGINT       NOT NULL REFERENCES tramite (id),
    nombre_archivo VARCHAR(255) NOT NULL,
    -- El MIME REAL, sniffeado del contenido; no el que declara el navegador.
    mime_real      VARCHAR(100) NOT NULL
);

-- -----------------------------------------------------------------------------
--  contador_folio — TABLA TÉCNICA, no entidad de dominio.
-- -----------------------------------------------------------------------------
--  Soporte de RN-02 (folios secuenciales SIN SALTOS). El Lab 06 la bloquea con
--  SELECT ... FOR UPDATE dentro de la misma transacción que persiste el folio.
--
--  ¿Por qué no una SEQUENCE de PostgreSQL? Porque una sequence es NO
--  transaccional: si la transacción revierte, el número consumido no vuelve, y
--  el libro de folios queda con un hueco. Un hueco en un libro de folios no se
--  borra: se explica, ante un fiscalizador.
CREATE TABLE contador_folio (
    id            SMALLINT PRIMARY KEY,
    ultimo_numero BIGINT NOT NULL
);
INSERT INTO contador_folio (id, ultimo_numero) VALUES (1, 0);

CREATE INDEX ix_tramite_contribuyente ON tramite (contribuyente_id);
CREATE INDEX ix_linea_f29_formulario  ON linea_f29 (formulario29_id);
CREATE INDEX ix_adjunto_tramite       ON adjunto (tramite_id);
