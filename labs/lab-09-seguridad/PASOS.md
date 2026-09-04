# Pasos · Lab 09 · Seguridad

Cinco pasos. Se trabaja en `practica/`, en vivo. Después de cada paso se reinicia la aplicación y
se prueba antes de seguir.

```bash
cd practica
./mvnw spring-boot:run
```

Escucha en el **8095** (`solucion/`, en el 8096). La base va en el **55440**.

Lo que llega hecho: la API de productos, la entidad `Usuario`, su repositorio y la tabla. Lo que
se escribe hoy vive en tres carpetas que llegan vacías:

```
config/      →  SeguridadConfig
services/    →  UsuarioDetailsService, ServicioDeTokens
soporte/     →  SembradorDeUsuarios
controllers/ →  AuthController, y un endpoint en ProductoController
```

---

## Paso 0 · La puerta abierta

**Se explica:** el punto de partida es la API que ya se conoce, y no pide nada a nadie.

**Se corre:** `./mvnw spring-boot:run`

**En consola:**

```
$ curl -o /dev/null -w "%{http_code}\n" http://localhost:8095/productos
200
```

Doscientos. Desde cualquier máquina que alcance el puerto, sin identificarse. **Ctrl+C.**

---

## Paso 1 · Una línea, y todo se cierra

**Se explica:** no se va a configurar nada todavía. Sólo se añade la dependencia y se arranca,
para ver qué hace Spring Security **antes** de que nadie le diga nada.

**Se pega:** en `practica/pom.xml`, **dentro de `<dependencies>`**, junto a las demás.

```xml
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-security</artifactId>
    </dependency>
```

**Se corre:** `./mvnw spring-boot:run`

**En consola:** aparece una línea que antes no estaba.

```
Using generated security password: 899fd35b-1dc5-4031-be00-3c284b88c6de
Tomcat started on port 8095 (http) with context path '/'
Started Lab09Application in 2.587 seconds
```

Y la API de hace un minuto:

```
$ curl -o /dev/null -w "%{http_code}\n" http://localhost:8095/productos
401

$ curl -o /dev/null -w "%{http_code}\n" -u user:899fd35b-1dc5-4031-be00-3c284b88c6de \
       http://localhost:8095/productos
200
```

**Lo que hay que notar, y es la idea del paso:** nadie escribió una línea de configuración y
**todo** quedó cerrado. Ese es el default de Spring Security, y es el correcto:

> Si te olvidas de proteger una ruta, queda **protegida**. El olvido cuesta una llamada que
> falla, no una filtración.

El default contrario —abierto salvo que digas lo contrario— convierte cada olvido en un agujero.
La clave generada cambia en cada arranque y sirve exactamente para lo que se acaba de hacer:
comprobar que la puerta existe. **No es un mecanismo de autenticación**, y es lo primero que se
va en el paso siguiente.

---

## Paso 2 · La cadena de filtros, y los cuatro beans

**Se explica:** ahora se toma el control. Una petición, antes de llegar a un controller, atraviesa
una **cadena de filtros**; ahí se decide si sigue o se corta. Se declara una vez, en una clase de
configuración, y **no se vuelve a tocar en todo el laboratorio**: los pasos siguientes añaden
piezas alrededor, no aquí.

Se escribe entera de una vez —la cadena y sus cuatro beans— y después se lee en voz alta.

**Se pega:** en `practica/pom.xml`, **dentro de `<dependencies>`** — la segunda y última
dependencia del día. Es la que trae el validador de tokens y la librería que firma.

```xml
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-oauth2-resource-server</artifactId>
    </dependency>
```

**Se pega:** archivo **nuevo** `practica/src/main/java/cl/dgt/seguridad/config/SeguridadConfig.java` — el archivo entero.

