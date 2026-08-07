# 99 · Desafío (opcional) — la bandeja de los muertos

> Lo opcional **nunca** baja el veredicto. Si no lo haces, el `90` no se entera.

La DLQ del TODO_3 guarda lo que nadie pudo procesar. Bien. Ahora responde honestamente:

**¿Quién la mira?**

Nadie. Una DLQ que nadie abre es un basurero con mejor nombre — y es peor que no tenerla, porque da
la sensación de que el problema está resuelto. Los mensajes se acumulan ahí durante meses hasta que
alguien pregunta por un aviso de marzo.

## El criterio de aceptación

Haz que la DLQ deje de ser un agujero silencioso. Se aprueba si:

1. Se puede **saber cuántos** mensajes muertos hay **sin entrar a la consola del broker**.
2. Un mensaje que cae a la DLQ **se nota** — que quede algo que un operador pueda ver o que una
   alerta pueda disparar.
3. Se puede **reprocesar** un mensaje de la DLQ una vez corregida su causa, sin perderlo si el
   reproceso también falla.

Sin pistas de implementación: esa es la gracia del `99`.

## Tres trampas, que no son pistas

- **Reprocesar en bucle.** Si devuelves el mensaje a la cola principal y su causa sigue ahí, vuelve
  a la DLQ. Y otra vez. Has construido una noria.
- **El contador que miente.** Si cuentas los muertos al arrancar y guardas el número, tendrás el de
  entonces. Piensa cuándo se mide.
- **Los duplicados.** Un mensaje reprocesado es, por definición, una segunda entrega. Si tu TODO_2
  está bien, esto ya está resuelto — compruébalo en vez de suponerlo.

## Para pensar (va en el reporte, aunque no lo implementes)

Dos preguntas, y la segunda es la difícil:

1. ¿En qué se parece esto a lo que hiciste en el Lab 10? (pista: no es el código)
2. Un mensaje lleva tres días en la DLQ. ¿Cuánto vale ese aviso todavía? ¿Hay avisos que **caducan**,
   y qué haces con ellos?

La segunda no tiene respuesta técnica. Tiene respuesta de negocio, y es justo el tipo de pregunta
que te va a esperar la semana que viene.
