# Guía 01 · La bandeja en rojo

**Acto 1 · El choque** 💥

Corre el validador sobre el starter:

```bash
./bin/90-validar.sh --dir starter
```

La pantalla se llena de rojo. `Tests run: 20, Failures: 3, Errors: 13`. El `90` te lista los
compromisos, en español:

```
· un tipo en blanco se rechaza con 400 y nombra el campo 'tipo'
· un RUT con dígito verificador falso se rechaza
· una transición ilegal responde 409 con tipo, origen y destino
...
```

No entres en pánico. **No está roto.** Cada línea roja es un compromiso que el equipo de QA
acordó con la DGT, y que el código aún no cumple. Tu trabajo es cumplirlos.

| Pregunta | Tu respuesta |
|---|---|
| ¿Cuántos compromisos hay en total? ¿Cuántos en rojo? | |
| ¿Cuáles reconoces sin abrir el código? | |
| ¿Por dónde empezarías, y por qué? | |

➡️ Antes de escribir una línea: [`02-leer-un-test-como-contrato.md`](02-leer-un-test-como-contrato.md)
