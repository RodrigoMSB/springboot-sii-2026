# Rúbrica de evaluación · Proyecto final

Tres ejes × cuatro niveles. Los descriptores son **concretos y verificables**: si un descriptor no se
puede comprobar mirando la entrega o escuchando la defensa, no está en esta rúbrica.

> **Umbral de aprobación: núcleo verde (Correctitud + Oficio ≥ Suficiente) Y Criterio ≥ Suficiente.**
>
> Un alumno con todo verde y Criterio Insuficiente **no aprueba**. No es una severidad arbitraria: es
> la tesis del curso. La sintaxis la escribe la máquina; lo que se certifica aquí es lo otro.

**Cómo se mide, ahora que no hay validadores.** El arco antiguo tenía `90-validar.sh`, `91-e2e.sh` y
siete reglas ArchUnit. No existen. Cada descriptor de abajo dice **cómo se comprueba**, y todas las
comprobaciones son cosas que el relator hace en minutos con la entrega delante.

---

## Eje 1 · CORRECTITUD — *¿funciona, y funciona de verdad?*

| Nivel | Descriptores | Cómo se comprueba |
|---|---|---|
| **Insuficiente** | Cualquiera de: no compila · su suite falla · el endpoint no devuelve lo pedido · **el pipeline es deshonesto** (`@Disabled`, `catch` vacío, aserciones que no pueden fallar) | `./mvnw test` · un `curl` al endpoint · leer los tests buscando `@Disabled` y aserciones vacías |
| **Suficiente** | Compila, `./mvnw test` pasa, y `GET /consolidados/{rut}?desde=&hasta=` devuelve los trámites del período, su estado y el total | `./mvnw test` · `curl` con el token de `ana` |
| **Competente** | Lo anterior **y** los bordes resueltos con coherencia: RUT inexistente → **404** (no una lista vacía) · anónimo → **401** · `luis` (CONTRIBUYENTE) → **403** · el total corresponde **al período pedido**, no al histórico | Los cuatro `curl` de la §«Comprobación rápida» · comparar el total contra los datos sembrados |
| **Destacado** | Lo anterior **y** hay evidencia de haber pensado en lo que no se pidió: **el total no se infla** y hay una prueba que lo demuestra · el contribuyente sin trámites en el período devuelve un consolidado en cero y no un 404 · el camino del batch está pensado · documentó el contrato sin que nadie se lo pidiera | Leer la prueba del total · `curl` a `78.333.333-3` (sembrado sin trámites) · la defensa |

> **Señal de alarma que baja a Insuficiente aunque todo esté verde: una prueba que no puede fallar.**
>
> **Cómo se comprueba, en dos minutos:** comenta una línea de producción del alumno —el filtro de
> fechas del total, o su regla de rol— y corre su suite. Si sigue verde, el verde no valía nada.
>
> Está verificado que muerde: sobre la solución de referencia, quitar el filtro de período del total
> pone la suite en rojo con `el total se salió del período`, y quitar la regla de rol la pone en rojo
> con `Status expected:<403> but was:<200>`.

---

## Eje 2 · OFICIO — *¿está bien hecho por dentro?*

| Nivel | Descriptores | Cómo se comprueba |
|---|---|---|
| **Insuficiente** | Cualquiera de: **suite flaky** (tres corridas no coinciden) · una credencial literal en un archivo versionado · el esquema improvisado a mano en vez de una migración · el `puntaje_riesgo` sale en la respuesta | `./mvnw test` tres veces · `grep -rn "password\|secreto\|clave" src/` · mirar `db/migration/` · `curl` y buscar el campo |
| **Suficiente** | Suite determinista, sin credenciales versionadas, migraciones en orden, y el dato interno no sale | Lo mismo, en verde |
| **Competente** | Lo anterior **y** el código se lee como el resto del curso: el controlador **no conoce la entidad** · la lógica vive en el servicio · el DTO es **lista blanca** (enumera lo que sale, no excluye lo que no debe salir) · los nombres dicen lo que hacen | Leer `controllers/` buscando imports de `entities/` · leer el DTO: ¿enumera o excluye? |
| **Destacado** | Lo anterior **y** decisiones que se sostienen solas: la consulta no arrastra el ORM donde no toca · hay comentarios que explican **por qué**, no qué · el que llegue mañana no necesita preguntar nada | Leer la consulta y los comentarios |

> **Sobre el flaky: no se negocia.** Un test que a veces pasa no es una prueba, es una moneda, y una
> suite con una moneda dentro no protege nada. **Cómo se comprueba:** `./mvnw test` tres veces
> seguidas. Si los tres resultados no coinciden, el eje es Insuficiente aunque todo lo demás brille.
>
> Es un criterio con precedente: al construir la solución de referencia, su suite falló
> **exactamente así** —verde al correr un test solo, roja al correrlos juntos— por dos contextos de
> Spring levantando el mismo PostgreSQL. Si le pasó a la referencia, le va a pasar a alguien.

