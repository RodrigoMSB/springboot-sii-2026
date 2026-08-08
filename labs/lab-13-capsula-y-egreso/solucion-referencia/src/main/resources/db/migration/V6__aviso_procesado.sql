-- =============================================================================
--  V6 — El registro de avisos ya procesados (TODO_2)
-- -----------------------------------------------------------------------------
--  La tabla que hace IDEMPOTENTE al consumidor.
--
--  ¿Por qué hace falta? Porque «exactly once» no existe. Ningún sistema de
--  mensajería del mundo lo garantiza, y los que dicen garantizarlo están
--  describiendo otra cosa. Lo que existe es «at least once»: el broker promete
--  que el mensaje llega AL MENOS una vez, y por tanto —a veces— dos.
--
--  Pasa por razones normalísimas, ninguna es un fallo: el consumidor procesa el
--  aviso, se cae antes de confirmar (ack), y el broker —que no tiene forma de
--  saber si alcanzó a trabajar— se lo entrega a otro. Correcto por parte del
--  broker. Duplicado por parte del negocio.
--
--  La respuesta no es pedirle al broker una garantía que no puede dar: es hacer
--  que recibir dos veces dé lo mismo que recibir una. Eso es idempotencia, y ya
--  la implementaste en el Lab 06 (RN-05: reintentar la emisión de un folio
--  devuelve el MISMO folio en vez de crear otro). Mismo principio, otro
--  transporte.
--
--  `clave` es la PRIMARY KEY, y ahí está el mecanismo entero, igual que en el
--  candado del Lab 11: el motor garantiza que solo una fila pueda existir. El
--  consumidor intenta insertar; si la clave ya estaba, el mensaje es un
--  duplicado y se descarta sin volver a trabajar.
-- =============================================================================

CREATE TABLE aviso_procesado (
    -- La clave de idempotencia. Identifica el HECHO de negocio, no la entrega:
    -- dos entregas del mismo aviso comparten clave; dos avisos distintos, no.
    clave        VARCHAR(120) PRIMARY KEY,
    procesado_en TIMESTAMP    NOT NULL
);
