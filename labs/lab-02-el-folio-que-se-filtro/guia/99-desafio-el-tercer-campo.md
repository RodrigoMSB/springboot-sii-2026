# Desafío 99 · El tercer campo

**Opcional.** Se evalúa aparte; no sube ni baja tu nota del núcleo.

Solo el criterio de aceptación. Sin pistas.

1. Agrega a la entidad `Contribuyente` un campo nuevo y sensible (por ejemplo,
   `correoElectronico`). Sin tocar el DTO ni la ficha.
2. Demuestra —con un test tuyo, fuera de `enunciado/`— que ese campo **no** sale por
   `GET /api/v1/contribuyentes/{rut}/ficha`. Es decir: que la lista blanca lo dejó fuera
   sola, sin que hicieras nada.
3. Ahora hazlo salir a propósito (agrégalo al DTO) y observa qué cambió en el diff y en
   Swagger. En dos frases: ¿por qué una lista blanca hace este cambio *visible* y una lista
   negra lo haría *invisible*?
