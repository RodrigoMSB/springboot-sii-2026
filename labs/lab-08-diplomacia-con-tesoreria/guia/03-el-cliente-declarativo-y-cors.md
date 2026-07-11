# Guía 03 · El cliente declarativo y el endurecimiento (TODO_3 y TODO_4)

## TODO_3 — La escalera colapsada (`@HttpExchange`)

Llamar a TESO a mano funciona, pero reparte la forma de la llamada por el código. Colápsalo
en un cliente DECLARATIVO detrás del puerto:

```java
@HttpExchange
public interface TesoreriaClient {
    @GetExchange("/pagos/{referencia}")
    ConfirmacionPago confirmar(@PathVariable String referencia);
}
```

Y se respalda con el `RestClient` que ya trae los timeouts (los de TODO_1):

```java
HttpServiceProxyFactory proxy = HttpServiceProxyFactory
        .builderFor(RestClientAdapter.create(restClient)).build();
return proxy.createClient(TesoreriaClient.class);
```

Es un **refactor**: no cambia el comportamiento, así que la MISMA suite del enunciado sigue
verde antes y después. Eso no se promete, se demuestra (lo sabes desde el Lab 05). La
aplicación depende del puerto `TesoreriaPort`, no del transporte.

## TODO_4 — El endurecimiento (la hora de M9)

CORS **nominal**, nunca `*`:

```java
.cors(Customizer.withDefaults())
// y el bean:
cfg.setAllowedOrigins(List.of("https://mi.dgt.cl"));   // solo el front de Mi DGT
```

Un preflight desde `https://mi.dgt.cl` recibe permiso; desde `https://intruso.cl`, no. Poner
`*` es rendirse. Y las cabeceras de endurecimiento:

```java
.headers(h -> h.contentSecurityPolicy(csp -> csp.policyDirectives("default-src 'self'")))
```

El test `E4` mide las dos cosas: el preflight nominal pasa, el intruso no, y la cabecera CSP
está presente.

> **CSRF** sigue deshabilitado, y con razón: esta API es sin estado (la credencial viaja en
> el header, no en una cookie de sesión). Deshabilitarlo en una app con cookies SÍ sería
> negligencia. El criterio es la clave, no la casilla.

## Cierra

```bash
./bin/90-validar.sh --dir starter
```

Los tres tests del enunciado en verde, la suite entera intacta tras el refactor, y la API que
sobrevive a un TESO caído. Completa `plantillas/reporte-entregable.md` y entrégalo.
