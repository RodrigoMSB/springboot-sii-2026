# Fixtures negativos

Cada clase de este paquete **viola a propósito** exactamente una regla AU. Existen para
que `MordidaDeLosGuardianesTest` demuestre que cada guardián muerde.

*Un guardián sin prueba de que muerde es un adorno.*

Viven solo en el classpath de test. La suite de producción importa con
`DO_NOT_INCLUDE_TESTS`, así que estas clases jamás contaminan la vigilancia del `main`.
