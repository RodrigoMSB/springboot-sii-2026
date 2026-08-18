-- ============================================================================
--  El esquema del proyecto final, con datos sembrados.
--  Llega hecho: modelar la base no es lo que se evalúa hoy.
-- ============================================================================

create table contribuyente (
    id             bigserial    primary key,
    rut            varchar(12)  not null unique,
    razon_social   varchar(120) not null,
    puntaje_riesgo int          not null
);

create table tramite (
    id               bigserial     primary key,
    tipo             varchar(40)   not null,
    estado           varchar(20)   not null,
    fecha            date          not null,
    monto            numeric(12,2) not null,
    contribuyente_id bigint        not null references contribuyente (id)
);

create table usuario (
    id         bigserial   primary key,
    nombre     varchar(60) not null unique,
    clave_hash varchar(60) not null,
    rol        varchar(20) not null
);

-- ---------------------------------------------------------------------------
--  Usuarios. Las dos claves son `secreta`, con hash BCrypt.
--     ana   FISCALIZADOR
--     luis  CONTRIBUYENTE
-- ---------------------------------------------------------------------------
insert into usuario (nombre, clave_hash, rol) values
  ('ana',  '$2a$10$z2RuZ6YymqMEOa9haqcN2.m1B31q1pL1oGfPzUUaYNbi43Lor3Lsy', 'FISCALIZADOR'),
  ('luis', '$2a$10$RBxoDtr9qH5oevKTWzwRaeKxD0Oc2pXrQtT07ayvBXO2h09HtqiN2', 'CONTRIBUYENTE');

-- ---------------------------------------------------------------------------
--  Tres contribuyentes. El tercero NO tiene trámites: es uno de los bordes.
-- ---------------------------------------------------------------------------
insert into contribuyente (rut, razon_social, puntaje_riesgo) values
  ('76.111.111-1', 'Comercial Andes Ltda.',      82),
  ('77.222.222-2', 'Servicios Pacífico SpA',     15),
  ('78.333.333-3', 'Inversiones Atacama Ltda.',  40);

-- ---------------------------------------------------------------------------
--  Trámites repartidos en dos años, para que el filtro por período importe.
-- ---------------------------------------------------------------------------
insert into tramite (tipo, estado, fecha, monto, contribuyente_id) values
  ('F29', 'PAGADO',    date '2026-01-15',  1200000.00, 1),
  ('F29', 'PAGADO',    date '2026-02-15',   950000.00, 1),
  ('F22', 'PENDIENTE', date '2026-03-10',  3400000.00, 1),
  ('F29', 'PAGADO',    date '2026-04-15',   780000.00, 1),
  ('F29', 'RECHAZADO', date '2025-11-15',   500000.00, 1),
  ('F22', 'PAGADO',    date '2025-06-30',  2100000.00, 1),
  ('F29', 'PAGADO',    date '2026-01-20',   640000.00, 2),
  ('F22', 'PENDIENTE', date '2026-05-05',  1850000.00, 2);