```java
package cl.dgt.seguridad.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.JwtTimestampValidator;
import org.springframework.security.web.SecurityFilterChain;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

@Configuration
public class SeguridadConfig {

    @Value("${lab09.jwt.secreto}")
    private String secreto;

    @Bean
    SecurityFilterChain cadena(HttpSecurity http) throws Exception {
        return http
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(rutas -> rutas
                        .requestMatchers("/auth/login").permitAll()
                        .requestMatchers("/productos/administracion").hasAuthority("SCOPE_ROLE_ADMIN")
                        .anyRequest().authenticated())
                .oauth2ResourceServer(oauth -> oauth.jwt(Customizer.withDefaults()))
                .build();
    }

    @Bean
    PasswordEncoder codificadorDeClaves() {
        return Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8();
    }

    @Bean
    AuthenticationManager gestorDeAutenticacion(AuthenticationConfiguration configuracion) throws Exception {
        return configuracion.getAuthenticationManager();
    }

    private SecretKeySpec clave() {
        return new SecretKeySpec(secreto.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
    }

    @Bean
    JwtEncoder codificadorDeTokens() {
        return NimbusJwtEncoder.withSecretKey(clave()).build();
    }

    @Bean
    JwtDecoder decodificadorDeTokens() {
        NimbusJwtDecoder decodificador = NimbusJwtDecoder.withSecretKey(clave()).build();

        decodificador.setJwtValidator(new DelegatingOAuth2TokenValidator<>(
                new JwtTimestampValidator(Duration.ZERO)));
        return decodificador;
    }
}
```

**Se corre:** `./mvnw spring-boot:run`

**En consola:**

```
$ curl -o /dev/null -w "%{http_code}\n" http://localhost:8095/productos
401

$ curl -o /dev/null -w "%{http_code}\n" -H "Authorization: Bearer cualquier-cosa" \
       http://localhost:8095/productos
401
```

Y **desapareció** la línea de la contraseña generada: en cuanto la aplicación declara su propia
cadena y su propio gestor, Spring deja de inventarse un usuario.

**Lo que hay que notar,** y son cuatro cosas que se leen sobre el archivo proyectado:

- **`STATELESS`** — no hay sesión. Cada petición trae su credencial y se basta sola. Es lo que
  permite tener diez instancias detrás de un balanceador sin compartir nada entre ellas.
- **`csrf().disable()`** — no es bajar la guardia. CSRF existe porque el navegador adjunta la
  cookie **solo**; aquí la credencial es una cabecera que hay que poner a mano en cada llamada,
  y ninguna página ajena puede hacer que el navegador la ponga. Sin cookie de sesión no hay CSRF
  que prevenir.
- **El orden de las reglas** — se leen de arriba abajo y gana la primera que encaja. Si la línea
  de `/productos/administracion` estuviera **debajo** de `anyRequest()`, no se llegaría nunca a
  ella y la ruta quedaría abierta a cualquier autenticado. Sin error y sin aviso: sólo un
  agujero. De lo más específico a lo más general, siempre.
- **`anyRequest().authenticated()`** — el cierre, y la línea más importante de las tres. Es el
  mismo default que Spring aplicó solo en el paso 1, ahora escrito a propósito.

> **Aviso, porque muerde:** hay que usar `NimbusJwtEncoder.withSecretKey(...)`. Con el
> constructor genérico, el encoder intenta firmar con RS256, la clave es HMAC, y falla en
> ejecución con `Failed to select a JWK signing key`.

Y el estado en que queda la aplicación es el interesante: **todo cerrado y sin forma de entrar
todavía**. Falta quién eres —el paso 3— y cómo lo demuestras —el paso 4.

---

## Paso 3 · Los usuarios, y por qué una clave no se guarda

**Se explica:** hacen falta usuarios de verdad, en la base. Y aquí aparece la regla que no se
negocia: **una contraseña no se guarda nunca**. El problema no es sólo que alguien lea la base:
es que **la gente repite contraseñas**, así que una filtración aquí compromete el correo y el
banco de esa persona.

La salida no es cifrar —cifrar es reversible— sino guardar un **hash**: una función de un solo
sentido. Al hacer login se vuelve a calcular y se comparan los hashes; la clave original **no
existe en ninguna parte del sistema**.

**Argon2** —concretamente **Argon2id**, que es lo que recomienda OWASP hoy— añade tres cosas
sobre un hash normal:

- **Sal**: un valor aleatorio distinto por contraseña, que se guarda dentro del propio hash. Por
  eso dos personas con la misma clave tienen hashes distintos, y por eso no sirve una tabla de
  hashes precalculada.
- **Costo en tiempo**: es **lento a propósito**. Al que hace login le cuesta unos milisegundos; al
  que prueba millones de claves por segundo le arruina el negocio.
- **Y costo en MEMORIA**, que es lo que lo distingue de BCrypt y la razón de que hoy se prefiera:
  cada cálculo exige reservar **16 MB**. Una GPU tiene miles de núcleos y muy poca memoria por
  núcleo, así que el ataque por fuerza bruta que hace a BCrypt vulnerable —lanzar veinte mil
  cálculos en paralelo en una tarjeta gráfica— aquí choca contra la RAM. Ésa es la debilidad de
  BCrypt hoy, y es la que Argon2 tapa.

