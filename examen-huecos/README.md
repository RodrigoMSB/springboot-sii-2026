# Examen de completar huecos · la DGT

**Esto no es un laboratorio, y tampoco es el proyecto final.** Es un examen corto sobre lo que se
construyó en los labs 01 a 09.

**No se escribe desde cero.** La aplicación ya está: compila, arranca, tiene su base de datos con
datos dentro y su login funcionando. Le faltan **doce trozos**, y esos doce son el examen.

---

## Lo que hay que saber antes de empezar

| | |
|---|---|
| **Doce huecos** | marcados en el código con un recuadro `HUECO NN` |
| **Cada hueco tiene su test** | doce tests, uno por hueco. Los corres tú, cuando quieras |
| **La nota** | huecos resueltos sobre doce. Sin ponderaciones y sin letra chica |
| **No hay guion** | cada hueco dice **qué** tiene que hacer. El **cómo** es lo que se evalúa |
| **Los labs se pueden abrir** | son tuyos. Esto no es una prueba de memoria |

**Los huecos son independientes.** Resolver el 7 no exige haber resuelto el 3. Puedes ir en el
orden que quieras, y dejar uno sin hacer no arrastra a los demás — está comprobado hueco a hueco.

---

## Cómo se corre

```bash
cd base
./mvnw test          # los doce tests. En Windows: mvnw.cmd test
```

Al terminar imprime la tabla, que es la nota:

```
=============================================================================
 HUECOS
-----------------------------------------------------------------------------
  [OK]    H-01 · la relacion: SCL-01 tiene 5 solicitudes
  [FALTA] H-02 · derivada por comuna: Santiago tiene 2 oficinas
  ...
-----------------------------------------------------------------------------
 RESUELTOS: 1 de 12
=============================================================================
```

Una corrida completa tarda **unos siete segundos**. Córrela todas las veces que quieras: es la
forma de saber si vas bien **antes** de entregar, y para eso está.

Y si quieres verlo funcionando de verdad:

```bash
./mvnw spring-boot:run                       # escucha en el 8109
curl -X POST localhost:8109/auth/login -H 'Content-Type: application/json' \
     -d '{"usuario":"ana","clave":"secreta"}'
```

| usuario | clave | rol |
|---|---|---|
| `ana` | `secreta` | **FISCALIZADOR** |
| `luis` | `secreta` | **CONTRIBUYENTE** |

---

## Qué viene resuelto

Todo lo que **no** se evalúa hoy:

- El esquema y los datos: tres oficinas —dos en la misma comuna, una **sin solicitudes**— y nueve
  solicitudes repartidas en dos años.
- La entidad `Solicitud` entera, con su lado de la relación.
- **La autenticación completa**: `POST /auth/login`, el JWT y la cadena de filtros.
- La infraestructura: el PostgreSQL embebido y las dos guardas de arranque.
- Los DTO `FichaOficina` y `SolicitudBreve`, las dos excepciones del dominio, y el endpoint
  `GET /oficinas/{codigo}/ficha`, que es el ejemplo resuelto que tienes al lado.

## Qué tienes que escribir tú

Los doce huecos. Son **48 líneas de código en total** — cuatro por hueco de media. Si estás
escribiendo mucho más que eso, probablemente te estés complicando.

| | Hueco | De qué lab viene |
|---|---|---|
| **01** | la relación entre `Oficina` y `Solicitud`, vista desde la oficina | 05 |
| **02** | una consulta derivada por un campo | 04 · 05 |
| **03** | una consulta derivada que cuenta | 04 · 06 |
| **04** | una consulta derivada que ordena | 04 · 06 |
| **05** | el DTO del resumen | 01 · 06 |
| **06** | la lista completa, en un DTO más escueto | 01 · 06 |
| **07** | la suma en el servicio | 02 · 06 |
| **08** | un número que no va escrito en el código, sino en la configuración | 02 · 09 |
| **09** | el POST que crea, y contesta como se contesta una creación | 01 |
| **10** | el 404 con cuerpo | 03 |
| **11** | el 400 que dice qué campo viene mal | 03 |
| **12** | quién puede ver qué | 09 |

---

## Qué se entrega

La carpeta `base/` con tus huecos resueltos. Nada más: no hay reporte que escribir.

**La nota es la última línea de `./mvnw test`.** La corres tú antes de entregar, así que no hay
sorpresas: sabes tu nota antes que nadie.

---

## Si algo se rompe y no es tu código

`../docs/troubleshooting.md` tiene la tabla, con números. Los dos que más salen:

- **«EL PUERTO 55446 YA ESTA OCUPADO»** — quedó vivo un PostgreSQL de una corrida anterior.
  El propio mensaje trae el comando para cerrarlo.
- **«ESTE MISMO PROYECTO YA ESTA CORRIENDO»** — lo tienes arrancado en otra terminal.
