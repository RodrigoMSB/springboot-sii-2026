# Troubleshooting · Lab 07

## L7-01 · `E1` rojo: una ruta sin regla responde 200
Tu default está ABIERTO. Usa `anyRequest().authenticated()` y una lista blanca SOLO de lo público.
La lista blanca es de puertas abiertas, nunca de cerradas.

## L7-02 · El login devuelve algo sin puntos (no es un JWT)
Estás devolviendo base64, no un JWT firmado. Un JWT es `header.payload.signature`. Usa el
`JwtEncoder` con `MacAlgorithm.HS256`.

## L7-03 · `E3` rojo: el token adulterado pasa
No estás validando la firma. No decodifiques el payload "a mano": deja que el
`oauth2ResourceServer(...jwt...)` + `JwtDecoder` recalculen el HMAC. Si un byte cambió, muere.

## L7-04 · `@PreAuthorize` no hace nada
Falta `@EnableMethodSecurity` en la config. Sin él, la anotación es decorativa y cualquiera emite.

## L7-05 · Valentina recibe 401 en vez de 403
401 es "no autenticado". Si Valentina trae un token válido y aun así recibe 401, tu filtro no la
está autenticando. Debe llegar a `@PreAuthorize` (autenticada) y ahí fallar por rol → 403.

## L7-06 · La app no arranca: `Could not resolve placeholder 'dgt.jwt.secret'`
Falta el default en dev, o corres en prod sin `DGT_JWT_SECRET`. En dev, `application-dev.yml` trae
el default de utilería. En prod, exporta la variable (es obligatoria a propósito).

## L7-07 · `IllegalArgumentException: secret key ... 256 bits`
HS256 exige una clave de al menos 32 bytes. El default de dev ya los tiene; si pusiste el tuyo,
hazlo largo.

## L7-08 · Docker no responde
Este lab necesita Docker: los tests de seguridad levantan PostgreSQL con Testcontainers. Ver T-03 del Lab 00.
