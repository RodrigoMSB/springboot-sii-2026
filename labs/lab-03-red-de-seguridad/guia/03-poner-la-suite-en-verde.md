# Guía 03 · Poner la suite en verde

**Acto 2 · El parche que la red desmiente** 🔨  y  **Acto 3 · La forma correcta** ✅

## El experimento (hazlo, y míralo fallar)

Abre `RutValidator` (TODO_2) y, en vez de implementar, hardcodea:

```java
public boolean isValid(String valor, ConstraintValidatorContext ctx) {
    return true;   // 🟡 el parche bruto
}
```

Corre solo ese test:

```bash
cd starter && ./mvnw test -Dtest='E2_RutValidoTest'
```

El caso *"un RUT válido se acepta"* pasa. Pero *"un RUT con DV falso se rechaza"* falla, y
*"basura de formato"* también. **La red triangula:** engañaste a un test, el conjunto te
desmintió. Prueba ahora `return false` — se invierte. No hay atajo: hay que implementar.

Borra el parche.

## Los cuatro TODOs, en orden sugerido

**TODO_2 · `@RutValido`.** Implementa el módulo 11 (la teoría §3 tiene el algoritmo). Normaliza
(quita puntos y guion, a mayúsculas), separa cuerpo y DV, valida que el cuerpo sea numérico,
calcula el DV y compáralo. Meta: `E2` en verde (los 12 casos).

**TODO_1 · Validación declarativa.** Anota `CrearTramiteRequest` (`@NotBlank`, `@Pattern`,
`@RutValido`). Y en `ManejadorDeErrores` escribe el handler de `MethodArgumentNotValidException`
→ 400 con una propiedad `campos` (mapa `campo → mensaje`). Meta: `E1` en verde.

**TODO_3 · El error con contrato.** En `ManejadorDeErrores`, el handler de
`TransicionIlegalException` → `ProblemDetail` 409 con `type`, `origen`, `destino`. Meta: `E3`.

**TODO_4 · Tus tests.** Abre `src/test/.../servicio/TramiteServiceTest.java`. **Elimina el olor**
(el `@Mock` de un DTO), y escribe dos tests con Mockito: que `crear` persiste en BORRADOR
(usa `ArgumentCaptor` para ver *qué* se guardó), y que con un RUT inexistente NO persiste
(`verify(..., never())`). Meta: el `90` marca TODO_4 en verde.

## El marcador

```bash
cd .. && ./bin/90-validar.sh --dir starter
```

Córrelo cada tanto. Verás el conteo subir. Cuando llegue a `🏆 LAB 03 APROBADO`, completa el
reporte.

➡️ ¿Sobró tiempo? [`99-desafio-el-rut-con-formato.md`](99-desafio-el-rut-con-formato.md)
