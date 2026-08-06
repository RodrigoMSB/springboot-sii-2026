# Guía 01 · El servicio que nos mata

## La escena del crimen

```bash
./bin/start-lab.sh --dir starter --teso-lento 30000
```

Verás 12 confirmaciones de pago dispararse contra un TESO que tarda 30 s, y luego el golpe:

```
     GET /tramites -> HTTP 000 en 8.0 s   (--max-time agotado: colgado)
```

`GET /tramites` no tiene NADA que ver con pagos. Y está muerto. La API entera, secuestrada
por un servicio ajeno que se puso lento.

## Por qué pasa

El cliente de TESO del starter es ingenuo: un `RestClient` **sin timeout**.

```java
this.restClient = RestClient.builder().baseUrl(baseUrl).build();  // sin timeouts: espera infinita
```

Cada confirmación de pago abre una transacción (`@Transactional`), toma una conexión de la
base, y se queda esperando a TESO **30 segundos** con esa conexión en la mano. Con el pool de
conexiones lleno de esperas, el listado —que necesita una conexión para consultar— no
consigue ninguna. Rehén tras rehén, hasta que no queda nada libre.

> **La lección estructural:** sin timeout, el hilo (y la conexión) de la petición es un
> rehén; con el pool lleno de rehenes, la app está secuestrada por su dependencia.

## Lo que Carolina exige

> *«TESO se cayó a las 9. A las 9:02 nosotros también — y nosotros no tenemos nada malo.»*

El problema de OTRO servicio se volvió NUESTRA caída. Hoy hacemos que no vuelva a pasar: un
timeout corto y dirigido, una mala noticia elegante, y el cliente hecho como se debe.

Sigue con [`02-el-timeout-y-la-mala-noticia.md`](02-el-timeout-y-la-mala-noticia.md).
