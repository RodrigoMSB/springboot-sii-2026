# Desafío · El secreto que viaja

Solo el criterio de aceptación. Sin pistas: eso es el desafío.

1. Arranca la app pasando la clave como argumento de línea de comandos
   (`--spring.datasource.password=loQueSea`) y averigua si aparece en algún sitio
   observable: el log, `/actuator/env`, o la lista de procesos del sistema.
2. Escribe un test **tuyo** —fuera de `enunciado/`, para que el manifiesto no lo toque— que
   falle si Actuator expone el valor de cualquier propiedad cuyo nombre contenga `password`.
3. En dos frases: ¿por qué una variable de entorno es más segura que un argumento de CLI, y
   menos segura que un gestor de secretos?

**No baja tu nota si no lo haces.** Si lo haces y queda a medias, tampoco. Se evalúa aparte.
