-- =============================================================================
--  V2 — Datos semilla de la DGT
-- -----------------------------------------------------------------------------
--  Universo heredado del curso de Cypress SII 2026: los mismos RUT, los mismos
--  nombres. Quien tomó aquel curso reconoce a Valentina y a Carolina.
--
--  Los `clave_hash` son BCrypt (cost 10) de la MISMA clave de laboratorio, y
--  están versionados a propósito: un hash no es un secreto. La clave en texto
--  plano vive en docs/clave-de-laboratorio.md, no aquí.
-- =============================================================================

INSERT INTO contribuyente (rut, razon_social, puntaje_riesgo_interno) VALUES
    ('11111111-1', 'Valentina Rojas',        12),
    ('12345678-5', 'Comercial Andina SpA',   67);

-- Carolina e Ignacio son funcionarios: son `usuario`, NUNCA `contribuyente`.
INSERT INTO usuario (rut, nombre, clave_hash, rol) VALUES
    ('11111111-1', 'Valentina Rojas',   '$2a$10$GcFz7025zzi0fEIGOHfq2OHQFXKrJPqWsj.zOU2GJJF8xiXFh/S6a', 'CONTRIBUYENTE'),
    ('9876543-2',  'Carolina Espinoza', '$2a$10$GcFz7025zzi0fEIGOHfq2OHQFXKrJPqWsj.zOU2GJJF8xiXFh/S6a', 'FUNCIONARIO'),
    ('8765432-1',  'Ignacio Bravo',     '$2a$10$GcFz7025zzi0fEIGOHfq2OHQFXKrJPqWsj.zOU2GJJF8xiXFh/S6a', 'FISCALIZADOR');

-- Trámites en estados variados: los primeros labs necesitan qué listar.
INSERT INTO tramite (contribuyente_id, tipo, estado) VALUES
    ((SELECT id FROM contribuyente WHERE rut = '11111111-1'), 'DECLARACION_F29',      'BORRADOR'),
    ((SELECT id FROM contribuyente WHERE rut = '11111111-1'), 'DECLARACION_F29',      'PRESENTADO'),
    ((SELECT id FROM contribuyente WHERE rut = '12345678-5'), 'DECLARACION_F29',      'PAGADO'),
    ((SELECT id FROM contribuyente WHERE rut = '12345678-5'), 'DECLARACION_F29',      'FOLIADO'),
    ((SELECT id FROM contribuyente WHERE rut = '12345678-5'), 'INICIO_ACTIVIDADES',   'PRESENTADO');

INSERT INTO formulario29 (tramite_id, periodo)
SELECT t.id, p.periodo
FROM tramite t
JOIN (VALUES (1, '2026-04'), (2, '2026-05'), (3, '2026-05'), (4, '2026-06')) AS p(orden, periodo)
  ON p.orden = t.id
WHERE t.tipo = 'DECLARACION_F29';

-- Líneas del F29. El `total` NO se guarda: lo deriva Formulario29.total() (RN-06).
INSERT INTO linea_f29 (formulario29_id, codigo, monto)
SELECT f.id, l.codigo, l.monto
FROM formulario29 f
JOIN (VALUES
        (1, '538',  1250000), (1, '511',  -340000),
        (2, '538',  2100000), (2, '511',  -560000), (2, '062', 85000),
        (3, '538',  9800000), (3, '511', -2400000),
        (4, '538',  4500000), (4, '062',  120000)
     ) AS l(orden, codigo, monto)
  ON l.orden = f.id;

-- El trámite FOLIADO ya tiene su folio. El contador refleja ese estado: si el
-- contador no avanzara con la semilla, el Lab 06 emitiría un folio repetido.
INSERT INTO folio (numero, tramite_id)
VALUES (1, (SELECT id FROM tramite WHERE estado = 'FOLIADO'));

UPDATE contador_folio SET ultimo_numero = 1 WHERE id = 1;

INSERT INTO adjunto (tramite_id, nombre_archivo, mime_real) VALUES
    ((SELECT id FROM tramite WHERE estado = 'PRESENTADO' LIMIT 1), 'balance-2026.pdf', 'application/pdf'),
    ((SELECT id FROM tramite WHERE estado = 'PAGADO'),             'comprobante.png',  'image/png');
