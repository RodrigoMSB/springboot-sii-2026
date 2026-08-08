-- =============================================================================
--  V1 — la tabla de contribuyentes y su semilla
-- -----------------------------------------------------------------------------
--  Flyway, no ddl-auto=update (corrección 3 de la SPEC-020 §6). El esquema es un
--  artefacto versionado: se revisa en el PR, se despliega con la aplicación y se
--  puede leer dentro de un año para saber qué había en la tabla. Un `update` de
--  Hibernate no deja rastro de nada.
--
--  La semilla va AQUÍ, en la migración, y no en un CommandLineRunner que la
--  recargue en cada arranque (corrección 5): se inserta una vez, y el
--  `ON CONFLICT DO NOTHING` hace que un segundo arranque no duplique nada.
--
--  Los datos son los canónicos del curso (SPEC-000 §3), los mismos que el alumno
--  lleva viendo trece sesiones.
-- =============================================================================

CREATE TABLE contribuyente (
    id           BIGSERIAL     PRIMARY KEY,
    rut          VARCHAR(12)   NOT NULL UNIQUE,
    razon_social VARCHAR(200)  NOT NULL
);

INSERT INTO contribuyente (rut, razon_social) VALUES
    ('11111111-1', 'Valentina Rojas'),
    ('12345678-5', 'Comercial Andina SpA')
ON CONFLICT (rut) DO NOTHING;
