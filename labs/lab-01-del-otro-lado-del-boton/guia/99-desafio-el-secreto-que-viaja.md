# Desafío 99 · El secreto que viaja

**Opcional.** No baja tu nota si no lo haces, y no la sube si lo haces mal. Se evalúa
aparte.

Solo el criterio de aceptación. Ninguna pista.

---

## El encargo

Carolina te llama: *«Un auditor pregunta si nuestros propios logs filtran secretos. Y si
alguien pusiera la contraseña como argumento de línea de comandos, ¿la veríamos?»*

## Criterio de aceptación

1. Arranca la aplicación pasando la clave como argumento:
   `--spring.datasource.password=loQueSea`. Averigua si aparece en algún sitio observable
   (el log de arranque, `/actuator/env`, la lista de procesos del sistema operativo).
2. Escribe un test —tuyo, en un paquete que no sea `enunciado/`— que falle si un endpoint
   de Actuator expone el valor de una propiedad que contenga `password` en su nombre.
3. Documenta en dos frases: ¿por qué una variable de entorno es más segura que un argumento
   de línea de comandos? ¿Y menos segura que un gestor de secretos?

Tu test vive fuera de `enunciado/`, así que el manifiesto no te lo va a tocar. Es tuyo.
