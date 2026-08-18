CREATE TABLE usuario (
    id         BIGSERIAL PRIMARY KEY,
    nombre     VARCHAR(60)  NOT NULL UNIQUE,
    clave_hash VARCHAR(60)  NOT NULL,
    rol        VARCHAR(20)  NOT NULL
);
