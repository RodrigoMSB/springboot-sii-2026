# Desafío 99 · El detalle pesado

**Opcional.** Se evalúa aparte. Solo el criterio.

1. La proyección es perfecta para el LISTADO. Pero hay un endpoint de DETALLE donde sí
   necesitas el árbol de un trámite (contribuyente + F29 + líneas). Con LAZY puro, eso es un
   N+1 de otra forma. Escribe —fuera de `enunciado/`— un método de repositorio con
   `@EntityGraph` (o `JOIN FETCH`) que traiga un trámite CON su contribuyente y su F29 en una
   consulta, y un test que lo mida con el contador (`≤ 2`).
2. En dos frases: ¿por qué para el listado elegiste proyección y para el detalle
   `@EntityGraph`? ¿Qué compra cada uno?
