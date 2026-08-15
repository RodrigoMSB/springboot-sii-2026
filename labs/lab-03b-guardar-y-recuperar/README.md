# Lab 3.5 · Guardar y recuperar

**Tu primera vez con JPA.** Llevas usando entidades desde el Lab 01 sin que nadie te haya
dicho qué son ni cómo funcionan. Hoy lo construyes tú: vas a tomar una tabla vacía y hacer
que un objeto Java se guarde solo en la base y vuelva solo cuando lo pidas — sin escribir
una línea de SQL.

---

## De qué se trata

La DGT necesita guardar **observaciones internas**: notas que un funcionario deja sobre un
contribuyente. La tabla ya existe en la base (`observacion_interna`). Lo que no existe es el
código Java que la conecte con tu aplicación. Eso lo escribes tú hoy.

Al terminar vas a poder:

- Tomar una fila de una tabla y tratarla como un objeto Java (y al revés).
- Guardar un objeto sin escribir un `INSERT`.
- Buscarlo sin escribir un `SELECT`.
- Ver el SQL que Hibernate escribió por ti, y entender qué hizo.

Eso es JPA. Y es lo que sostiene todo lo que viene después en el curso.

---

## Lo primero: míralo funcionar

Antes de teclear nada, levanta la aplicación y guarda tu primera observación con JPA ya
resuelto, para ver a dónde vas a llegar:

```bash
./bin/start-lab.sh --dir solucion       # la versión terminada, para mirar
./bin/91-demo-jpa.sh                     # guarda una observación y la recupera
```

Vas a ver un objeto Java entrar a la base y volver, y el SQL exacto que Hibernate generó.
Ese es el destino. Ahora vamos a construirlo desde cero en `starter/`.

```bash
./bin/99-destruir.sh
./bin/start-lab.sh                       # ahora sí, el starter — el que vas a completar
```

---

## La sesión

| | |
|---|---|
| 1 · El mapa | El instructor explica la idea: tu clase ES la tabla. 15 min. |
| 2 · Los tres TODOs | ~20 min cada uno. Todo lo que necesitas está junto al código. |
| 3 · Ver el SQL | Arrancas con `--ver-sql` y lees lo que Hibernate escribió por ti. |
| 4 · El cierre | `./bin/90-validar.sh` y la demo, ahora corriendo sobre tu código. |

---

## Los tres TODOs

Cada uno está explicado **en el archivo donde se hace**: qué escribir, la pista que te
ahorra media hora, y qué test lo verifica. No hay que cambiar de ventana.

| | Qué | Dónde | Lo verifica |
|---|---|---|---|
| **TODO_1** | Mapear la tabla a una entidad | `domain/entity/ObservacionInterna.java` | `E1_EntidadMapeadaIT` |
| **TODO_2** | El repositorio: guardar y buscar | `infrastructure/repository/ObservacionInternaRepository.java` | `E2_GuardarYRecuperarIT` |
| **TODO_3** | Conectar el servicio y ver el SQL | `application/ObservacionInternaService.java` | `E3_ServicioConectadoIT` |

Los tests del enunciado viven en `src/test/java/cl/dgt/tramites/enunciado/`. **Léelos**: son
el contrato de la sesión, y cada `@DisplayName` es un compromiso en español.

---

## Ver el SQL que escribiste sin escribirlo

En el TODO_3, arranca así:

```bash
./bin/start-lab.sh --ver-sql
```

Guarda una observación y pídela de vuelta. Mira el log (`.estado/dgt.log`): ahí están el
`INSERT` y el `SELECT` que Hibernate generó a partir de tu entidad y tu repositorio. Tú
escribiste cero líneas de SQL, y ahí está el SQL — correcto, con su `JOIN`, con sus tipos.

**Ese es el trato de JPA:** tú declaras la correspondencia entre tu clase y la tabla una
sola vez, y Hibernate escribe el SQL de cada operación por ti, para siempre.

---

## Comandos

```bash
./bin/start-lab.sh                       # levanta starter/ (--dir solucion para la terminada)
./bin/start-lab.sh --ver-sql             # …y muestra el SQL que Hibernate genera
./bin/91-demo-jpa.sh                     # guarda y recupera una observación, en vivo
./bin/90-validar.sh                      # ¿está tu trabajo listo?
./bin/99-destruir.sh                     # deja la máquina como estaba
./bin/95-recuperar.sh --solo-enunciado   # restaura los tests del enunciado si los tocaste
```

Desde `starter/` o `solucion/`, la suite completa:

```bash
./mvnw verify
```

---

## Después de la sesión

`PARA-EL-SABADO.md` — la profundización: cómo Hibernate traduce entre objetos y filas, el
ciclo de vida de una entidad, qué es una consulta derivada y cuándo se queda corta, y una
mirada de una página a cómo era esto **antes** de JPA (spoiler: cuarenta líneas para leer
una tabla) — para que valores lo que te ahorra.

Y lo que JPA **no** resuelve solo: si pedir una observación te trae también al contribuyente,
y a sus trámites, y a los folios de cada trámite… eso tiene un costo. El Lab 04 lo mide.

Si algo falla: `../lab-00-estacion-base/docs/troubleshooting.md` tiene una tabla con números.
Cita el número.