El `PasswordEncoder` ya está declarado desde el paso 2. Faltan las dos clases que lo usan.

**Se pega:** archivo **nuevo** `practica/src/main/java/cl/dgt/seguridad/services/UsuarioDetailsService.java` — el archivo entero.
Es el puente entre la tabla y Spring Security.

```java
package cl.dgt.seguridad.services;

import cl.dgt.seguridad.repositories.UsuarioRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UsuarioDetailsService implements UserDetailsService {

    private final UsuarioRepository repositorio;

    public UsuarioDetailsService(UsuarioRepository repositorio) {
        this.repositorio = repositorio;
    }

    @Override
    public UserDetails loadUserByUsername(String nombre) {
        return repositorio.findByNombre(nombre)
                .map(u -> User.withUsername(u.getNombre())
                        .password(u.getClaveHash())
                        .roles(u.getRol())
                        .build())
                .orElseThrow(() -> new UsernameNotFoundException("No existe el usuario " + nombre));
    }
}
```

**Se pega:** archivo **nuevo** `practica/src/main/java/cl/dgt/seguridad/soporte/SembradorDeUsuarios.java` — el archivo entero.
Crea los dos usuarios la primera vez, y **imprime los hashes siempre**, se haya sembrado o no.

```java
package cl.dgt.seguridad.soporte;

import cl.dgt.seguridad.entities.Usuario;
import cl.dgt.seguridad.repositories.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class SembradorDeUsuarios implements CommandLineRunner {

    private final UsuarioRepository repositorio;
    private final PasswordEncoder codificador;

    public SembradorDeUsuarios(UsuarioRepository repositorio, PasswordEncoder codificador) {
        this.repositorio = repositorio;
        this.codificador = codificador;
    }

    @Override
    public void run(String... args) {
        if (repositorio.count() == 0) {
            repositorio.save(new Usuario("ana", codificador.encode("dgt2026"), "ADMIN"));
            repositorio.save(new Usuario("luis", codificador.encode("dgt2026"), "USUARIO"));
        }

        System.out.println("[semilla] usuarios ana/dgt2026 (ADMIN) y luis/dgt2026 (USUARIO)");
        repositorio.findAll().forEach(u ->
                System.out.printf("[semilla] %-5s %-8s %s%n", u.getNombre(), u.getRol(), u.getClaveHash()));
    }
}
```

**Se corre:** `./mvnw spring-boot:run`

**En consola:**

```
[semilla] usuarios ana/dgt2026 (ADMIN) y luis/dgt2026 (USUARIO)
[semilla] ana   ADMIN    $argon2id$v=19$m=16384,t=2,p=1$4/hkUnFEeRln96MWwoMq5Q$JmwNO6b+wwI93/ntep1WjGkEH08BGkHamGvglmffpHQ
[semilla] luis  USUARIO  $argon2id$v=19$m=16384,t=2,p=1$eFPVYfr596eNE7tc1P6nrw$QjSIE1stNTlgVFptCXgWBNlKY/smK01UcD4dn0lsw54
```

**Aquí se para y se mira.** Las dos claves son `dgt2026` — la misma palabra— y los dos hashes no
se parecen en nada. Se lee el formato en voz alta:

```
$argon2id$ v=19 $ m=16384,t=2,p=1 $ 4/hkUnFEeRln96MWwoMq5Q $ JmwNO6b+wwI93/...
    │        │          │                    │                      │
    │        │          │                    └── la sal             └── el hash
    │        │          └── 16 MB de memoria, 2 pasadas, 1 hilo
    │        └── versión del algoritmo
    └── la variante: Argon2**id**, la recomendada
```

**El `m=16384` es el número que hay que señalar**: son 16 MB de RAM por cada cálculo. Es lo que
BCrypt no tiene, y lo que hace que una GPU —miles de núcleos, poquísima memoria cada uno— no pueda
paralelizar el ataque.

La guarda `if (repositorio.count() == 0)` envuelve **sólo la siembra**, no el método entero: por
eso los hashes también salen en la segunda corrida, cuando la tabla ya está poblada. La base
sobrevive al apagado —vive en el directorio `.datos-pg/`— y los hashes son los mismos. Quien
quiera verlos nacer distintos otra vez borra `.datos-pg/` y vuelve a arrancar.

