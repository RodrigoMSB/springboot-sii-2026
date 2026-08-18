-- Las dos tablas que vienen del Lab 05, más la de hoy.

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

-- El folio: un correlativo por año. Fíjate en lo que NO tiene: nada impide
-- guardar dos veces el mismo (anio, numero). Eso llega en el paso 5.
create table folio (
    id      bigserial primary key,
    anio    integer   not null,
    numero  integer   not null
);
