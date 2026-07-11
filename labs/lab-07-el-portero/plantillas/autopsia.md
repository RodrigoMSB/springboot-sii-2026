# Autopsia · Lab 07

Para pensar en voz alta mientras depuras. No se entrega.

## La puerta
- ¿`E1` rojo? ¿Puse `anyRequest().authenticated()` o se me quedó un `permitAll()`?
- ¿La ruta sin regla responde 401? Si responde 200, mi default está abierto.

## El login
- ¿El token tiene tres partes separadas por punto? Si no, no estoy firmando un JWT.
- ¿Uso `PasswordEncoder` (BCrypt) para COMPARAR, o comparo strings a mano?
- ¿El 401 de clave mala es idéntico al de usuario inexistente?

## La firma
- ¿`E3` (adulterado) rojo? ¿Estoy validando la firma con el `JwtDecoder`, o leyendo el payload?
- ¿El secreto del decoder es el MISMO con que firmo? (HS256, simétrica)

## Los roles
- ¿`@EnableMethodSecurity` está puesto? Sin él, `@PreAuthorize` es decorativo.
- ¿Valentina recibe 403 (no 401, no 200)?

## El secreto
- ¿`dgt.jwt.secret` resuelve en dev? ¿La app arranca sin `DGT_JWT_SECRET` en dev pero no en prod?
