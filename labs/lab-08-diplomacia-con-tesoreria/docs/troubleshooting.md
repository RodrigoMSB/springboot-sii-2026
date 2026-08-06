# Troubleshooting · Lab 08

## L8-01 · `E1` rojo: el pago no falla rápido
Falta el timeout en el cliente de TESO. Ponle un `requestFactory` con connect+read cortos al `RestClient`.

## L8-02 · `E2` rojo: no es 503 (es 500 o 200)
Si es 500: el adaptador no traduce `RestClientException` a `TesoreriaNoDisponibleException`, o falta el
`@ExceptionHandler`. Si es 200: no hay timeout, el pago esperó y avanzó el trámite.

## L8-03 · `E4` rojo: el preflight nominal no pasa / falta CSP
Falta `.cors(Customizer.withDefaults())` + el bean `CorsConfigurationSource`, o la cabecera CSP en `.headers(...)`.

## L8-04 · El puerto 8089 está ocupado (TESO no arranca)
TESO/WireMock usa el 8089 fijo. Si lo tienes ocupado, cámbialo en `compose.yaml` y en `application.yml`
(`dgt.teso.base-url`). Ver la nota en el compose.

## L8-05 · La demo `--teso-lento` no cuelga la API
Necesita el pool pequeño (Hikari max 5, en `application.yml`) y suficientes pagos concurrentes. Si tu máquina
es muy rápida, sube el número de pagos o baja el pool. En producción el pool es grande: el crimen tarda más,
pero es el mismo.

## L8-06 · Los tests fallan con "connection refused" tras el primer
Contenedores con ciclo de vida por-clase que se detienen mientras el contexto queda cacheado. Por eso
`BaseResilienciaIT` usa contenedores SINGLETON (bloque estático). No lo cambies a `@Container`.

## L8-07 · Docker no responde
Este lab necesita Docker: PostgreSQL y el WireMock de TESO. Ver T-03 del Lab 00.
