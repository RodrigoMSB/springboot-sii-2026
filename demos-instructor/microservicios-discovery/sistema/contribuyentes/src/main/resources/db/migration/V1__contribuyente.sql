CREATE TABLE contribuyente (
    id       BIGSERIAL PRIMARY KEY,
    rut      VARCHAR(20)  NOT NULL UNIQUE,
    nombre   VARCHAR(120) NOT NULL,
    segmento VARCHAR(40)  NOT NULL
);

INSERT INTO contribuyente (rut, nombre, segmento) VALUES
    ('11111111-1', 'Carolina Fuentes Aravena', 'PERSONA_NATURAL'),
    ('22222222-2', 'Comercial Los Andes SpA',  'PYME'),
    ('33333333-3', 'Minera Atacama Limitada',  'GRAN_EMPRESA');
