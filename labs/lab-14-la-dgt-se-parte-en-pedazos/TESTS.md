# TESTS.md · Lab 14

*Qué prueba cada test, y por qué existe.*

Los tests viven en `sistema/dgt-tramites/src/test/java/cl/dgt/tramites/enunciado/`. Son
**siete**, en tres clases, y ninguno levanta un contenedor: corren en menos de dos segundos.

```bash
cd sistema
./mvnw test -pl dgt-tramites                                   # contra TU configuración
./mvnw test -pl dgt-tramites -Ddgt.config-repo=../config-repo-solucion   # contra la de referencia
```

---

## Por qué estos tests no tocan la red

Podrían. Sería más «realista» levantar las seis piezas y medir milisegundos. Y sería un
error, por dos razones:

1. **Un E2E de seis contenedores mide tiempos de propagación entre cachés.** En una máquina
   compartida —o en un runner de CI— eso parpadea. Un test que a veces falla se acaba
   ignorando, y un test ignorado protege menos que ninguno. La rúbrica del Lab 13 llama a
   eso «pipeline deshonesto», y no vamos a cometerlo en el lab siguiente.
2. **Lo que hay que juzgar es una DECISIÓN, no una máquina.** Los cuatro umbrales o sirven
   para el escenario del laboratorio o no sirven, y eso se puede determinar leyendo el
   archivo y ejerciendo un circuit breaker de verdad contra él.

Los milisegundos existen y se miden — pero en `bin/start-lab.sh --contribuyentes-lento`,
contra el sistema levantado, que es donde el alumno los ve caer con sus ojos.

**Aquí no hay ni un `Thread.sleep`.** Todo lo que se afirma es contable, no cronometrable.

---

## `UmbralesDelCircuitoTest` — el criterio de aceptación (3 tests)

Lee `config-repo/dgt-tramites.yml` —el archivo de verdad, el que edita el alumno—,
construye con él un `CircuitBreakerConfig` **real** de Resilience4j, y le da el trato del
escenario: diez llamadas seguidas que fallan.

| Test | Qué afirma | Qué avería caza |
|---|---|---|
| `elCircuitoAbreDentroDelEscenario` | tras 10 fallos el circuito está `OPEN` | **el circuit breaker decorativo**: con los defaults hacen falta 100 llamadas y no abre nunca |
| `abiertoDejaDeLlamar` | con el circuito abierto, la operación protegida **no se invoca** | que «fallar rápido» sea una frase y no un hecho |
| `abiertoRechazaConSuPropiaExcepcion` | rechaza con `CallNotPermittedException` | que el circuito reenvíe el error del vecino y confunda dos problemas distintos en el log |

**Sobre el segundo:** «falla rápido» no se mide con un reloj, se demuestra con un
**contador**. Cuando el circuito abre, el `Supplier` protegido deja de ejecutarse. Cero
invocaciones es una prueba más fuerte que «tardó poco», y no depende de lo cargado que esté
el portátil.

**Los valores ausentes se respetan.** Si el alumno no declara un umbral, el test deja el
default real de Resilience4j — no se inventa uno. Así reproduce exactamente lo que hace el
framework en tiempo de ejecución.

### Este es el gate, y muerde

| Configuración | Veredicto |
|---|---|
| `config-repo/` (starter, sin umbrales) | **3 fallos** → `90-validar.sh` sale con 1 |
| `config-repo-solucion/` | 7 verdes → sale con 0 |

Es el paso canónico de la SPEC-000 §7.6, y lo comprueba `91-e2e.sh` en cada ejecución: un
starter que ya aprueba es un enunciado sin ejercicio; una solución que no aprueba es un
enunciado sin salida.

**Y no se aprueba tecleando más código.** No hay ninguna clase que escribir. Se aprueba
entendiendo qué mide una ventana deslizante y eligiendo cuatro números que quepan en el
problema que tienes delante.

---

## `FallbackYRetryTest` — que las anotaciones hagan algo (2 tests)

Levanta un contexto de Spring **mínimo** —solo AOP y los aspectos de Resilience4j— con un
`ContribuyenteCliente` de mentira que siempre falla y cuenta cuántas veces lo llaman. Sin
base de datos, sin Config Server, sin red.

| Test | Qué afirma | Qué avería caza |
|---|---|---|
| `elFallbackDispara` | devuelve la ficha degradada con el RUT y `razonSocial` nula | firma del fallback mal escrita — **la aplicación arranca igual** y el fallo aparece en producción |
| `elRetryReintenta` | el cliente recibe exactamente `max-attempts` llamadas | el `fallbackMethod` puesto en `@CircuitBreaker` en vez de en `@Retry`: **el retry no reintenta nunca** |

El segundo merece una explicación, porque es una avería silenciosa de manual. Resilience4j
compone los aspectos así:

```
Retry ( CircuitBreaker ( tu método ) )
```

Con el `fallbackMethod` en el anillo **interior**, la excepción se convierte en respuesta
válida antes de que el `Retry` la vea. El `Retry` concluye que todo salió bien a la primera
y no reintenta. Sigue habiendo anotación en el código, número en la configuración y línea en
el diagrama de arquitectura. Y cero reintentos, para siempre.

El test también afirma algo del **negocio**: `razonSocial` tiene que venir **nula**, no
«Desconocido» ni «N/D». Un sistema que rellena huecos con texto inventado es peor que uno
que los deja vacíos: fabrica datos.

---

## `BalanceoTest` — que reparta, no que exista (2 tests)

Usa el `RoundRobinLoadBalancer` **real** de Spring Cloud con una lista fija de instancias.

| Test | Qué afirma |
|---|---|
| `repartePorMitades` | 20 llamadas sobre 2 instancias → exactamente 10 y 10 |
| `conUnaSolaNoSeRompe` | con una instancia viva, las 20 van ahí, sin quejarse |

**Por qué es determinista aunque el round-robin empiece donde quiera.**
`RoundRobinLoadBalancer` arranca en una posición aleatoria, y a propósito: si todas las
instancias de tu servicio empezaran en la posición cero, todas mandarían su primera petición
al mismo sitio. Así que no se puede afirmar quién atiende la primera llamada.

Lo que sí es determinista es el **reparto**: con un número par de llamadas y dos
instancias, salgan en el orden que salgan, tocan a la mitad cada una. Eso es lo que se
afirma, y es lo que de verdad significa balancear.

Un balanceador que manda siempre a la misma instancia es indistinguible de uno que funciona
— hasta el día que esa instancia se cae con el triple de carga que las demás.

---

## Lo que estos tests NO cubren, dicho en voz alta

- **Que las seis piezas se encuentren de verdad por el registro.** Eso se comprueba con el
  sistema levantado: `90-validar.sh` lo mira si Docker está arriba, y `start-lab.sh` no te
  devuelve el control hasta que el portal responde completo tres veces seguidas.
- **Los milisegundos.** Están en `start-lab.sh --contribuyentes-lento`.
- **El gateway y sus rutas.** No hay test de gateway: sus rutas son configuración servida
  por el Config Server, y lo que las certifica es que el portal responda.
- **El dominio.** No hay tests de negocio, y es coherente: este laboratorio no enseña
  dominio. Todo lo que hay que probar sobre folios, F29 y adjuntos ya se probó en los trece
  labs anteriores, contra la aplicación entera.
