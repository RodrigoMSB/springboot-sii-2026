# SPEC-006 · Lab 00 «Estación Base», la caja de herramientas y el ESTADO.md

| Campo | Valor |
|---|---|
| ID | SPEC-006 |
| Título | Primer material visible: Lab 00, `lib-comunes.sh`, `ESTADO.md`, guardián de la semilla |
| Autor | Arquitecto |
| Aprueba | PO (Rodrigo) — **APROBADA** (capa humana presentada y aceptada) |
| Depende de | SPEC-005, SPEC-FIX-01 |
| Estado | LISTA PARA EJECUCIÓN |

> **Instrucción de ejecución (mocito):** primer paso, guardar este archivo íntegro en
> `docs/specs/SPEC-006-lab00-estacion-base.md` y commitearlo en rama antes de ejecutar.
>
> **Protocolo de verificación en dos etapas (nuevo, permanente):** todo script y todo
> flujo de esta SPEC lo ejecutas **tú primero**, sobre estado limpio, con salida citada
> en el reporte. Solo cuando tu evidencia está en verde, el PO ejecuta la "Prueba del
> PO" (§7) como aceptación final. El PO **jamás** es el primero en correr algo. Esta
> regla se agrega al "Protocolo SPEC" del README como norma permanente.

---

## §1 · Objetivo

Que exista **la primera carpeta que un alumno (y el PO) puede abrir, leer y correr**:
`labs/lab-00-estacion-base/`. Su misión: que ninguno de los 18 alumnos llegue a la
sesión 1 con la máquina a medio configurar. Junto con ella nacen las tres piezas que
todos los labs futuros comparten: la caja de herramientas de scripts, el `ESTADO.md`
para humanos, y el guardián que le faltaba a la semilla.

## §2 · `ESTADO.md` (raíz del repo)

Una página, **en cristiano, cero jerga**, con cuatro secciones fijas:

1. **Qué existe hoy** (con rutas: "el temario está en…", "la app corre con…").
2. **Qué falta** (la lista de labs pendientes, sin detalle técnico).
3. **Qué viene ahora** (la SPEC en curso, en una frase).
4. **Si estás perdido** (los 3 comandos para ver algo funcionando).

**Obligación permanente** (se agrega al Protocolo SPEC del README): toda SPEC, al
cerrar, actualiza el `ESTADO.md`. Un `ESTADO.md` desactualizado es un bug del material.

## §3 · La caja de herramientas: `labs/lib/lib-comunes.sh`

Se escribe **una vez, bien**, y los 12 labs la consumen (`source ../lib/lib-comunes.sh`
o ruta relativa robusta). Contiene:

- `paso_ok / paso_fail / paso_skip / paso_warn`: contadores dinámicos (`N/N` calculado,
  jamás escrito a mano), prefijos `[OK]/[ERROR]/[SKIP]/[WARN]/[INFO]`, **sin ANSI**.
- `resumen_final`: imprime `X/Y verificaciones` y fija el exit code (0 solo si
  `fallos == 0`).
- `requiere_comando <cmd> <mensaje-humano>`: verifica existencia con mensaje accionable.
- Detección de plataforma (macOS / Linux / Git Bash) para los mensajes.

**Restricciones vinculantes:** bash **3.2 compatible** (macOS de fábrica; nada de
`mapfile`, arrays asociativos, `${var,,}`); sin `while` tras tubería para acumular
contadores (D3 de la SPEC-004: el subshell se traga las variables); sin Python; sin
color. El `90` de cada lab será de solo lectura y sin `set -e` — la lib debe soportar
ese modo acumulando fallas.

## §4 · `labs/lab-00-estacion-base/`

Estructura (instancia mínima de la anatomía de SPEC-000 §7.6 — el Lab 00 no tiene
crimen ni TODOs: es pre-curso, no computa horas):

```
labs/lab-00-estacion-base/
├── README.md                  # la bienvenida de Carolina + el mapa del pre-vuelo
├── guia/
│   ├── 01-instala-tu-estacion.md    # Java 25 (Temurin), Docker/Podman, Git, IDE — por SO
│   └── 02-conoce-el-terreno.md      # el repo, la app, cómo pedir ayuda
├── docs/
│   ├── entorno-alumno.md            # correlato humano del CI (P-17), incluye --sin-docker
│   └── troubleshooting.md           # tabla numerada T-01, T-02… (filas citables)
└── bin/
    ├── 00-verificar.sh              # ¿está lista tu máquina?
    ├── start-lab.sh                 # levanta la app de la DGT
    └── 99-destruir.sh               # deja todo como estaba (compose down -v, procesos)
```

### 4.1 `00-verificar.sh`

Verifica y reporta con mensaje **accionable** (qué falta y dónde conseguirlo):
Java 25 en el PATH (`java -version` parseado con tolerancia), Docker/Podman **daemon
corriendo** (no solo instalado), Git, conectividad a Maven Central y Docker Hub
(timeout corto, mensaje de proxy si falla), espacio en disco razonable, y que
`./mvnw -q -version` funciona desde `dgt-tramites-api/`.

**Modo `--sin-docker` (D-007):** los chequeos de Docker pasan a `[SKIP]` con la
explicación de qué será demo del relator en ese escenario. El veredicto final distingue
`ESTACIÓN LISTA` de `ESTACIÓN LISTA (MODO SIN DOCKER: capacidades reducidas)`.

### 4.2 `start-lab.sh`

