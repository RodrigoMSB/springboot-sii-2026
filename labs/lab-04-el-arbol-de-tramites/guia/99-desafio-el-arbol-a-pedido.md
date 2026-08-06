# Desafío 99 · El árbol a pedido

**Opcional.** Se evalúa aparte. Solo el criterio de aceptación.

1. Con todo en LAZY, hay un endpoint (o un test tuyo) donde SÍ necesitas el contribuyente de
   cada trámite de una lista. Con LAZY puro, eso dispara una consulta por trámite (lo verás
   la próxima semana con nombre y apellido). Investiga `@EntityGraph` y escribe un método de
   repositorio —tuyo, fuera de `enunciado/`— que traiga los trámites de un contribuyente
   **con** el contribuyente ya cargado, en una sola consulta.
2. Documenta en dos frases: ¿por qué esto NO contradice la regla "LAZY siempre"? (Pista: la
   diferencia entre el default de una relación y traer a pedido lo que una consulta concreta
   necesita.)
