-- =============================================================================
--  V5 — El candado distribuido (TODO_2)
-- -----------------------------------------------------------------------------
--  El candado NO puede vivir en la aplicación. Un `synchronized`, un
--  `ReentrantLock` o una bandera en `application.yml` solo saben de SU JVM: dos
--  instancias tienen dos candados distintos, y dos candados distintos no son un
--  candado. Es la misma lección del Lab 06, y por eso la respuesta es la misma:
--  el candado vive en el DATO, donde todas las instancias lo ven.
--
--  `nombre` es la PRIMARY KEY, y ahí está el mecanismo entero: PostgreSQL
--  garantiza que solo UNA fila pueda existir con ese nombre. Dos instancias
--  haciendo INSERT a la vez no empatan — una gana y la otra recibe una
--  violación de clave. No hay ventana entre "miro si está libre" y "lo tomo",
--  porque son la MISMA operación atómica.
--
--  `expira_en` es el seguro contra el desastre silencioso: si la instancia que
--  tomó el candado muere a mitad del cierre (se cae el pod, alguien reinicia,
--  se corta la luz), el candado queda tomado por un muerto. Sin expiración, el
--  cierre no vuelve a correr JAMÁS y nadie se entera hasta que Carolina
--  pregunta por qué el resumen del martes no llegó. Con expiración, el sistema
--  se recupera solo.
--
--  Es un compromiso, y hay que declararlo: si el cierre tarda MÁS que la
--  expiración, dos instancias podrían ejecutarlo a la vez. El TTL se elige
--  generoso respecto de la duración real del trabajo, y esa elección se
--  documenta. No hay candado distribuido sin esta decisión.
-- =============================================================================

CREATE TABLE candado_tarea (
    -- El nombre de la tarea. Que sea la PK es lo que hace atómica la toma.
    nombre      VARCHAR(80) PRIMARY KEY,
    -- Quién lo tiene. Para el log, y para que el operador sepa a quién mirar.
    tomado_por  VARCHAR(80) NOT NULL,
    -- Hasta cuándo vale. Pasada esta hora, cualquiera puede arrebatarlo.
    expira_en   TIMESTAMP   NOT NULL
);
