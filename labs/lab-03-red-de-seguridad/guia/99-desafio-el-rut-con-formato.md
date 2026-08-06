# Desafío 99 · El RUT con formato

**Opcional.** Se evalúa aparte. Solo el criterio de aceptación.

1. Tu `@RutValido` acepta `12345678-5`. ¿Acepta también `12.345.678-5` (con puntos)? ¿Y
   `12345678-K`? Escribe un test parametrizado —tuyo, fuera de `enunciado/`— que cubra las
   variantes de formato que decidas soportar, y haz que pasen.
2. Un `@RutValido` sobre un campo `null`: ¿debería fallar la validación, o pasarla y dejar
   que `@NotBlank` se encargue del nulo? Investiga la diferencia y decide. Documenta tu
   decisión en dos frases: ¿por qué una anotación de formato no debería también validar
   presencia?
