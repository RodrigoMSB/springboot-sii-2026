-- ============================================================================
--  El esquema del examen, con datos sembrados.
--  Llega hecho: modelar la base no es lo que se evalúa hoy.
-- ============================================================================

create table oficina (
    id     bigserial   primary key,
    codigo varchar(10) not null unique,
    nombre varchar(80) not null,
    comuna varchar(60) not null
);

create table solicitud (
    id         bigserial     primary key,
    tipo       varchar(40)   not null,
    estado     varchar(20)   not null,
    fecha      date          not null,
    monto      numeric(12,2) not null,
    oficina_id bigint        not null references oficina (id)
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
--  Tres oficinas. Dos comparten comuna, para que el filtro por comuna importe.
--  La tercera NO tiene solicitudes: es uno de los bordes.
-- ---------------------------------------------------------------------------
insert into oficina (codigo, nombre, comuna) values
  ('SCL-01', 'Oficina Santiago Centro',   'Santiago'),
  ('SCL-02', 'Oficina Santiago Poniente', 'Santiago'),
  ('VAL-01', 'Oficina Valparaiso',        'Valparaiso');

-- ---------------------------------------------------------------------------
--  Nueve solicitudes repartidas en dos años y en las dos primeras oficinas.
--     PAGADO: 5, y suman 5.670.000
--     La mas reciente de todas es la del 2026-05-05
-- ---------------------------------------------------------------------------
insert into solicitud (tipo, estado, fecha, monto, oficina_id) values
  ('F29', 'PAGADO',    date '2026-01-15', 1200000.00, 1),
  ('F29', 'PAGADO',    date '2026-02-15',  950000.00, 1),
  ('F22', 'PENDIENTE', date '2026-03-10', 3400000.00, 1),
  ('F29', 'PAGADO',    date '2026-04-15',  780000.00, 1),
  ('F29', 'RECHAZADO', date '2025-11-15',  500000.00, 1),
  ('F22', 'PAGADO',    date '2025-06-30', 2100000.00, 2),
  ('F29', 'PAGADO',    date '2026-01-20',  640000.00, 2),
  ('F22', 'PENDIENTE', date '2026-05-05', 1850000.00, 2),
  ('F29', 'RECHAZADO', date '2026-02-01',  300000.00, 2);
