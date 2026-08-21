# Pasos · Lab 09 · Seguridad

Seis pasos. Se trabaja en `practica/`, en vivo. Después de cada paso se reinicia la aplicación y
se prueba antes de seguir.

```bash
cd practica
./mvnw spring-boot:run
```

Escucha en el **8095** (`solucion/`, en el 8096). La base va en el **55440**.

Lo que llega hecho: la API de productos, la entidad `Usuario`, su repositorio y la tabla. Lo que
se escribe hoy vive en dos carpetas que llegan vacías:

```
seguridad/   →  SeguridadConfig, UsuarioDetailsService, ServicioDeTokens
services/    →  SembradorDeUsuarios
controllers/ →  AuthController, y dos endpoints en ProductoController
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

## Paso 2 · La cadena de filtros

**Se explica:** ahora se toma el control. Una petición, antes de llegar a un controller, atraviesa
una **cadena de filtros**; ahí se decide si sigue o se corta. Se declara en una clase de
configuración, y lo primero que se dice es qué es público y qué no.

Se empieza con un usuario **en memoria**, escrito a mano, porque el asunto de este paso son las
rutas, no de dónde salen los usuarios. Eso es el paso 3.

**Se pega:** archivo **nuevo** `practica/src/main/java/cl/dgt/seguridad/seguridad/SeguridadConfig.java` — el archivo entero.

<!-- pasos:intermedio · los pasos 3, 4, 5 y 6 lo van cambiando -->

```java
package cl.dgt.seguridad.seguridad;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SeguridadConfig {

    @Bean
    SecurityFilterChain cadena(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(rutas -> rutas
                        .requestMatchers("/productos").permitAll()
                        .anyRequest().authenticated())
                .httpBasic(Customizer.withDefaults())
                .build();
    }

    @Bean
    UserDetailsService usuariosEnMemoria() {
        return new InMemoryUserDetailsManager(
                User.withUsername("ana").password("{noop}secreta").roles("ADMIN").build());
    }
}
```

**En consola:**

```
$ curl -o /dev/null -w "%{http_code}\n" http://localhost:8095/productos
200                                    ← pública: la declaramos permitAll

$ curl -o /dev/null -w "%{http_code}\n" http://localhost:8095/productos/2
401                                    ← no la declaramos: anyRequest().authenticated()

$ curl -o /dev/null -w "%{http_code}\n" -u ana:secreta http://localhost:8095/productos/2
200
```

Y **desapareció** la línea de la contraseña generada: en cuanto hay un `UserDetailsService`
propio, Spring deja de inventarse uno.

**Lo que hay que notar:** el orden de las reglas importa y se lee de arriba abajo — gana la
primera que encaja. Y `{noop}` delante de la clave significa «esta contraseña está en texto
plano». Spring **obliga** a decirlo: sin ese prefijo, falla con
`There is no PasswordEncoder mapped for the id "null"`. Es una molestia deliberada, y el paso
siguiente la resuelve de verdad.

---

## Paso 3 · BCrypt, y por qué una clave no se guarda

**Se explica:** el `{noop}` de arriba es exactamente lo que no se hace nunca. Y el problema no es
sólo que alguien lea la base: es que **la gente repite contraseñas**, así que una filtración aquí
compromete el correo y el banco de esa persona.

La salida no es cifrar —cifrar es reversible— sino guardar un **hash**: una función de un solo
sentido. Al hacer login se vuelve a calcular y se comparan los hashes; la clave original **no
existe en ninguna parte del sistema**.

BCrypt añade dos cosas sobre un hash normal:

- **Sal**: un valor aleatorio distinto por contraseña, que se guarda dentro del propio hash. Por
  eso dos personas con la misma clave tienen hashes distintos, y por eso no sirve una tabla de
  hashes precalculada.
- **Costo**: es **lento a propósito**. El `10` del hash significa 2¹⁰ vueltas. Al que hace login
  le cuesta unos milisegundos; al que prueba millones de claves por segundo le arruina el negocio.

**Se pega:** archivo **nuevo** `practica/src/main/java/cl/dgt/seguridad/seguridad/UsuarioDetailsService.java` — el archivo entero.
Es el puente entre la tabla y Spring Security.

```java
package cl.dgt.seguridad.seguridad;

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

y `services/SembradorDeUsuarios.java`, que crea los dos usuarios la primera vez:

```java
package cl.dgt.seguridad.services;

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
        if (repositorio.count() > 0) {
            return;
        }
        repositorio.save(new Usuario("ana", codificador.encode("secreta"), "ADMIN"));
        repositorio.save(new Usuario("luis", codificador.encode("secreta"), "USUARIO"));

        System.out.println("[semilla] usuarios ana/secreta (ADMIN) y luis/secreta (USUARIO)");
        repositorio.findAll().forEach(u ->
                System.out.printf("[semilla] %-5s %-8s %s%n", u.getNombre(), u.getRol(), u.getClaveHash()));
    }
}
```

En `SeguridadConfig`, **se borra** el `usuariosEnMemoria()` y se pone el codificador:

