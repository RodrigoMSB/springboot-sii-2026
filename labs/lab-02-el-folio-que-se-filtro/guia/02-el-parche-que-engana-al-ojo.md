# Guía 02 · El parche que engaña al ojo

**Acto 2 · El parche bruto** 🔨

La salida tentadora: esconder el campo. Un `@JsonIgnore` sobre `puntajeRiesgoInterno` en la
entidad.

```java
@JsonIgnore
@Column(name = "puntaje_riesgo_interno", nullable = false)
private int puntajeRiesgoInterno;
```

Hazlo. Arranca, y vuelve a pedir la ficha:

```json
{"rut":"12345678-5","razonSocial":"Comercial Andina SpA","id":2}
```

El puntaje ya no está. **A tu ojo, el JSON parece limpio.** ¿Terminaste?

## No. Contesta primero

| Pregunta | Tu respuesta |
|---|---|
| ¿Sigue saliendo `id`? ¿Debería? | |
| Mañana alguien agrega `saldoDeudaMorosa` a la entidad. ¿Sale por la ficha? | |
| ¿Quién decide, con `@JsonIgnore`, qué es público: el que escribe el DTO, o el que agrega el campo? | |

<details>
<summary>💡 Lista negra vs lista blanca</summary>

`@JsonIgnore` es una **lista negra**: enumeras lo que quieres esconder. Funciona hasta que
alguien agrega un campo nuevo — que nace **expuesto**, porque nadie lo puso en la lista. El
descuido es el estado por defecto.

Y el campo `id` sigue ahí: se te escapó, ¿verdad? Esa es exactamente la trampa.

Lo peor: el `@JsonIgnore` engaña al ojo (el curl parece limpio) pero **no instala ningún
guardián**. Mañana otro controlador devuelve otra entidad, y no hay nada que lo detenga.
</details>

Quita el `@JsonIgnore`. La solución no es esconder campos: es no devolver la entidad.

➡️ Siguiente: [`03-la-lista-blanca-y-los-guardianes.md`](03-la-lista-blanca-y-los-guardianes.md)