**Y se abre DBeaver** con `localhost:55440`, base `postgres`, usuario `postgres`, sin clave:

```sql
SELECT nombre, rol, clave_hash FROM usuario;
```

Ver la fila en la tabla es lo que cierra el argumento: **ni el administrador de la base puede
saber cuál era la contraseña.**

---

## Paso 4 · El login y el token

**Se explica:** ya hay usuarios y ya hay puerta. Falta la llave. Se hace login **una vez** y se
recibe un **JWT**: un papel firmado que dice quién eres y hasta cuándo vale. En las siguientes
peticiones se manda el papel, y el servidor no vuelve a consultar la base.

**Se pega:** archivo **nuevo** `practica/src/main/java/cl/dgt/seguridad/services/ServicioDeTokens.java` — el archivo entero.

```java
package cl.dgt.seguridad.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.stream.Collectors;

@Service
public class ServicioDeTokens {

    private final Duration vigencia;

    private final JwtEncoder codificador;

    public ServicioDeTokens(JwtEncoder codificador,
                            @Value("${lab09.jwt.vigencia-segundos:1800}") long vigenciaSegundos) {
        this.codificador = codificador;
        this.vigencia = Duration.ofSeconds(vigenciaSegundos);
    }

    public String emitir(Authentication autenticacion) {
        Instant ahora = Instant.now();

        String roles = autenticacion.getAuthorities().stream()
                .map(a -> a.getAuthority())
                .filter(a -> a.startsWith("ROLE_"))
                .collect(Collectors.joining(" "));

        JwtClaimsSet cuerpo = JwtClaimsSet.builder()
                .issuer("lab09")
                .issuedAt(ahora)
                .expiresAt(ahora.plus(vigencia))
                .subject(autenticacion.getName())
                .claim("scope", roles)
                .build();

        return codificador.encode(JwtEncoderParameters.from(cuerpo)).getTokenValue();
    }
}
```

**Se pega:** archivo **nuevo** `practica/src/main/java/cl/dgt/seguridad/controllers/AuthController.java` — el archivo entero.

```java
package cl.dgt.seguridad.controllers;

import cl.dgt.seguridad.services.ServicioDeTokens;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthenticationManager gestor;
    private final ServicioDeTokens tokens;

    public AuthController(AuthenticationManager gestor, ServicioDeTokens tokens) {
        this.gestor = gestor;
        this.tokens = tokens;
    }

    public record Credenciales(String usuario, String clave) {
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody Credenciales credenciales) {
        try {
            Authentication autenticado = gestor.authenticate(
                    new UsernamePasswordAuthenticationToken(credenciales.usuario(), credenciales.clave()));

            return ResponseEntity.ok(Map.of("token", tokens.emitir(autenticado)));
        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Credenciales inválidas"));
        }
    }
}
```

**Se corre:**

```bash
TOKEN=$(curl -s -X POST http://localhost:8095/auth/login \
        -H 'Content-Type: application/json' \
        -d '{"usuario":"ana","clave":"dgt2026"}' | sed 's/.*"token":"\([^"]*\)".*/\1/')
```

**En consola — las tres pruebas del paso:**

```
$ curl -o /dev/null -w "%{http_code}\n" http://localhost:8095/productos
401                                              ← sin token

$ curl -o /dev/null -w "%{http_code}\n" -H "Authorization: Bearer $TOKEN" \
       http://localhost:8095/productos
200                                              ← con token

$ curl -o /dev/null -w "%{http_code}\n" -H "Authorization: Bearer ${TOKEN%.*}.FIRMAFALSA" \
       http://localhost:8095/productos
401                                              ← token manipulado
```

Y la clave equivocada, que no llega ni a emitir token:

```
$ curl -o /dev/null -w "%{http_code}\n" -X POST http://localhost:8095/auth/login \
       -H 'Content-Type: application/json' -d '{"usuario":"ana","clave":"otra"}'
401
```

**Lo que hay que notar:** nadie consultó la base en esas peticiones. El token **se basta a sí
mismo** —trae quién eres y va firmado—, y por eso `sessionCreationPolicy(STATELESS)`: no hay
sesión que guardar. Ese es el motivo por el que este esquema escala a muchos servidores: no hay
nada compartido entre ellos.

### La demostración del token vencido

