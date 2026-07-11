# Desafío 99 · Rate limiting del login (opcional)

> Opcional (P-15). Si no lo haces, no pierdes nada del lab. Si lo haces, márcalo con honestidad.

## El reto

El endpoint de login es público (tiene que serlo). Eso lo hace blanco de fuerza bruta: miles
de intentos por segundo probando claves. Ponle un **límite de tasa**: por ejemplo, N intentos
por minuto por IP; si se pasa, **429 Too Many Requests**.

Ideas de implementación (elige y justifica):
1. Un filtro con un contador por IP (en memoria: simple, pero no sobrevive a dos instancias).
2. Un token bucket (biblioteca tipo Bucket4j).
3. Delegarlo al gateway/reverse proxy (a veces la respuesta correcta es "no en la app").

## Las preguntas

1. ¿Por qué el login, y no todos los endpoints?
2. Tu contador en memoria: ¿qué pasa con dos instancias detrás de un balanceador? (rima con
   el `synchronized` del Lab 06)
3. ¿429 o 503? ¿Cuál es la diferencia semántica, y cuál corresponde aquí?

No hay validador para esto. Resume tu conclusión en el reporte.
