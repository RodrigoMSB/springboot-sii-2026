-- Tres tablas para dos entidades. Ésa es toda la novedad del laboratorio.
--
-- `tramite` y `documento` son las dos que se ven en Java. La tercera,
-- `tramite_documento`, NO tiene clase: existe solo para guardar los pares. En el
-- lab 05 la relación era una columna; aquí es una tabla.
--
-- Fíjate en su clave primaria: las DOS columnas juntas. Es lo que impide adjuntar
-- dos veces el mismo documento al mismo trámite, y es la razón de que un `Set`
-- sea el tipo que dice la verdad sobre esta tabla.

create table tramite (
    id     bigserial   primary key,
    tipo   varchar(40) not null,
    rut    varchar(12) not null,
    fecha  date        not null
);

create table documento (
    id      bigserial    primary key,
    codigo  varchar(20)  not null unique,
    nombre  varchar(120) not null
);

create table tramite_documento (
    tramite_id    bigint not null references tramite (id),
    documento_id  bigint not null references documento (id),
    primary key (tramite_id, documento_id)
);