Un token no se puede revocar: el servidor no lo guarda en ninguna parte, sólo verifica su firma.
Si a alguien se le despide a las 10:00 y su token vale hasta las 18:00, sigue entrando hasta las
18:00. Por eso vencen pronto — 30 minutos es el **techo del daño**.

Y se puede ver, sin tocar una línea de Java. **Se pega:** en `practica/src/main/resources/application.yml`,
se baja la vigencia:

```yaml
lab09:
  jwt:
    vigencia-segundos: 40
```

Se reinicia, se pide un token, se espera y se vuelve a usar:

```
$ curl -o /dev/null -w "%{http_code}\n" -H "Authorization: Bearer $TOKEN" \
       http://localhost:8095/productos
200        ← recién emitido

  (a los 32 segundos)
200        ← todavía vale

  (a los 40 segundos)
$ curl -o /dev/null -w "%{http_code}\n" -H "Authorization: Bearer $TOKEN" \
       http://localhost:8095/productos
401        ← vencido
```

**Cuarenta segundos, los que dice el yml.** Nadie escribió la comprobación de la fecha: la hace el
`JwtDecoder` solo, leyendo el claim `exp`.

> **Y aquí hay una línea de `SeguridadConfig` que hay que señalar**, porque de fábrica esto NO
> saldría así:
>
> ```java
>         decodificador.setJwtValidator(new DelegatingOAuth2TokenValidator<>(
>                 new JwtTimestampValidator(Duration.ZERO)));
> ```
>
> `NimbusJwtDecoder` viene con una **tolerancia de reloj de 60 segundos**: acepta un token vencido
> hace menos de un minuto. Sin poner esa tolerancia a cero, un token de 40 segundos viviría **100**
> —40 de vigencia más 60 de gracia— y la demo no cuadraría con lo que dice el yml.
>
> **En producción esa tolerancia se deja puesta**, y por una razón buena: los relojes de dos
> servidores nunca coinciden al segundo, y rechazar un token por medio segundo de deriva es peor
> que aceptarlo por medio minuto. Aquí se pone a cero **sólo** para que el laboratorio pueda
> enseñar la expiración sin pedirle a la sala que espere minuto y medio.

**Se vuelve a dejar la vigencia en `1800`** antes de seguir, o el resto del laboratorio obliga a
hacer login cada dos curl.

---

## Paso 5 · La sorpresa del día, y 401 no es 403

**Se explica:** hasta aquí, todo el que entra puede hacer todo. Falta la segunda mitad:
**autenticación** es quién eres; **autorización** es qué te toca. La regla ya está escrita desde
el paso 2 —la línea de `hasAuthority`—; falta la ruta que protege.

**Se pega (1 de 2):** en `practica/src/main/java/cl/dgt/seguridad/controllers/ProductoController.java`,
**arriba**, junto a `import java.util.List;`.

```java
import java.util.Map;
```

**Se pega (2 de 2):** en el mismo archivo, **antes de la llave que cierra la clase** — es un
endpoint nuevo, no reemplaza a ninguno.

```java
    @GetMapping("/administracion")
    public Map<String, String> administracion() {
        return Map.of("mensaje", "Sólo un ADMIN ve esto");
    }
```

**Se corre:** se piden los dos tokens, uno de cada usuario.

```bash
ANA=$(curl -s -X POST http://localhost:8095/auth/login -H 'Content-Type: application/json' \
      -d '{"usuario":"ana","clave":"dgt2026"}'  | sed 's/.*"token":"\([^"]*\)".*/\1/')
LUIS=$(curl -s -X POST http://localhost:8095/auth/login -H 'Content-Type: application/json' \
      -d '{"usuario":"luis","clave":"dgt2026"}' | sed 's/.*"token":"\([^"]*\)".*/\1/')
```

### El momento del laboratorio

Antes de cruzar las peticiones se **abren los dos tokens**. Tienen tres partes separadas por
puntos, y las dos primeras se decodifican **sin la clave, sin permiso, sin nada** — es Base64, no
cifrado:

```
$ echo $ANA | cut -d. -f2 | base64 -d
{"iss":"lab09","sub":"ana","exp":1787071711,"iat":1787069911,"scope":"ROLE_ADMIN"}

$ echo $LUIS | cut -d. -f2 | base64 -d
{"iss":"lab09","sub":"luis","exp":1787071713,"iat":1787069913,"scope":"ROLE_USUARIO"}
```

(También sirve pegarlo en un decodificador de JWT en el navegador. Sale lo mismo.)

**Está a la vista.** Y aquí va la frase del día:

