-- =============================================================================
--  V3 — observacion_interna
-- -----------------------------------------------------------------------------
--  Notas que los funcionarios escriben sobre un contribuyente. Son INTERNAS: no
--  salen por la API pública, y por eso viven en un endpoint que solo existe en
--  el perfil `dev`.
--
--  La tabla llega VACÍA y sin entidad JPA, y las dos cosas son a propósito: el
--  Lab 3.5 consiste en escribir el código Java que la conecta con la aplicación,
--  y en guardar la primera fila desde un objeto.
-- =============================================================================

CREATE TABLE observacion_interna (
    id               BIGSERIAL    PRIMARY KEY,
    contribuyente_id BIGINT       NOT NULL REFERENCES contribuyente (id),
    texto            VARCHAR(500) NOT NULL,
    autor            VARCHAR(200) NOT NULL,
    creada_en        TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Se busca siempre por contribuyente: el índice nace con la tabla.
CREATE INDEX ix_observacion_interna_contribuyente ON observacion_interna (contribuyente_id);
