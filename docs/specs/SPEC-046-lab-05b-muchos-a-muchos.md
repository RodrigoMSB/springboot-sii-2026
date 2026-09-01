# SPEC-046 · Lab 05b · Muchos a muchos

**Emite:** el PO · **Ejecuta:** el mocito
**Fecha:** 31 de agosto de 2026
**Rama:** `spec-046-lab-05b-muchos-a-muchos` desde `main` · PR contra `main`
**Prefijo de commits:** `SPEC-046: <qué>`
**Autorización:** commit, PR, merge y tag sin firma del PO. Trabaja de corrido, sin interrumpirlo.

---

## 0 · De dónde sale esto

Un alumno lo pidió en clase, dictando el lab 05. Es el hueco natural del arco: se enseñó
`@ManyToOne` y `@OneToMany`, y falta la tercera forma de relación.

---

## 1 · El dominio

**Trámite ↔ documento.** Un trámite necesita varios documentos (cédula, escritura, poder) y el
mismo tipo de documento sirve para varios trámites.

Continúa el mundo del lab 05 sin inventar dominio nuevo. **Dos entidades, ninguna más.**

---

## 2 · La forma

Todo igual que el resto de labs, **sin excepción**:

- directorio `labs/lab-05b-muchos-a-muchos`
- las tres carpetas: `practica/` sin documentación, `solucion/` con poca, `instructor/` con todo
  y en el `.gitignore`
- `README.md`
- `PASOS.md` con los bloques «se pega» **extraídos de `solucion/`**
- la guía en PDF en la carpeta del lab
- la maleta
- las guardas de puerto y candado
- el estilo de las demos, con su `seccion(...)` e `informe(...)`
- **puertos libres que no choquen con ningún lab**

**Tope de tres horas.**

---

## 3 · Los seis pasos

1. **La relación con `@JoinTable`** — y mirar la tabla intermedia recién creada.
2. **Agregar y quitar documentos de un trámite**, viendo los INSERT y DELETE que salen en la
   intermedia.
3. **El lado espejo con `mappedBy`** — que no guarda nada, igual que el `@OneToMany` del 05.
4. **Set contra List — el momento del lab.** Con `List` en un `@ManyToMany` bidireccional,
   Hibernate borra toda la relación del trámite y la reinserta entera en cada cambio. **Mídelo y
   cítalo: cuántas sentencias con `List` y cuántas con `Set` para el mismo cambio.** Si al medirlo
   el comportamiento no es el que digo, **manda lo que midas y dilo en el informe**.
5. **Una consulta que navega la relación.**
6. **Cuándo `@ManyToMany` deja de servir:** en cuanto la tabla intermedia necesita un dato propio
   —la fecha en que se adjuntó el documento, quién lo subió—, hay que convertirla en una entidad
   con dos `@ManyToOne`. **Se explica y se muestra en qué se convierte, sin implementarlo.**

---

## 4 · Lo que el lab debe dejar dicho en su cierre

Dos cosas:

1. Que **el N+1 del lab 06 aplica igual aquí y peor**.
2. Que **`@ManyToMany` es LAZY por defecto pero se declara igual**.

---

## 5 · Numeración

El lab va **entre el 05 y el 06** y **no se renumera nada**.

Si el orden alfabético de `labs/` deja el `05b` en mal sitio, **dilo en el informe y no lo
arregles por tu cuenta.**

---

## 6 · Verificación

Como siempre, **incluida la V1 de pegar los bloques sobre `practica/` limpia**.

---

## 7 · Cierre

Commit, PR, merge y tag. `main` al terminar.