---

## Eje 3 · CRITERIO — *¿sabe por qué lo hizo así?*

**Lo mide:** el relator, con la defensa oral y el reporte. Guion en `instructor/guia-defensa.md`.

| Nivel | Descriptores |
|---|---|
| **Insuficiente** | No identifica ningún borde · no puede justificar sus decisiones más allá de «así lo hice» · atribuye sus elecciones a la costumbre o a lo que sugirió una herramienta, sin haberlas evaluado |
| **Suficiente** | Identifica **al menos un** borde y explica qué decidió y por qué · sabe decir qué probó y qué no · reconoce alguna limitación de su entrega |
| **Competente** | Identifica **varios** bordes y los resuelve con un criterio coherente entre ellos · sabe decir **qué habría hecho decidir lo contrario** · distingue lo que dejó fuera por alcance de lo que dejó fuera por descuido · su selección de pruebas tiene una lógica que puede explicar |
| **Destacado** | Lo anterior **y** anticipa el costo futuro de sus decisiones («esto se revisa el día que un contribuyente tenga miles de trámites, y así lo mediría») · nombra explícitamente lo que NO hizo y por qué era lo correcto no hacerlo · conecta su solución con lecciones concretas del curso, no con lugares comunes |

**La pregunta que separa Suficiente de Competente:** *«¿qué te habría hecho decidir lo contrario?»*.
Quien sólo puede defender su elección tomó una decisión; quien puede describir el escenario que la
invalida **entendió el problema**.

---

## Comprobación rápida (10 minutos por entrega)

```bash
cd <entrega-del-alumno>
./mvnw test && ./mvnw test && ./mvnw test        # determinismo
./mvnw spring-boot:run &

ANA=$(curl -s -X POST localhost:8107/auth/login -H 'Content-Type: application/json' \
      -d '{"usuario":"ana","clave":"secreta"}' | python3 -c 'import sys,json;print(json.load(sys.stdin)["token"])')
LUIS=$(curl -s -X POST localhost:8107/auth/login -H 'Content-Type: application/json' \
      -d '{"usuario":"luis","clave":"secreta"}' | python3 -c 'import sys,json;print(json.load(sys.stdin)["token"])')
Q='desde=2026-01-01&hasta=2026-12-31'

curl -s -o /dev/null -w '%{http_code} sin token\n'   "localhost:8107/consolidados/76.111.111-1?$Q"
curl -s -o /dev/null -w '%{http_code} contribuyente\n' -H "Authorization: Bearer $LUIS" "localhost:8107/consolidados/76.111.111-1?$Q"
curl -s -o /dev/null -w '%{http_code} rut inexistente\n' -H "Authorization: Bearer $ANA" "localhost:8107/consolidados/99.999.999-9?$Q"
curl -s -o /dev/null -w '%{http_code} sin periodo\n'  -H "Authorization: Bearer $ANA" "localhost:8107/consolidados/76.111.111-1"
curl -s -H "Authorization: Bearer $ANA" "localhost:8107/consolidados/76.111.111-1?$Q"
```

**Lo esperable:** `401 · 403 · 404 · 400` y un consolidado con **4 trámites y total 6.330.000**
(los dos trámites de 2025 quedan fuera). El campo `puntajeRiesgo` **no** debe aparecer.

Y el empaquetado: `./mvnw package jib:buildTar` deja `target/jib-image.tar`.

---

## Cómo se combina

| Correctitud | Oficio | Criterio | Resultado |
|---|---|---|---|
| ≥ Suficiente | ≥ Suficiente | ≥ Suficiente | **Aprueba** |
| ≥ Suficiente | ≥ Suficiente | Insuficiente | **No aprueba** |
| Insuficiente | cualquiera | cualquiera | **No aprueba** |
| cualquiera | Insuficiente | cualquiera | **No aprueba** |

El resultado se comunica **por eje**, nunca como un número. Un «Competente en Criterio, Suficiente en
Oficio» le dice al alumno dónde está y hacia dónde ir. Un «5,8» no le dice nada.

---

## Lo que esta rúbrica NO evalúa, a propósito

- **La cantidad de código.** Una solución de treinta líneas que se defiende gana a una de trescientas
  que no.
- **El parecido con la referencia.** `instructor/solucion-referencia/` es *una* solución. Si la del
  alumno difiere y defiende su criterio, puede estar igual de bien o mejor.
- **La velocidad.** Terminar en dos horas no suma. Terminar en tres, tampoco resta.
- **Si usó IA.** Se da por hecho que sí, y está bien: el curso entero parte de ahí. Lo que se evalúa
  es si sabe **auditar** lo que le entregó — y eso se ve en la defensa en treinta segundos.
