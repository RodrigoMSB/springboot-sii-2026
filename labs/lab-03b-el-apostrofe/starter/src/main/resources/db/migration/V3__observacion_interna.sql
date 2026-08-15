-- =============================================================================
--  V3 — observacion_interna
-- -----------------------------------------------------------------------------
--  Notas que los funcionarios escriben sobre un contribuyente. Son INTERNAS: no
--  salen por la API pública, y por eso viven en un endpoint que solo existe en
--  el perfil `dev`.
--
--  Esta tabla llega SIN entidad JPA a propósito. En el `starter` la lee un DAO
--  de JDBC crudo heredado, y mapearla es el trabajo del Lab 3.5.
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

-- -----------------------------------------------------------------------------
--  Semilla — DOS contribuyentes, y eso importa.
-- -----------------------------------------------------------------------------
--  El apóstrofe del Lab 3.5 no demuestra nada si en la tabla solo hay filas de
--  una persona: filtrarlas todas se vería igual que filtrar las suyas. Las
--  observaciones de «Comercial Andina SpA» son las que NO deberían aparecer
--  jamás cuando alguien pregunta por Valentina Rojas.
-- -----------------------------------------------------------------------------
INSERT INTO observacion_interna (contribuyente_id, texto, autor) VALUES
    ((SELECT id FROM contribuyente WHERE rut = '11111111-1'),
     'Presenta sus declaraciones dentro de plazo. Sin observaciones.', 'Carolina Espinoza'),
    ((SELECT id FROM contribuyente WHERE rut = '11111111-1'),
     'Solicitó certificado de situación tributaria en agosto.',       'Carolina Espinoza'),
    ((SELECT id FROM contribuyente WHERE rut = '12345678-5'),
     'Diferencias reiteradas entre F29 declarado y pagado.',          'Ignacio Bravo'),
    ((SELECT id FROM contribuyente WHERE rut = '12345678-5'),
     'Fiscalización en curso. NO divulgar fuera del área.',           'Ignacio Bravo'),
    ((SELECT id FROM contribuyente WHERE rut = '12345678-5'),
     'Representante legal no ubicable en el domicilio registrado.',   'Ignacio Bravo');
