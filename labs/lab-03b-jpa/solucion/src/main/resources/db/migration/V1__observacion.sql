-- =============================================================================
--  V1 — La tabla del laboratorio
-- -----------------------------------------------------------------------------
--  Una sola tabla, sin relaciones con nadie. Las relaciones entre entidades son
--  el Lab 04; hoy el tema es más básico: cómo una fila y un objeto Java pasan a
--  ser la misma cosa.
--
--  La crea Flyway al arrancar. Tú no la tocas: lo que escribes es la clase Java
--  que se corresponde con ella.
--
--  EL NOMBRE DEL ARCHIVO NO ES DECORATIVO. Flyway lo lee: `V` de versionada, `1`
--  el número de orden, `__` doble guion bajo obligatorio, y el resto es
--  descripción. Con ese número lleva la cuenta en una tabla suya
--  (`flyway_schema_history`) de qué migraciones ya aplicó, así que la segunda
--  vez que arranques NO intentará crear la tabla otra vez.
--
--  Y una regla que vale para siempre: una migración ya aplicada NO SE EDITA.
--  Flyway guarda su huella y se negaría a arrancar si cambia. Lo que se hace es
--  añadir una V2 — el Lab 06 lo hace, para meter una restricción.
-- =============================================================================

CREATE TABLE observacion (
    -- BIGSERIAL: entero largo con su propio contador. Es lo que hace que el
    -- INSERT no tenga que mencionar el id, y se corresponde con el
    -- @GeneratedValue(IDENTITY) de la entidad.
    id     BIGSERIAL    PRIMARY KEY,

    -- Los tres NOT NULL se corresponden con los `nullable = false` de la clase.
    -- Si una de las dos partes cambiara sin la otra, `ddl-auto: validate` lo
    -- detendría al arrancar en vez de dejar que fallara más tarde.
    texto  VARCHAR(500) NOT NULL,
    autor  VARCHAR(100) NOT NULL,

    -- DATE es solo fecha, sin hora: se corresponde con el LocalDate de Java.
    fecha  DATE         NOT NULL
);

-- Sin INSERT de datos: la tabla nace VACÍA a propósito. Los datos los pone la
-- demo 1 desde Java, que es donde se ve el save() haciendo su trabajo.
