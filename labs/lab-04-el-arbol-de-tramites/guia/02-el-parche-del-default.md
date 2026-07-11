# Guía 02 · El parche del default

**Acto 2 · El parche bruto** 🔨

La salida tentadora: *"si EAGER carga de más, quito el `fetch` y que decida el framework"*.

Prueba, en una entidad, quitar `fetch = FetchType.EAGER` del `@ManyToOne`, dejándolo pelado:

```java
@ManyToOne(optional = false)   // sin fetch: "que decida JPA"
```

Compila. Corre. Y el muro… ¿desaparece? Depende de la anotación. Mira la tabla del §3 de la
teoría:

| Anotación | Default sin `fetch` |
|---|---|
| `@ManyToOne` | **EAGER** |
| `@OneToMany` | LAZY |

## Contesta

| Pregunta | Tu respuesta |
|---|---|
| Sin declarar `fetch`, ¿tu `@ManyToOne contribuyente` quedó EAGER o LAZY? | |
| ¿Y un `@OneToMany`? | |
| Entonces, "no declarar", ¿es neutral, o es elegir sin saberlo? | |

<details>
<summary>💡 No declarar es declarar</summary>

Quitar el `fetch` no apaga el muro: para los `@ManyToOne` (que son la mayoría de las
relaciones) el default es EAGER, así que el muro sigue, ahora **invisible**. Y para los
`@OneToMany` es LAZY. Dos comportamientos opuestos, decididos por una tabla que quien lee tu
código tendría que recordar de memoria.

No declarar no es neutralidad. Es delegar la decisión — y perder la evidencia de qué
decidiste.
</details>

Deshaz el cambio. La solución no es quitar el fetch: es declararlo, LAZY, siempre.

➡️ Siguiente: [`03-lazy-y-el-guardian.md`](03-lazy-y-el-guardian.md)
