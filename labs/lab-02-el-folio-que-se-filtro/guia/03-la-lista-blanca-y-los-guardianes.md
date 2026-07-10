# Guía 03 · La lista blanca y los guardianes

**Acto 3 · La forma correcta** ✅

## TODO_1 · La ficha es una lista blanca

Crea `web/dto/FichaContribuyenteDTO.java`: un `record` con **exactamente** los campos
públicos, `rut` y `razonSocial`. Nada más. Un campo nuevo y sensible en la entidad no puede
colarse: para salir por la ficha, alguien tendría que agregarlo aquí a mano.

El mapeo entidad → DTO va en la capa `application` (un servicio o un mapper), nunca en la
web: `AU-01` prohíbe que `..web..` dependa de `..domain.entity..`.

## TODO_2 · La capa que faltaba

El controlador no piensa: traduce HTTP. Saca la lógica a `FichaService` — una **interfaz** y
su implementación. El controlador recibe la interfaz por constructor y delega.

```java
public interface FichaService { FichaContribuyenteDTO fichaDe(String rut); }
```

> **Pista.** Hay dos tests (`T2`) que lo verifican estructuralmente: que `FichaController`
> dependa de `FichaService`, y que NO hable directo con el repositorio.

## TODO_3 · Instala los guardianes

Abre `arquitectura/ReglasDeLaCasa.java`. `AU_01` y `AU_02` son cascarones: reglas
tautológicas que pasan siempre y no vigilan nada. **Reemplázalas enteras.**

- **AU-01:** ninguna clase en `..web..` depende de `..domain.entity..`.
- **AU-02:** ninguna clase anotada `@RestController` depende de una anotada `@Entity`.

> **La trampa (medida en el spike S-1):** NO uses `haveRawReturnType`. Un controlador que
> devuelve `ResponseEntity<Contribuyente>` tiene tipo crudo `ResponseEntity`, y esa regla
> pasa en verde mientras la entidad viaja en el genérico. Usa `dependOnClassesThat()`.

El test `T3` exige de cada guardián las dos mitades: que **pase** sobre tu código de
producción, y que **falle** sobre su fixture (la clase que lo viola a propósito). Un guardián
sin prueba de que muerde es un adorno.

> ⚠️ **Aviso que ahorra media hora:** si escribes AU-02 correctamente pero **no** hiciste el
> TODO_1 (tu controlador todavía devuelve la entidad), AU-02 te cazará a ti: `T3` fallará en
> "pasa sobre producción". El guardián no distingue tu crimen del de nadie. Arregla el
> TODO_1 primero.

## TODO_4 · El contrato visible

Anota el endpoint con OpenAPI (`@Operation`, `@ApiResponse`, `@Schema`) y confirma el
versionado `/api/v1/`. Levanta la app y navega:

```
http://localhost:8099/swagger-ui/index.html
```

| Pregunta | Tu respuesta |
|---|---|
| ¿Qué campos muestra el esquema de la ficha en Swagger? | |
| ¿Por qué `/api/v1/` y no `/api/`? | |

## Cierre

```bash
cd .. && ./bin/90-validar.sh
```

Buscas `🏆 LAB 02 APROBADO`. Luego completa el reporte.

➡️ ¿Sobró tiempo? [`99-desafio-el-tercer-campo.md`](99-desafio-el-tercer-campo.md)
