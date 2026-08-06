# Guía 02 · Leer un test como contrato

Abre `starter/src/test/java/cl/dgt/tramites/enunciado/E2_RutValidoTest.java`. No lo corras.
Léelo.

## Léelo al revés

```java
@DisplayName("un RUT con dígito verificador falso se rechaza")
void rutConDvFalso(String rut) {
    assertThat(validador.isValid(rut, null)).isFalse();   // ← el ASSERT: qué se promete
}
```

1. **El `@DisplayName`** es el titular: *"un RUT con DV falso se rechaza"*.
2. **El `assert`** lo hace concreto: `isValid(rut)` debe ser `false`.
3. **El `@ValueSource`** (arriba) da los casos: `11111111-2`, `12345678-9`, `7654321-0`.

Ese test es una **especificación ejecutable**. No dice *cómo* validar — dice *qué* debe
pasar. Tu implementación es libre, mientras cumpla el contrato.

## El ejercicio

Lee los cuatro archivos del enunciado y llena esto **antes** de implementar:

| Test | ¿Qué promete? | ¿Qué necesito para cumplirlo? |
|---|---|---|
| `E1_ValidacionDelRequestTest` | | |
| `E2_RutValidoTest` | | |
| `E3_TransicionIlegalTest` | | |
| `E4_AU05VigilaTest` | | |

El que lee los tests primero implementa lo que se pide. El que no, implementa lo que cree.

➡️ Siguiente: [`03-poner-la-suite-en-verde.md`](03-poner-la-suite-en-verde.md)
