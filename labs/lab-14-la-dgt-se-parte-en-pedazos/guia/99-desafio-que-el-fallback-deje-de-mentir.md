# Desafío · Que el fallback deje de mentir

*Opcional. No baja el veredicto si no lo haces; tampoco lo sube si lo haces mal.*

---

## El criterio de aceptación

> **Un cliente del portal tiene que poder distinguir, sin adivinar, una respuesta completa
> de una degradada.**

Eso es todo. No hay pista 2 inline, no hay andamio, no hay firma que respetar. El *cómo* es
tuyo.

---

## Por qué esto es un desafío y no un ejercicio

Porque no tiene una respuesta correcta, tiene **decisiones defendibles**, y la parte difícil
no es escribir el código: es elegir.

Hoy el sistema devuelve esto con `dgt-contribuyentes` caído:

```json
{"id":1,"tipo":"DECLARACION_F29","estado":"EN_PROCESO",
 "rutContribuyente":"11111111-1","nombreContribuyente":null,"atendidoPor":null}
```

HTTP 200. JSON válido. Y un contribuyente a punto de firmar una declaración a la que le
falta el nombre del titular.

Algunas salidas, cada una con su precio:

| Camino | A favor | En contra |
|---|---|---|
| Un campo `completo: false` en el cuerpo | barato, explícito, no rompe a nadie | quien no lo lea sigue igual de engañado |
| Cabecera `X-Datos-Degradados: contribuyente` | no toca el contrato del cuerpo | las cabeceras se ignoran todavía más |
| HTTP **206 Partial Content** | está en el estándar y significa justo eso | poco usado; muchos clientes lo tratan como 200 |
| HTTP **503** y que no haya respuesta | honesto del todo | tiras a la basura los datos que **sí** tenías |
| Último valor conocido, desde caché | el usuario ve algo cierto | ¿cuánto de viejo es demasiado viejo? |

**Ninguna es gratis.** Elige una, impleméntala, y en el reporte escribe qué perdiste al
elegirla. Esa segunda parte es la que se evalúa.

---

## Dónde tocar

- `ConsultaDeContribuyentes.fichaDegradada()` — quien decide qué devolver
- `TramiteDto` — lo que sale por la API
- `TramiteController.componer()` — donde se juntan lo local y lo remoto

---

## Si te sobra tiempo: el problema de verdad

El del §5 del bloque 3, que es más difícil y más valioso:

> El circuit breaker es **por servicio**. Cuando abre porque una instancia murió, deja de
> llamar también a las instancias sanas.

¿Cómo harías que el fallo de una instancia no castigue al servicio entero?

No lo implementes a ciegas: **escribe primero qué observarías** para saber si tu solución
funciona. Si no sabes decir qué medirías, todavía no sabes qué estás arreglando — y esa
frase resume mejor que ninguna otra las catorce sesiones de este curso.
