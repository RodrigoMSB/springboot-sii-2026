# Desafío 99 · Refresh tokens y firma asimétrica (opcional)

> Opcional (P-15). Si no lo haces, no pierdes nada del lab. Si lo haces, márcalo con
> honestidad en tu reporte.

## Reto A — Un refresh token

El access token dura una hora. Emite además un *refresh token* de vida larga que canjee
nuevos access tokens sin re-loguear. Piensa:

1. ¿Dónde se guarda el refresh token, y por qué con más cuidado que el access?
2. ¿Qué pasa si te lo roban? ¿Cómo lo revocas? (pista: un access token corto no se revoca;
   un refresh sí necesita una lista)

## Reto B — De HS256 a RS256

Cambia la firma simétrica (un secreto) por asimétrica (clave privada firma, pública valida):

1. Genera un par de claves RSA.
2. Firma con la privada (`NimbusJwtEncoder` con un `RSAKey`), valida con la pública
   (`NimbusJwtDecoder.withPublicKey(...)`).
3. Pregunta de criterio: ahora podrías repartir la clave pública a diez APIs y ninguna podría
   fabricar tokens. ¿Cuándo vale esa complejidad, y cuándo la simétrica es suficiente?

No hay validador para esto: es exploración. Resume tu conclusión en el reporte.
