# Guía del instructor · Lab 08 — diplomacia con Tesorería

## Antes de la sesión

1. `./bin/91-e2e.sh` — el starter falla los TODOs de resiliencia, la solución pasa. Si falla, no des la clase.
2. **Docker corriendo**, y el puerto **8089** libre (es el de TESO/WireMock). Ensaya:
   `./bin/start-lab.sh --dir starter --teso-lento 30000` (la API se cuelga) y `--dir solucion --teso-lento 30000` (viva).

## ORDEN PARA CLASE (180 min)

### 🔪 00:00 – 00:10 · La escena del crimen (en vivo)

```bash
./bin/start-lab.sh --dir starter --teso-lento 30000
```

Que la sala vea 12 pagos colgados y el `GET /tramites` —que no toca pagos— muriéndose. Entonces, Carolina:

> *«TESO se cayó a las 9. A las 9:02 nosotros también — y nosotros no tenemos nada malo.»*

### 📚 00:10 – 00:50 · Teoría

`TEORIA.md`. Imprescindibles:
- **§1 y §2, el rehén y el timeout.** El hilo/conexión secuestrado. Timeout = presupuesto de espera.
- **§3, por qué agrandar el pool no resuelve.** Posponer no es resolver.
- **§4, la mala noticia rápida.** El 503 elegante. La pregunta de criterio.
- **§8, gRPC.** El otro protocolo del módulo. Ver la demo de abajo.
- **§10, la hora de M9.** CORS nominal, CSRF con criterio.

**Demo del relator (Feign):** muestra un cliente Feign al lado de un `@HttpExchange`. Misma idea; Feign en
mantenimiento. Criterio: sistema viejo con Feign, quédate; desarrollo nuevo, HTTP Interfaces nativas.

---

#### 🎬 Demo del relator · gRPC (~8 min, dentro de la teoría)

El Módulo 10 se llama «Comunicación entre Servicios: HTTP Declarativo **y gRPC**». El
laboratorio construye el lado HTTP; esto muestra el otro. **No es un TODO** y no toca el
presupuesto de tecleo del alumno.

> ⚠️ **Ensáyalo la semana previa.** La primera compilación descarga `protoc` (el compilador
> de Protocol Buffers, binario nativo) y genera los stubs. Con la caché fría son un par de
> minutos que no se pueden gastar en clase.

```bash
cd labs/lab-08-diplomacia-con-tesoreria/demo-grpc
```

1. **Abre `src/main/proto/teso.proto` y proyéctalo.** Esta es la demo entera: el contrato
   existe **antes** que el código. *«En vuestro laboratorio, el contrato con TESO es un
   acuerdo entre dos equipos que nadie comprueba. Aquí es un archivo, y si lo cambias deja
   de compilar.»*

   Señala los números (`= 1`, `= 2`): *«El nombre `folio` no viaja por la red. Viaja el
   número 1. Por eso podéis renombrar un campo sin romper nada — y por eso cambiar su número
   es un incidente.»*

2. **Muestra que el código Java no está escrito por nadie:**
   ```bash
   ./mvnw -q generate-sources
   ls target/generated-sources/protobuf/cl/dgt/grpc/teso/
   ```
   Aparece `TesoreriaGrpc.java` y compañía. *«Esto no está en el repositorio. Se regenera en
   cada compilación. Y el mismo `.proto` genera el cliente en Go o en Python para el equipo
   que no usa Java.»*

3. **Levanta el servidor:**
   ```bash
   ./mvnw spring-boot:run
   ```
   En el log: `gRPC Server started, listening on address: ..., port: 9090`.

4. **La pregunta que cierra la demo.** Pide a la sala que le haga un `curl`. Deja que lo
   intenten unos segundos.

   > *«No podéis. Va sobre HTTP/2 con cuerpos binarios. Acabáis de perder la herramienta con
   > la que investigáis incidencias, y eso no aparece en ningún gráfico de rendimiento.»*

5. **Y la prueba de que funciona**, en otra terminal:
   ```bash
   ./mvnw test
   ```
   Un test que llama al servicio de verdad y verifica el comprobante. *«Un verde delante de
   vosotros vale más que una diapositiva.»*

**Cierra con el criterio, no con la tecnología** (§8 de la teoría): entre dos servicios
tuyos con mucho tráfico, sí. Para la API pública de la DGT, no — un contribuyente no va a
generar stubs para consultar su folio.

### ☕ 00:50 – 01:00 · Descanso

### 🔧 01:00 – 02:50 · Laboratorio

**El error que cometerá la sala:** agrandar el pool en vez de poner timeout. Muéstrales que con pool N+1 pagos
muere igual. **Segundo:** poner el timeout global en vez de dirigido al cliente de TESO — todos los endpoints
pagan el peaje. **Tercero:** al hacer CORS, poner `*` "para que funcione". Muéstrales `E4`: el intruso pasa.

### ✅ 02:50 – 03:00 · Cierre — LA SIEMBRA DE LOS 400 MB

`90`, reporte, y la siembra del Lab 09 (`TEORIA §12`):

> *«TESO ya no puede matarnos. Pero anoche alguien emitió un folio al contribuyente equivocado, y Carolina
> llegó con 400 MB de logs y una sola pregunta: encuéntralo. La próxima semana traes lupa.»*

## Qué revisar en los reportes

1. **§1, el crimen.** ¿Transcribió el `/tramites` colgado?
2. **§2, el timeout.** ¿Entendió connect vs read, y por qué cortos y dirigidos? Es el corazón.
3. **§3, la mala noticia.** ¿Por qué 503 rápido > 30 s?
4. **§4, CORS.** ¿Por qué `*` es rendirse?
5. **§5, honestidad.** Nunca penalices un "usé `--todo`".
