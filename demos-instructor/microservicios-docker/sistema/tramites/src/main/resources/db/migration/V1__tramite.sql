-- Aquí NO hay tabla `contribuyente`, y no es un olvido: es la frontera del laboratorio.
-- Esta base es de trámites; la de contribuyentes es de otro servicio, en otro puerto.
CREATE TABLE tramite (
    id                BIGSERIAL PRIMARY KEY,
    rut_contribuyente VARCHAR(20) NOT NULL,
    tipo              VARCHAR(40) NOT NULL,
    estado            VARCHAR(30) NOT NULL,
    creado_en         TIMESTAMPTZ NOT NULL
);

INSERT INTO tramite (rut_contribuyente, tipo, estado, creado_en) VALUES
    ('11111111-1', 'DECLARACION_F29', 'EN_PROCESO', now()),
    ('22222222-2', 'INICIO_ACTIVIDADES', 'APROBADO', now());
