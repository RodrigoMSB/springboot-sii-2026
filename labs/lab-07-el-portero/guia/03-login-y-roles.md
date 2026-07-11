# Guía 03 · El login real y los roles (TODO_2 y TODO_4)

## TODO_2 — El login real

Dos piezas. Primero, un `UserDetailsService` que carga de la tabla `usuario` (BCrypt de la
semilla) — el hash lo compara el `PasswordEncoder`, no tú:

```java
@Service
public class DgtUserDetailsService implements UserDetailsService {
    public UserDetails loadUserByUsername(String rut) {
        return usuarios.findByRut(rut)
            .map(u -> User.withUsername(u.getRut()).password(u.getClaveHash())
                          .roles(u.getRol().name()).build())
            .orElseThrow(() -> new UsernameNotFoundException("Credenciales inválidas"));
    }
}
```

Segundo, el emisor del JWT: autentica (contra ese servicio + BCrypt) y **firma** el token.

```java
Authentication auth = autenticador.authenticate(
        new UsernamePasswordAuthenticationToken(rut, clavePlana));   // 401 si falla
JwtClaimsSet claims = JwtClaimsSet.builder()
        .subject(rut).issuedAt(ahora).expiresAt(ahora.plus(Duration.ofHours(1)))
        .claim("roles", roles).build();
String token = encoder.encode(JwtEncoderParameters.from(
        JwsHeader.with(MacAlgorithm.HS256).build(), claims)).getTokenValue();
```

> **El 401 no distingue** si falló el usuario o la clave: distinguir le regalaría a un
> atacante qué RUT existen. Mismo mensaje siempre. El test `E2` verifica el token de tres
> partes y el 401 genérico.

## TODO_4 — Cada rol a su puerta

Emitir folios es acto de FUNCIONARIO. Una anotación sobre el método basta (con
`@EnableMethodSecurity` en la config):

```java
@PreAuthorize("hasRole('FUNCIONARIO')")
@PostMapping("/{id}/folio")
public ResponseEntity<FolioDto> emitirFolio(@PathVariable Long id) { ... }
```

- Carolina (FUNCIONARIO) emite → **201**.
- Valentina (CONTRIBUYENTE) lo intenta → **403** (autenticada, pero sin el rol), no 401.
- Ignacio (FISCALIZADOR) lee el listado → **200**.

El test `E4` los prueba a los tres. La diferencia 401/403 es el corazón: no es lo mismo "no
sé quién eres" que "sé quién eres y no puedes esto".

## La clave de firma (D-012)

No vive en el repo. Llega por `DGT_JWT_SECRET`, con default de utilería **solo en dev** y
obligatoria en prod (`VerificadorDeSecretosProd` no deja arrancar sin ella). Misma doctrina
del Lab 01.

## Cierra

```bash
./bin/90-validar.sh --dir starter
```

Los cuatro tests en verde, la arquitectura intacta, y los tests heredados (concurrencia,
RN-03) pasando ya autenticados. Completa `plantillas/reporte-entregable.md` y entrégalo.
