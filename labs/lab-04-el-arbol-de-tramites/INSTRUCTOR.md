# Guía del instructor · Lab 04

## Antes de la sesión

1. `./bin/91-e2e.sh` — starter en rojo, solución en verde. Si falla, no des la clase.
2. **Docker corriendo.** Este lab lo necesita.
3. Ensaya el muro: ten el `grep` del log listo (abajo).

## ORDEN PARA CLASE (180 min)

### 🔪 00:00 – 00:10 · La escena del crimen (en vivo)

Proyecta la terminal. Levanta el starter, pide un trámite, y muestra el SQL:

```bash
./bin/start-lab.sh --dir starter
curl http://localhost:8099/api/v1/tramites/1
grep -A20 '    select' .estado/dgt.log | head -30
```

Que la sala vea el `join contribuyente ... left join formulario29 ... left join folio`. Y que
vea que se dispararon **varios** `select`. Pausa.

> *"Funciona, ¿no? Nadie se ha quejado. Guárdense esa cara de 'y qué importa' — la vamos a
> necesitar la próxima semana."*

Abre `Tramite.java`, muestra el comentario del practicante (*"EAGER en todo, así no sale el
LazyInitializationException"*). Y Carolina:

> *"Hoy no vinimos a apagar un incendio. Vinimos a entender qué compramos cada vez que
> escribimos una anotación de fetch. El que no declara el fetch, lo está declarando igual —
> solo que no sabe cuál."*

### 📚 00:10 – 00:50 · Teoría (M5 parte 1)

`TEORIA.md`. Imprescindibles:
- **§3, la tabla de defaults.** Proyéctala. Es el acto 2: `@ManyToOne` EAGER, `@OneToMany`
  LAZY. "No declarar" no es neutral.
- **§10, el caveat del `@OneToOne` inverso.** Sé honesto: LAZY no siempre es LAZY. El curso no
  vende magia.

### ☕ 00:50 – 01:00 · Descanso

### 🔧 01:00 – 02:50 · Laboratorio

**El error que cometerá la sala:** instalarán AU-04 y olvidarán corregir una relación a LAZY.
`E5` los caza a ELLOS (`L4-02`). Se frustran: *"¡mi regla está bien!"*. Y lo está — falta que
su propia entidad la cumpla. No des la respuesta: *«¿qué campo nombra el error?»*.

**Segundo:** intentarán `JOIN FETCH` en TODO_3 (lo habrán googleado). `E3` lo rechaza. Es
deliberado: esa herramienta es del Lab 05, y se aprende cuando duele, no antes.

### ✅ 02:50 – 03:00 · Cierre — LA SIEMBRA

Esta es la siembra más importante del curso hasta ahora. Léela casi textual (`TEORIA §11`):

> *"Hicieron lo correcto: LAZY explícito. La próxima semana, ese mismo LAZY, iterado por un
> listado inocente sobre cincuenta mil lotes, va a costar once segundos y mil ochocientas
> consultas. Lo correcto también se mide. La ingeniería no es elegir el default bueno — es
> saber qué compras con cada decisión. El módulo se llama 'Once segundos'. Traigan
> cronómetro."*

No la apures. Es el gancho que sostiene la sesión siguiente.

## Qué revisar en los reportes

1. **§1, la transcripción del muro.** ¿Pegó el SELECT con sus JOINs? ¿Contó las tablas?
2. **§2, el default.** ¿Entendió que quitar el fetch no es neutral?
3. **§7, la siembra.** ¿Anticipa el costo del LAZY iterado? Si lo hace, ya piensa en
   trade-offs, no en reglas.
4. **§6, honestidad.** Nunca penalices un "sí".