Desde la raíz del lab: levanta el compose de `dgt-tramites-api`, arranca la app
(`./mvnw spring-boot:run` en background con log a archivo, o foreground con
instrucción clara — a tu juicio, declara la elección), espera el health con reintentos
(sin `sleep` ciego: bucle con timeout), e imprime **la victoria del alumno**:

```
[OK] La DGT está viva. Pruébalo tú mismo:
     curl http://localhost:8080/api/contribuyentes/11111111-1
     → Valentina Rojas te va a responder.
```

Si el puerto 8080 está ocupado, lo detecta y **lo dice con nombre y apellido** (T-NN
del troubleshooting), sugiriendo `--puerto 8081` (bandera soportada).

### 4.3 README del lab

Narrativa: la bienvenida de Carolina (tono SPEC-000 §3), el checklist del pre-vuelo
(instalar → verificar → levantar → destruir), tiempos estimados, y la sección "Para el
Instructor" (qué revisar la semana previa, cómo interpretar los reportes de
`00-verificar` que manden los alumnos).

## §5 · El guardián de la semilla (hallazgo 1 de la SPEC-FIX-01)

En `dgt-tramites-api`, test de integración `SemillaCoherenteIT` (Testcontainers):
`contador_folio.ultimo_numero == MAX(folio.numero)` sobre los datos de V2, más
"todo trámite FOLIADO tiene folio y ningún no-FOLIADO lo tiene". La fila de la bitácora
deja de ser una frase: ahora muerde. Corre en el job `app` existente.

## §6 · Verificación del ejecutor (etapa 1 — antes de que el PO toque nada)

Con salidas citadas en el reporte, sobre **estado limpio** (sin la app corriendo, sin
contenedores previos):

1. `00-verificar.sh` en tu macOS: veredicto completo. Luego **sabotéalo**: escóndele
   Docker (PATH recortado o daemon detenido) y cita que reporta el faltante con mensaje
   accionable, no un stacktrace. Pruébalo también con `--sin-docker`.
2. `start-lab.sh`: arranque completo hasta el curl de Valentina, citado. Luego el
   sabotaje del puerto: con tu `alchemia-postgres`/proceso en 8080, cita que lo nombra
   y sugiere la salida. `99-destruir.sh`: cita que deja el sistema como estaba
   (`docker ps` antes/después) y que `alchemia-postgres` sobrevive intacto.
3. Ciclo completo dos veces seguidas (idempotencia del andamiaje): verificar → levantar
   → destruir → verificar → levantar → destruir.
4. `shellcheck` limpio sobre `lib-comunes.sh` y los tres `bin/` (instálalo si falta;
   las excepciones se justifican inline con `# shellcheck disable=SCXXXX # razón`).
5. CI: job nuevo `labs-sh` en `material-ci.yml`: `shellcheck` en ubuntu + `bash -n`
   (sintaxis) de todos los `labs/**/*.sh` en **windows-latest con Git Bash** — no
   ejecuta los scripts (no hay Docker ahí), pero garantiza que el día que un alumno de
   Windows los corra, al menos parsean. La limitación queda comentada en el YAML.
6. `SemillaCoherenteIT` verde dentro de `./mvnw verify`, citado.

## §7 · La Prueba del PO (etapa 2 — aceptación final)

Solo cuando §6 esté completo y citado, el reporte termina con esta invitación literal
para Rodrigo (tres comandos, resultados esperados escritos):

```
Rodrigo, tu turno (10 minutos):
  cd /Users/rodrigosilva/SII/SPRINGBOOT
  git pull
  cd labs/lab-00-estacion-base

  1) ./bin/00-verificar.sh          → esperas: "ESTACIÓN LISTA (7/7)"
  2) ./bin/start-lab.sh             → esperas el curl de Valentina; córrelo
  3) ./bin/99-destruir.sh           → esperas: "Todo quedó como estaba"

Si algo NO sale como lo escrito arriba, es un bug mío, no tuyo:
pégame la salida tal cual.
```

La SPEC-006 **no se considera cerrada** hasta que el PO reporte su resultado. Si el PO
encuentra un fallo, se corrige en la misma rama antes del merge (no es SPEC-FIX: la
SPEC aún no cerró).

## §8 · Criterios de aceptación

- [ ] SPEC-006 commiteada antes que el material; trabajo por rama + PR.
- [ ] `ESTADO.md` en la raíz, con sus 4 secciones, y la obligación registrada en el
      Protocolo SPEC del README (junto con el protocolo de dos etapas del preámbulo).
- [ ] `lib-comunes.sh` con contadores dinámicos, sin ANSI, bash 3.2, shellcheck limpio.
- [ ] Lab 00 completo según §4; toda la evidencia de §6 citada (sabotajes incluidos).
- [ ] `SemillaCoherenteIT` verde en local y en CI.
- [ ] Job `labs-sh` verde (ubuntu shellcheck + windows sintaxis), run citado.
- [ ] Bitácora: una fila por el nacimiento del Lab 00 y la caja de herramientas; una
      por el protocolo de dos etapas.
- [ ] **Prueba del PO ejecutada y reportada por Rodrigo** — criterio final.
- [ ] Commits `SPEC-006:`; PR mergeado con checks verdes.

## §9 · Reporte

Todas las salidas de §6 (incluidos los sabotajes y el doble ciclo), URL del run,
`git log --oneline`, discrepancias, hallazgos — y al final, la invitación de §7 al PO,
tal cual está escrita.
