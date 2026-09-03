# Requerimiento · Consolidado del contribuyente

**De:** Carolina Espinoza — Jefatura de Fiscalización, DGT
**Para:** el equipo de desarrollo

---

## Lo que necesito

Que los fiscalizadores puedan pedir un **consolidado de un contribuyente**: sus trámites de un
período, el estado de cada uno, y el total declarado.

Un fiscalizador escribe un RUT y dos fechas, y ve el resultado.

---

## La especificación, cerrada

Esto no tiene huecos. **Todo lo que necesitas decidir ya está decidido**: tu trabajo es
implementarlo, no adivinarlo.

### El endpoint

```
GET /consolidados/{rut}?desde=YYYY-MM-DD&hasta=YYYY-MM-DD
```

### Los cuatro casos, y qué responde cada uno

| caso | respuesta |
|---|---|
| Sin token | **401** |
| Con token de **CONTRIBUYENTE** | **403** |
| RUT que no existe | **404**, cuerpo `{"mensaje": "..."}` |
| Falta `desde` o `hasta` | **400**, cuerpo `{"mensaje": "..."}` |
| Contribuyente **sin trámites** en el período | **200**, `tramites: []` y `totalDeclarado: 0` |
| Todo bien | **200** con el consolidado |

> **Ojo a las dos filas del medio**, porque es el error más común de este encargo: un RUT que **no
> existe** es un 404. Un contribuyente que **existe y no tiene trámites en ese período** es un 200
> perfectamente normal, con la lista vacía y el total en cero. No son el mismo caso.

### Los campos que salen, exactamente estos

```json
{
  "rut": "76.111.111-1",
  "razonSocial": "Comercial Andes Ltda.",
  "desde": "2026-01-01",
  "hasta": "2026-12-31",
  "tramites": [
    { "id": 1, "tipo": "F29", "estado": "PAGADO", "fecha": "2026-01-15", "montoDeclarado": 1200000.00 }
  ],
  "totalDeclarado": 6330000.00
}
```

**Ningún campo más.** Si la entidad tiene algo que no está en esta lista, no sale.

### El total

`totalDeclarado` es la **suma de `montoDeclarado` de todos los trámites del período**, sin filtrar
por estado. Un trámite PENDIENTE o RECHAZADO suma igual: la pregunta es cuánto se **declaró**, no
cuánto se pagó.

### Quién puede verlo

**Sólo el rol FISCALIZADOR.** Los dos usuarios están sembrados y el login ya funciona:

```
ana   / secreta   FISCALIZADOR
luis  / secreta   CONTRIBUYENTE
```

### Lo que NO hay que hacer

Y es tan parte del encargo como lo anterior — **no** se evalúa, y hacerlo no suma:

- Sin proceso batch.
- Sin paginación.
- Sin reporte escrito.
- Sin endpoints extra.

---

## Lo que tienes que escribir · seis piezas

| # | Archivo | Qué hace |
|---|---|---|
| 1 | `dto/ConsolidadoContribuyente.java` · `dto/TramiteDelConsolidado.java` | Dos `record`. Lo que sale por el JSON |
| 2 | `repositories/TramiteRepository.java` | La consulta: trámites de un RUT entre dos fechas, **sin N+1** |
| 3 | `services/ConsolidadoService.java` | Arma el DTO y suma el total |
| 4 | `controllers/ConsolidadoController.java` | El endpoint |
| 5 | `seguridad/SeguridadConfig.java` | La regla de rol. **Una línea** |
| 6 | `src/test/java/…` | **Dos tests**: uno de servicio con `@Mock` que comprueba el total, uno de controller con `MockMvc` que comprueba el 404 |

Necesitarás además un `@RestControllerAdvice` para el 404 y el 400 con cuerpo — es el manejador de
errores del Lab 03.

---

## De dónde partes · `base/`

`base/` **compila, arranca y tiene su test en verde**. Trae resuelto todo lo que no es el encargo:

- El esquema, los datos sembrados y las entidades con sus repositorios.
- **La autenticación completa**: `POST /auth/login` devuelve un JWT válido, y la cadena de filtros
  exige token en todo salvo el login, `/actuator/health` y la documentación.
- **Actuator** con `health`, `info` y `metrics`.
- **OpenAPI**: `/swagger-ui.html` funciona. Tu endpoint aparecerá ahí **solo**, sin anotar nada.
- **La métrica de negocio ya declarada**: `ContadorDeConsolidados` existe y expone
  `dgt.consolidados.emitidos`. Pídelo por constructor en tu servicio y llama a
  `contador.emitidos().increment()`. Es **una línea**.
- **Jib** configurado: `./mvnw package jib:buildTar` construye la imagen.
- Un **test de contexto** que ya pasa.

---

## Y tienes el mismo encargo, resuelto · `ejemplo/`

**`ejemplo/` es `base/` con este mismo encargo hecho sobre otra entidad**, pieza por pieza, con los
mismos nombres de método y la misma forma de cada clase.

Ábrelo al lado de `base/` y **traduce**:

| Tu encargo | El ejemplo |
|---|---|
| Consolidado de un **contribuyente** | Resumen de una **oficina** |
| `GET /consolidados/{rut}?desde&hasta` | `GET /resumenes/{codigo}?desde&hasta` |
| `ConsolidadoContribuyente`, `TramiteDelConsolidado` | `ResumenOficina`, `TramiteDelResumen` |
| Consulta por RUT y fechas | Consulta por código de oficina y fechas |
| `ConsolidadoService` | `ResumenService` |
| `ConsolidadoController` | `ResumenController` |
| Sólo FISCALIZADOR: 401 y 403 | Sólo FISCALIZADOR: 401 y 403 |
| 404 si el RUT no existe | 404 si el código no existe |
| Dos tests: servicio y controller | Dos tests: servicio y controller |

Cada archivo del ejemplo lleva arriba **dos líneas** que dicen qué hace y cuál es su equivalente en
tu encargo. Los comentarios de dentro explican **por qué** está escrito así, no qué hace.

Para correrlo:

```bash
cd ejemplo
./mvnw test              # sus tres tests en verde
./mvnw spring-boot:run   # levanta en el 8108
```

> **Una diferencia que vas a notar, y es a propósito:** el ejemplo filtra por `oficinaCodigo`, que
> es una columna del propio trámite. Tú filtras por el RUT, que vive en `Contribuyente` — o sea que
> tu consulta **navega la relación**. Está dicho en el comentario de su `TramiteRepository`.

---

## Cómo se te evalúa

`rubrica/rubrica.md`, **seis criterios**, cada uno con su peso y con el comando que lo comprueba.
**Se aprueba con «suficiente» en los seis.** Léela antes de empezar: no es un examen sorpresa.

## Cómo se entrega

En el `README.md` de `proyecto-final/`. Un zip de tu `base/`, por correo.

**Plazo: viernes 25 de septiembre de 2026, 23:59.**
