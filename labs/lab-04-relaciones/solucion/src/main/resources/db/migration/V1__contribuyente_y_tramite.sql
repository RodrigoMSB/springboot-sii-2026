-- Las dos tablas del arco. Nada más: lo mínimo para que la relación se vea.
--
-- Fíjate en la última línea de `tramite`: la columna `contribuyente_id` con su
-- REFERENCES. Ahí vive la relación en la base. En la clase Java se declara con
-- @ManyToOne, y las dos cosas tienen que decir lo mismo.

create table contribuyente (
    id            bigserial primary key,
    rut           varchar(12)  not null unique,
    razon_social  varchar(120) not null
);

create table tramite (
    id                bigserial   primary key,
    tipo              varchar(40) not null,
    estado            varchar(20) not null,
    fecha             date        not null,
    contribuyente_id  bigint      not null references contribuyente (id)
);
