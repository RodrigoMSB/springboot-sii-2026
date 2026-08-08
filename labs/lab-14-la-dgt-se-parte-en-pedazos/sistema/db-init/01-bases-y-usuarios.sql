-- =============================================================================
--  01-bases-y-usuarios.sql — una base por servicio, y una frontera de verdad
-- -----------------------------------------------------------------------------
--  Este script lo ejecuta la imagen de PostgreSQL una sola vez, al crear el
--  volumen. Crea DOS bases y DOS usuarios, y ahí está la lección:
--
--    · dgt_contribuyentes  -> solo el usuario `svc_contribuyentes` entra
--    · dgt_tramites        -> solo el usuario `svc_tramites` entra
--
--  `svc_tramites` NO PUEDE leer la tabla de contribuyentes. No es una convención
--  del equipo ni un comentario en el README: es un GRANT que no existe. Si
--  alguien intenta el JOIN «rápido» para ahorrarse la llamada HTTP, el motor le
--  dice que no.
--
--  Esa frontera es lo que separa un microservicio de un módulo. Sin ella
--  aparece el `monolito distribuido`: dos procesos, dos despliegues, dos
--  equipos… y una base de datos compartida que los ata a los dos. Todos los
--  costos de partir el sistema, ninguna de sus ventajas.
--
--  ⚠️  Las claves de aquí son de LABORATORIO, para una base desechable que vive
--  en el portátil del alumno y muere con `docker compose down -v`. Versionarlas
--  es correcto y hace el entorno reproducible — es la misma nota que llevan los
--  compose de los trece labs anteriores. Lo que jamás va versionado es una
--  credencial de PRODUCCIÓN, y por eso los `config-repo/*.yml` leen las suyas de
--  variables de entorno y no traen ni un valor por defecto.
-- =============================================================================

CREATE USER svc_contribuyentes WITH PASSWORD 'contribuyentes-dev';
CREATE USER svc_tramites       WITH PASSWORD 'tramites-dev';

CREATE DATABASE dgt_contribuyentes OWNER svc_contribuyentes;
CREATE DATABASE dgt_tramites       OWNER svc_tramites;

-- Y la parte que hace real la frontera: revocar el acceso por defecto que
-- PostgreSQL concede a PUBLIC sobre una base recién creada.
REVOKE CONNECT ON DATABASE dgt_contribuyentes FROM PUBLIC;
REVOKE CONNECT ON DATABASE dgt_tramites       FROM PUBLIC;

GRANT CONNECT ON DATABASE dgt_contribuyentes TO svc_contribuyentes;
GRANT CONNECT ON DATABASE dgt_tramites       TO svc_tramites;
