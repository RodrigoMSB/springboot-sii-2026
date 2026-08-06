# Guía 02 · Cerrar la puerta (TODO_1 y TODO_3)

## TODO_1 — Denegar por defecto

Reemplaza la puerta abierta por un `SecurityFilterChain` que cierra todo y abre solo lo justo:

```java
http
    .csrf(AbstractHttpConfigurer::disable)                 // API sin estado; CSRF es del Lab 08
    .sessionManagement(s -> s.sessionCreationPolicy(STATELESS))
    .authorizeHttpRequests(reglas -> reglas
        .requestMatchers("/actuator/health", "/actuator/info").permitAll()
        .requestMatchers("/api/v1/auth/login").permitAll()
        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll()
        .anyRequest().authenticated())                     // <- todo lo demás, CERRADO
    .oauth2ResourceServer(o -> o.jwt(j -> j.jwtAuthenticationConverter(conversorDeRoles())));
```

> **La regla del pulgar:** la lista blanca es de puertas ABIERTAS, nunca de cerradas. Si
> olvidas una regla, la ruta nace **cerrada** (401), no abierta. El error cae hacia lo seguro.
> El test `E1` lo prueba: `/api/contribuyentes` —que nadie cerró a mano— responde 401.

## TODO_3 — El validador que no cree

El `oauth2ResourceServer(...jwt...)` de arriba es el validador: un `JwtDecoder` que
**recalcula la firma** del token y la compara. Si no cuadra, 401 — aunque el payload sea
perfecto.

```java
@Bean
JwtDecoder jwtDecoder() {
    return NimbusJwtDecoder.withSecretKey(clave).macAlgorithm(MacAlgorithm.HS256).build();
}
```

Y el claim `roles` del token se convierte en autoridades `ROLE_<rol>`:

```java
JwtGrantedAuthoritiesConverter autoridades = new JwtGrantedAuthoritiesConverter();
autoridades.setAuthoritiesClaimName("roles");
autoridades.setAuthorityPrefix("ROLE_");
```

El test `E3` toma un token válido, le edita el payload (un ascenso a FUNCIONARIO), deja la
firma vieja, y exige **401**. Es el contraste exacto con el base64 del starter: allí editar
el payload te ascendía; aquí, te deja fuera.

Sigue con [`03-login-y-roles.md`](03-login-y-roles.md).
