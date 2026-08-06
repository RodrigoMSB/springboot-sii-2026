# Los tests del Lab 07

## El enunciado (`enunciado/`) — protegido, necesita Docker

| Test | TODO | Qué prueba |
|---|---|---|
| `E1_PuertaCerradaIT` | TODO_1 | sin token → 401; health → 200; una ruta sin regla nace cerrada |
| `E2_LoginIT` | TODO_2 | credenciales buenas → JWT de 3 partes; malas → 401 genérico |
| `E3_TokenFirmadoIT` | TODO_3 | token válido → 200; token adulterado (firma vieja) → 401 |
| `E4_RolesIT` | TODO_4 | Carolina emite 201; Valentina 403; Ignacio lee 200 |

`BaseSeguridadIT` trae el helper de login (los tres usuarios, clave `dgt-2026`) y el contenedor.

## Regresión y compatibilidad hacia atrás

Este lab **rompe hacia atrás**: la API ahora exige token, así que los tests heredados que golpean
endpoints protegidos GANARON autenticación (no se relajó la seguridad):

| Test | Cómo se adaptó |
|---|---|
| `web/ContratoRn03IT` | hace login como Ignacio (FISCALIZADOR) y pasa el token |
| `web/ContribuyenteControllerTest` | `@WithMockUser` (rebanada `@WebMvcTest`) |
| `concurrencia/E1..E4` (del Lab 06) | llaman al servicio directo, sin HTTP: no los toca la seguridad |

Cada divergencia está declarada en `derivacion-solucion.txt`. Es el precedente: cuando un lab rompe
los supuestos de sus antecesores, los tests afectados se autentican y se declaran.

## Comandos

```bash
./bin/90-validar.sh --dir starter     # tu trabajo
./bin/90-validar.sh --dir solucion    # la referencia — el mismo criterio
```