```java
    @Bean
    PasswordEncoder codificadorDeClaves() {
        return new BCryptPasswordEncoder();
    }
```

**En consola:**

```
[semilla] ana   ADMIN    $2a$10$z2RuZ6YymqMEOa9haqcN2.m1B31q1pL1oGfPzUUaYNbi43Lor3Lsy
[semilla] luis  USUARIO  $2a$10$RBxoDtr9qH5oevKTWzwRaeKxD0Oc2pXrQtT07ayvBXO2h09HtqiN2
```

**Aquí se para y se mira.** Las dos claves son `secreta` — la misma palabra— y los dos hashes no
se parecen en nada. Se lee el formato en voz alta:

```
$2a$ 10 $ z2RuZ6YymqMEOa9haqcN2. m1B31q1pL1oGfPzUUaYNbi43Lor3Lsy
 │    │   └── sal (22 car.)      └── el hash propiamente tal
 │    └── costo: 2^10 vueltas
 └── algoritmo BCrypt
```

**Y se abre DBeaver** con `localhost:55440`, base `postgres`, usuario `postgres`, sin clave:

```sql
SELECT nombre, rol, clave_hash FROM usuario;
```

Ver la fila en la tabla es lo que cierra el argumento: **ni el administrador de la base puede
saber cuál era la contraseña.**

---

## Paso 4 · El token, y la sorpresa del día

**Se explica:** con HTTP Basic, cada petición manda usuario y clave, y el servidor consulta la
base y calcula BCrypt **cada vez**. Es lento y obliga a que la clave viaje una y otra vez.

La alternativa: se hace login **una vez** y se recibe un **JWT** — un papel firmado que dice quién
eres y hasta cuándo vale. En las siguientes peticiones se manda el papel.

**Se pega:** archivo **nuevo** `practica/src/main/java/cl/dgt/seguridad/seguridad/ServicioDeTokens.java` — el archivo entero.

```java
package cl.dgt.seguridad.seguridad;

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

    private static final Duration VIGENCIA = Duration.ofMinutes(30);

    private final JwtEncoder codificador;

    public ServicioDeTokens(JwtEncoder codificador) {
        this.codificador = codificador;
    }

    public String emitir(Authentication autenticacion) {
        Instant ahora = Instant.now();

        String roles = autenticacion.getAuthorities().stream()
                .map(a -> a.getAuthority())
                .filter(a -> a.startsWith("ROLE_"))
                .collect(Collectors.joining(" "));

        JwtClaimsSet cuerpo = JwtClaimsSet.builder()
                .issuer("lab08")
                .issuedAt(ahora)
                .expiresAt(ahora.plus(VIGENCIA))
                .subject(autenticacion.getName())
                .claim("scope", roles)
                .build();

        return codificador.encode(JwtEncoderParameters.from(cuerpo)).getTokenValue();
    }
}
```

y `controllers/AuthController.java`:

```java
package cl.dgt.seguridad.controllers;

import cl.dgt.seguridad.seguridad.ServicioDeTokens;
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

y en `SeguridadConfig`, las piezas que hacen falta: el `AuthenticationManager`, la clave de firma,
y `/auth/login` como ruta pública.

```java
    @Value("${lab08.jwt.secreto}")
    private String secreto;

    private SecretKeySpec clave() {
        return new SecretKeySpec(secreto.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
    }

    @Bean
    JwtEncoder codificadorDeTokens() {
        return NimbusJwtEncoder.withSecretKey(clave()).build();
    }

    @Bean
    AuthenticationManager gestorDeAutenticacion(AuthenticationConfiguration configuracion) throws Exception {
        return configuracion.getAuthenticationManager();
    }
```

> **Aviso, porque muerde:** hay que usar `NimbusJwtEncoder.withSecretKey(...)`. Con el
> constructor genérico, el encoder intenta firmar con RS256, la clave es HMAC, y falla con
> `An error occurred while attempting to encode the Jwt: Failed to select a JWK signing key`.

**Se corre:**

```bash
curl -X POST http://localhost:8095/auth/login \
     -H 'Content-Type: application/json' \
     -d '{"usuario":"ana","clave":"secreta"}'
```

**En consola:**

```json
{"token":"eyJraWQiOiI5ZHUzd2JRZnJYcU1DOXpSRUU4NzM4Q250cWhTWS01TnN2MXVtZHN6T0VZIiwidHlwIjoiSldUIiwiYWxnIjoiSFMyNTYifQ.eyJpc3MiOiJsYWIwOCIsInN1YiI6ImFuYSIsImV4cCI6MTc4NzA3MTcxMSwiaWF0IjoxNzg3MDY5OTExLCJzY29wZSI6IlJPTEVfQURNSU4ifQ.7bK..."}
```

### El momento del laboratorio

Se mira el token: tiene **tres partes separadas por puntos**. Y se decodifican las dos primeras
**sin la clave, sin permiso, sin nada** — es Base64, no cifrado:

```
$ echo $TOKEN | cut -d. -f1 | base64 -d
{"kid":"9du3wbQfrXqMC9zREE8738CntqhSY-5Nsv1umdszOEY","typ":"JWT","alg":"HS256"}

$ echo $TOKEN | cut -d. -f2 | base64 -d
{"iss":"lab08","sub":"ana","exp":1787071711,"iat":1787069911,"scope":"ROLE_ADMIN"}
```

(También sirve pegarlo en un decodificador de JWT en el navegador. Sale lo mismo.)

**Está a la vista.** Y aquí va la frase del día:

> **Un JWT no oculta. Garantiza.**
>
> La tercera parte —la firma— no impide **leerlo**: impide **cambiarlo**. Cualquiera puede ver
> que este token dice `ana` y `ROLE_ADMIN`; nadie puede fabricar uno que diga `ROLE_ADMIN` sin la
> clave del servidor.

Corolario, y es lo que hay que llevarse: **nunca se mete en un token nada que no pueda leer el
que lo lleva.** Ni un RUT que no sea suyo, ni un correo de otro, ni una nota interna. Va firmado,
no va tapado.

---

## Paso 5 · Validar el token en cada petición

**Se explica:** el token ya se emite. Falta que el servidor lo **exija** y lo compruebe. Eso lo
hace un filtro, y no hay que escribirlo: se declara la aplicación como *resource server* y Spring
pone el filtro.

**Se pega:** en `practica/pom.xml`, **dentro de `<dependencies>`** — la segunda dependencia del
día.

```xml
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-oauth2-resource-server</artifactId>
    </dependency>
```

y en `SeguridadConfig`, se cambia `httpBasic` por el validador de tokens, se declara el
decodificador, y la sesión se apaga:

```java
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(rutas -> rutas
                        .requestMatchers("/auth/login").permitAll()
                        .anyRequest().authenticated())
                .oauth2ResourceServer(oauth -> oauth.jwt(Customizer.withDefaults()))
```

```java
    @Bean
    JwtDecoder decodificadorDeTokens() {
        return NimbusJwtDecoder.withSecretKey(clave()).build();
    }
```

Y en `ProductoController`, el endpoint que demuestra que el token llegó entero.

**Se pega (1 de 2):** en `practica/src/main/java/cl/dgt/seguridad/controllers/ProductoController.java`,
**arriba**, con los imports.

```java
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Map;
```

**Se pega (2 de 2):** en el mismo archivo, **antes de la llave que cierra la clase**.

```java
    @GetMapping("/quien-soy")
    public Map<String, Object> quienSoy(@AuthenticationPrincipal Jwt jwt) {
        return Map.of("usuario", jwt.getSubject(), "roles", jwt.getClaimAsString("scope"));
    }
```

**Se corre — las tres pruebas del paso:**

```
$ curl -o /dev/null -w "%{http_code}\n" http://localhost:8095/productos
401                                              ← sin token

$ curl -o /dev/null -w "%{http_code}\n" -H "Authorization: Bearer $TOKEN" \
       http://localhost:8095/productos
200                                              ← con token

$ curl -H "Authorization: Bearer $TOKEN" http://localhost:8095/productos/quien-soy
{"roles":"ROLE_ADMIN","usuario":"ana"}
```

Y la tercera, que es la que prueba la firma. Se le cambia el final al token:

```
$ curl -o /dev/null -w "%{http_code}\n" -H "Authorization: Bearer ${TOKEN%.*}.FIRMAFALSA" \
       http://localhost:8095/productos
401                                              ← token manipulado
```

**Lo que hay que notar:** nadie consultó la base en esas tres peticiones. El token **se basta a
sí mismo** — trae quién eres y va firmado—, y por eso `sessionCreationPolicy(STATELESS)`: no hay
sesión que guardar. Ese es el motivo por el que este esquema escala a muchos servidores: no hay
nada compartido entre ellos.

---

## Paso 6 · 401 no es 403

**Se explica:** hasta aquí, todo el que entra puede hacer todo. Falta la segunda mitad:
**autenticación** es quién eres; **autorización** es qué te toca.

**Se pega:** en `practica/src/main/java/cl/dgt/seguridad/controllers/ProductoController.java`, **antes de la llave que cierra la
clase** — es un endpoint nuevo, no reemplaza a ninguno.

```java
    @GetMapping("/administracion")
    public Map<String, String> administracion() {
        return Map.of("mensaje", "Sólo un ADMIN ve esto");
    }
```

**Se pega:** en `seguridad/SeguridadConfig.java`, **reemplazando la línea de `anyRequest()`** por
estas dos. El orden importa: la regla del ADMIN va **antes**, que si no nunca se llega a ella.

```java
                        .requestMatchers("/productos/administracion").hasRole("ADMIN")
                        .anyRequest().authenticated())
```

**Se corre:** se piden dos tokens, uno de cada usuario, y se cruzan las peticiones.

**En consola:**

```
GET /productos                  sin token      401
GET /productos                  ana ADMIN      200
GET /productos                  luis USUARIO   200
GET /productos/administracion   ana ADMIN      200
GET /productos/administracion   luis USUARIO   403
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
