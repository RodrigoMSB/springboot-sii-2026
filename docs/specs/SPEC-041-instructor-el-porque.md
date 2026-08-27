# SPEC-041 · La documentación de `instructor/` que explica el porqué — labs 04 a 07

**Emite:** el PO · **Ejecuta:** mocito · **Fecha:** 27 de agosto de 2026

---

## 1 · El problema, medido en clase

El PO dictó el lab 04 con `instructor/` al lado y **le faltó lo que necesitaba**. La
documentación explica **qué hace** cada anotación, pero no **por qué esa y no otra** — que es
exactamente lo que un alumno pregunta y lo que el instructor tiene que responder sin pensar.

El caso concreto que lo destapó:

```java
@GeneratedValue(strategy = GenerationType.IDENTITY)
```

La documentación dice que genera el id. **No dice** qué hace `IDENTITY` por debajo, ni que
existen `AUTO`, `SEQUENCE` y `TABLE`, ni por qué aquí se eligió esta, ni en qué caso elegirías
otra. Con eso, ante la pregunta «¿y por qué IDENTITY?», el instructor no tiene respuesta a mano.

## 2 · El criterio nuevo para `instructor/`

Cada **decisión técnica** del código —cada anotación con opciones, cada parámetro que pudo ser
otro, cada interfaz que se eligió entre varias— documenta **cuatro cosas**:

1. **qué hace exactamente**
2. **qué alternativas existen**
3. **por qué se eligió esta aquí**
4. **en qué caso elegirías otra**

En ese orden y sin adornos. **Donde algo no tenga alternativa real, se dice y se pasa.**

## 3 · Alcance

- **Labs 04, 05, 06 y 07.**
- **Solo la carpeta `instructor/`.**
- **No se toca** `practica/`, `solucion/`, `PASOS.md` ni el código.

## 4 · Casos que ya sabemos que faltan

**No es lista cerrada: hay que barrer los cuatro labs.**

- `GenerationType` y sus cuatro estrategias
- `fetch = LAZY`, y por qué la especificación pone `EAGER` de default en `@ManyToOne`
- `@Transactional(readOnly = true)` y qué gana frente a `@Transactional` a secas
- `JpaRepository` frente a `CrudRepository` y `PagingAndSortingRepository`
- `mappedBy` y qué pasa si se pone en el lado equivocado
- `nullable` / `length` en `@Column` y qué valida realmente
- el `protected` del constructor sin argumentos
- `@JoinColumn` y qué ocurre si se omite

## 5 · Entrega

Trabajo de corrido, sin interrumpir al PO. Commit, PR, merge y tag por el ejecutor. Informe al
terminar **con la lista de decisiones documentadas por lab**.
