# Guía 01 · La puerta que no existía

## La escena del crimen

```bash
./bin/start-lab.sh --dir starter --crimen
```

Dos golpes, los dos entran:

```
     1) curl anónimo (sin token) POST /tramites/1/folio   -> HTTP 201
     2) curl con token FABRICADO a mano (base64, sin firma) -> HTTP 200
```

El primero emite un folio **sin ser nadie**. El segundo usa un "token" que armaste tú con
`echo -n 'ladron:FUNCIONARIO' | base64` — y el starter te cree que eres funcionario.

## Por qué pasa

El starter trae dos cosas que parecen seguridad y no lo son:

1. **La puerta abierta.** `SeguridadConfig` hace `anyRequest().permitAll()`: nada está
   cerrado. Un `curl` anónimo llega hasta el fondo.
2. **El disfraz.** El "login" devuelve `base64(rut:rol)` (ni mira tu clave), y un filtro lo
   decodifica y te cree. Codificar no es cifrar, y cifrar no es firmar: cualquiera fabrica
   su credencial.

```java
// starter/SeguridadConfig — el crimen
.authorizeHttpRequests(reglas -> reglas.anyRequest().permitAll())
.addFilterBefore(new FiltroBase64Falso(), ...);   // "valida" un base64 sin firma
```

## Lo que Carolina no puede tolerar

> *«Acabo de emitir un folio desde la micro. Yo ni siquiera trabajo en emisión.»*

Los folios del Lab 06 son perfectos y **perfectamente disponibles para cualquiera**. La
puerta no existe. Este lab la construye — y el token que la abre no se cree, se **verifica**.

Sigue con [`02-cerrar-la-puerta.md`](02-cerrar-la-puerta.md).
