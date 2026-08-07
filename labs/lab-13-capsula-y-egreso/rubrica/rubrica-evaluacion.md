# Rúbrica de evaluación · Lab 13 (egreso)

Tres ejes × cuatro niveles. Los descriptores son **concretos y verificables**: si un descriptor no se
puede comprobar mirando la entrega o escuchando la defensa, no está en esta rúbrica.

> **Umbral de aprobación: núcleo verde (Correctitud + Oficio ≥ Suficiente) Y Criterio ≥ Suficiente.**
>
> Un alumno con todo verde y Criterio Insuficiente **no aprueba**. No es una severidad arbitraria: es
> la tesis del curso. La sintaxis la escribe la máquina; lo que se certifica aquí es lo otro.

---

## Eje 1 · CORRECTITUD — *¿funciona, y funciona de verdad?*

**Lo mide:** `90-validar.sh` (automático) + el relator para el salto a Competente/Destacado.

| Nivel | Descriptores |
|---|---|
| **Insuficiente** | Cualquiera de: no compila · la suite falla · el endpoint no responde lo pedido · **el pipeline es deshonesto** (`@Disabled` sobre pruebas, `catch` vacío, aserciones que no pueden fallar) · la imagen OCI no se construye |
| **Suficiente** | Compila, la suite pasa, el endpoint devuelve trámites + estado + total del período, y la imagen arranca con `/actuator/health` en UP |
| **Competente** | Lo anterior **y** los bordes del brief están resueltos con coherencia: el RUT inexistente responde 404 (no una lista vacía), el rol se exige de verdad (403 al contribuyente, 401 al anónimo), y el total corresponde al período pedido y no al histórico |
| **Destacado** | Lo anterior **y** hay evidencia de haber pensado en lo que no se pidió: el total no se infla con un `JOIN` mal agrupado y hay una prueba que lo demuestra · el batch tiene un camino pensado · el contrato está documentado (OpenAPI) sin que nadie lo pidiera |

**Señal de alarma que baja a Insuficiente aunque todo esté verde:** una prueba que no puede fallar.
Si el relator comenta una línea de producción y la suite sigue verde, el verde no valía nada.

---

## Eje 2 · OFICIO — *¿está bien hecho por dentro?*

**Lo mide:** `90-validar.sh` + `91-e2e.sh` (semi-automático) + lectura del diff.

| Nivel | Descriptores |
|---|---|
| **Insuficiente** | Cualquiera de: una regla ArchUnit rota · **suite flaky** (las 3 corridas del `91` no coinciden) · una credencial literal en un archivo versionado · migraciones con huecos o esquema improvisado |
| **Suficiente** | Los 7 guardianes verdes, suite determinista, sin credenciales, migraciones versionadas y en orden |
| **Competente** | Lo anterior **y** el código se lee como el resto del proyecto: el controlador no conoce la entidad · la lógica vive en la capa de aplicación · el DTO es **lista blanca** (enumera lo que sale, no excluye lo que no debe salir) · los nombres dicen lo que hacen |
| **Destacado** | Lo anterior **y** decisiones de diseño que se sostienen solas: la consulta agregada no arrastra el ORM donde no toca · hay comentarios que explican **por qué**, no qué · el que llegue mañana a mantenerlo no necesita preguntar nada |

**Nota sobre el flaky:** no se negocia. Un test que a veces pasa no es una prueba, es una moneda, y
una suite con una moneda dentro no protege nada. Si el `91` lo declara, el eje es Insuficiente aunque
todo lo demás brille.

---

## Eje 3 · CRITERIO — *¿sabe por qué lo hizo así?*

**Lo mide:** el relator, con la defensa oral y el reporte de egreso. Guion en
[`guia-instructor.md`](guia-instructor.md).

| Nivel | Descriptores |
|---|---|
| **Insuficiente** | No identifica ningún borde del brief · no puede justificar sus decisiones más allá de «así lo hice» · atribuye sus elecciones a la costumbre o a lo que sugirió una herramienta, sin haberlas evaluado |
| **Suficiente** | Identifica **al menos un** borde y explica qué decidió y por qué · sabe decir qué probó y qué no · reconoce alguna limitación de su entrega |
| **Competente** | Identifica **varios** bordes y los resuelve con un criterio coherente entre ellos · sabe decir **qué habría hecho decidir lo contrario** · distingue lo que dejó fuera por alcance de lo que dejó fuera por descuido · su selección de pruebas tiene una lógica que puede explicar |
| **Destacado** | Lo anterior **y** anticipa el costo futuro de sus decisiones («esto se revisa el día que un contribuyente tenga miles de trámites, y así lo mediría») · nombra explícitamente lo que NO hizo y por qué era lo correcto no hacerlo · conecta su solución con lecciones concretas del curso, no con lugares comunes |

**La pregunta que separa Suficiente de Competente:** *«¿qué te habría hecho decidir lo contrario?»*.
Quien solo puede defender su elección tomó una decisión; quien puede describir el escenario que la
invalida **entendió el problema**.

---

## Cómo se combina

| Correctitud | Oficio | Criterio | Resultado |
|---|---|---|---|
| ≥ Suficiente | ≥ Suficiente | ≥ Suficiente | **Aprueba** |
| ≥ Suficiente | ≥ Suficiente | Insuficiente | **No aprueba** — ver «qué hacer con el que no alcanza» |
| Insuficiente | cualquiera | cualquiera | **No aprueba** |
| cualquiera | Insuficiente | cualquiera | **No aprueba** |

El resultado se comunica **por eje**, nunca como un número. Un «Competente en Criterio, Suficiente en
Oficio» le dice al alumno dónde está y hacia dónde ir. Un «5,8» no le dice nada.

---

## Lo que esta rúbrica NO evalúa, a propósito

- **La cantidad de código.** Una solución de treinta líneas que se defiende gana a una de trescientas
  que no.
- **El parecido con la referencia.** `solucion-referencia/` es *una* solución. Si la del alumno
  difiere y defiende su criterio, puede estar igual de bien o mejor.
- **La velocidad.** Terminar en dos horas no suma. Terminar en tres, tampoco resta.
- **Si usó IA.** Se da por hecho que sí, y está bien: el curso entero parte de ahí. Lo que se evalúa
  es si sabe **auditar** lo que le entregó — y eso se ve en la defensa en treinta segundos.
