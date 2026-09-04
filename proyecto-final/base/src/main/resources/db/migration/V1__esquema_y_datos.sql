-- ============================================================================
--  El esquema del proyecto final, con datos sembrados.
--  Llega hecho: modelar la base no es lo que se evalúa hoy.
-- ============================================================================

create table contribuyente (
    id           bigserial    primary key,
    rut          varchar(12)  not null unique,
    razon_social varchar(120) not null
);

-- La oficina que tramitó cada expediente. Existe para el EJEMPLO resuelto que hay
-- en `ejemplo/`: allí el encargo es el resumen de una oficina, con la misma forma
-- que el consolidado de un contribuyente. Aquí no estorba y no entra en el encargo.
create table oficina (
    id     bigserial   primary key,
    codigo varchar(10) not null unique,
    nombre varchar(80) not null
);

create table tramite (
    id               bigserial     primary key,
    tipo             varchar(40)   not null,
    estado           varchar(20)   not null,
    fecha            date          not null,
    monto            numeric(12,2) not null,
    contribuyente_id bigint        not null references contribuyente (id),
    oficina_codigo   varchar(10)   not null references oficina (codigo)
);

create table usuario (
    id         bigserial   primary key,
    nombre     varchar(60) not null unique,
    clave_hash varchar(120) not null,   -- 120: un hash Argon2 mide ~95 y su largo depende de los parámetros
    rol        varchar(20) not null
);

-- ---------------------------------------------------------------------------
--  Usuarios. Las dos claves son `dgt2026`, con hash Argon2id — el mismo
--  algoritmo que enseña el Lab 09. Los dos hashes son distintos: es la sal.
--     ana   FISCALIZADOR
--     luis  CONTRIBUYENTE
-- ---------------------------------------------------------------------------
insert into usuario (nombre, clave_hash, rol) values
  ('ana',  '$argon2id$v=19$m=16384,t=2,p=1$RfCQMhqOIR2PP0ab3OUM5Q$fG0zPxCafxTuSVAaCkOgcJPSMOIDRAiXQv+awlgC29Y', 'FISCALIZADOR'),
  ('luis', '$argon2id$v=19$m=16384,t=2,p=1$BcXeXiLj1vY6WkjoBuWfSg$kUpG7TRm237LW6cM0E861/jlcgRaQaJjMtxP87rnK0s', 'CONTRIBUYENTE');

-- ---------------------------------------------------------------------------
--  Tres oficinas. La tercera NO tiene trámites: es el borde del ejemplo, igual
--  que el tercer contribuyente lo es del encargo.
-- ---------------------------------------------------------------------------
insert into oficina (codigo, nombre) values
  ('SCL-CEN', 'Santiago Centro'),
  ('VAP-PTO', 'Valparaíso Puerto'),
  ('ANF-NOR', 'Antofagasta Norte');

-- ---------------------------------------------------------------------------
--  Tres contribuyentes. El tercero NO tiene trámites: es uno de los bordes.
-- ---------------------------------------------------------------------------
insert into contribuyente (rut, razon_social) values
  ('76.111.111-1', 'Comercial Andes Ltda.'),
  ('77.222.222-2', 'Servicios Pacífico SpA'),
  ('78.333.333-3', 'Inversiones Atacama Ltda.');

-- ---------------------------------------------------------------------------
--  Trámites repartidos en dos años, para que el filtro por período importe.
-- ---------------------------------------------------------------------------
insert into tramite (tipo, estado, fecha, monto, contribuyente_id, oficina_codigo) values
  ('F29', 'PAGADO',    date '2026-01-15',  1200000.00, 1, 'SCL-CEN'),
  ('F29', 'PAGADO',    date '2026-02-15',   950000.00, 1, 'SCL-CEN'),
  ('F22', 'PENDIENTE', date '2026-03-10',  3400000.00, 1, 'VAP-PTO'),
  ('F29', 'PAGADO',    date '2026-04-15',   780000.00, 1, 'SCL-CEN'),
  ('F29', 'RECHAZADO', date '2025-11-15',   500000.00, 1, 'SCL-CEN'),
  ('F22', 'PAGADO',    date '2025-06-30',  2100000.00, 1, 'VAP-PTO'),
  ('F29', 'PAGADO',    date '2026-01-20',   640000.00, 2, 'SCL-CEN'),
  ('F22', 'PENDIENTE', date '2026-05-05',  1850000.00, 2, 'VAP-PTO');