> **Un JWT no oculta. Garantiza.**
>
> La tercera parte —la firma— no impide **leerlo**: impide **cambiarlo**. Cualquiera puede ver
> que este token dice `ana` y `ROLE_ADMIN`; nadie puede fabricar uno que diga `ROLE_ADMIN` sin la
> clave del servidor. Es lo que se acaba de comprobar en el paso 4 con `FIRMAFALSA`.

Corolario, y es lo que hay que llevarse: **nunca se mete en un token nada que no pueda leer el
que lo lleva.** Ni un RUT que no sea suyo, ni un correo de otro, ni una nota interna. Va firmado,
no va tapado.

Y de paso, ahí está el `scope` que la cadena va a leer dentro de un segundo: `ROLE_ADMIN` en uno,
`ROLE_USUARIO` en el otro. **Ojo al nombre**, que es la trampa del día: el lector de tokens de
Spring le antepone `SCOPE_` a lo que encuentre en ese claim, así que la autoridad se llama
`SCOPE_ROLE_ADMIN` — que es exactamente lo que pide la línea del paso 2. Cuando los prefijos no
cuadran, el error no dice nada: dice 403.

### La matriz

**Se corre:** las cinco peticiones del README, en orden.

**En consola:**

```
$ curl -o /dev/null -w "%{http_code}\n" http://localhost:8095/productos
401

$ curl -o /dev/null -w "%{http_code}\n" -H "Authorization: Bearer $ANA"  http://localhost:8095/productos
200

$ curl -o /dev/null -w "%{http_code}\n" -H "Authorization: Bearer $LUIS" http://localhost:8095/productos
200

$ curl -o /dev/null -w "%{http_code}\n" -H "Authorization: Bearer $ANA"  http://localhost:8095/productos/administracion
200

$ curl -o /dev/null -w "%{http_code}\n" -H "Authorization: Bearer $LUIS" http://localhost:8095/productos/administracion
403
```

**Las dos últimas líneas son el paso.** Se comparan con la primera:

| | qué dice el servidor |
|---|---|
| **401** Unauthorized | «**No sé quién eres.** Identifícate y vuelve» |
| **403** Forbidden | «**Sé perfectamente quién eres**, y esto no te toca. Volver con el mismo token no va a servir» |

El nombre del 401 es un error histórico del estándar: dice *Unauthorized* y significa *no
autenticado*. Se nombra en voz alta porque genera confusión el resto de la carrera.

Y hay una consecuencia práctica que conviene dejar dicha: **un 403 se le devuelve a alguien que
ya sabemos quién es.** Eso vale para el registro de auditoría —hay un nombre que anotar— y para
el mensaje: a un 401 no se le cuenta nada; a un 403 se le puede decir qué rol haría falta.

**Lo que hay que notar:** el endpoint `/administracion` **no comprueba el rol**. No hay un
`if (esAdmin)`. La regla vive en `SeguridadConfig`, y el método de luis no llega a ejecutarse
nunca: el filtro cortó antes.

---

## Al terminar

`practica/` responde exactamente la misma matriz que `solucion/`. Si algo no cuadra, `solucion/`
está al lado para comparar archivo por archivo.

Lo que hay que poder decir con las propias palabras:

> La API está cerrada por defecto y se abre a propósito. La contraseña no se guarda: se guarda un
> hash con sal, distinto para cada persona aunque la clave sea la misma. El token dice quién soy,
> va firmado y **cualquiera puede leerlo**. Y 401 y 403 no son lo mismo: uno es no sé quién eres,
> el otro es no te toca.

### Lo que siembra este lab

La API ya sabe quién llama y qué puede hacer. Y hoy, sin decirlo, se volvió **dependiente de algo
que no controla**.

Míresenlo: cada login consulta la base de datos. Si la base tarda, el login tarda. Si la base no
responde, el login **no responde** — y el hilo que atendía esa petición se queda ahí, esperando,
sin devolverle nada a nadie.

Hoy no se notó porque la base vive dentro del mismo proceso y contesta en microsegundos. En
producción no es así: la base está en otra máquina, y al lado hay un servicio de pagos, uno de
notificaciones y uno de terceros que a veces se cae.

> **La pregunta que abre el Lab 10** — nuestra aplicación está bien escrita y bien protegida.
> ¿Qué le pasa cuando **la de al lado** deja de contestar?

La respuesta por defecto, y es peor de lo que parece: espera. Para siempre.
