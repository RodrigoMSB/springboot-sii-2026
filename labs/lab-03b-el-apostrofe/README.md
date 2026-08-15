# Lab 3.5 · El apóstrofe

**La persistencia, desde cero.** Vienes usando entidades desde el Lab 01 sin que nadie te haya
explicado qué son. Hoy toca: primero el dolor de hacerlo a mano, después la herramienta.

---

## El crimen

En `starter/` hay una clase heredada: `infrastructure/legacy/ReporteInternoLegacyDao.java`.
La escribió un practicante hace dos años, lee las observaciones internas de un contribuyente,
y **funciona**. Nadie la ha vuelto a mirar.

Levanta la aplicación y míralo tú mismo:

```bash
./bin/start-lab.sh                 # levanta starter/ en el puerto 8099
./bin/91-demo-inyeccion.sh         # dos consultas al mismo endpoint
```

La primera consulta pide las observaciones de Valentina Rojas y devuelve **2**. La segunda pide
lo mismo, con un apóstrofe de más en el RUT, y devuelve **5** — incluidas tres de otro
contribuyente, una de ellas marcada *«NO divulgar fuera del área»*.

No hubo hackeo. Se escribió una comilla en un campo de texto.

Ese DAO tiene cuatro defectos, y están marcados en el propio archivo como PECADO 1 a 4:
SQL concatenado, mapeo a mano columna por columna, una conexión que se filtra en cuanto algo
falla, y un `catch` vacío que convierte cualquier error en una lista vacía.

Al terminar la sesión, ese archivo no existe.

---

## La sesión

| | |
|---|---|
| 1 · El golpe | Ves la demo de arriba y lees el DAO entero. 40 líneas para leer una tabla. |
| 2 · La teoría | La expone el instructor. El mapa: tu clase ES la tabla. |
| 3 · Los cuatro TODOs | ~15 min cada uno. Todo lo que necesitas está junto al código. |
| 4 · El cierre | `./bin/90-validar.sh` y la demo otra vez, ahora sin filtración. |

---

## Los cuatro TODOs

Cada uno está explicado **en el archivo donde se hace**: qué escribir, la pista que te ahorra
media hora, y qué test lo verifica. No hay que cambiar de ventana.

| | Qué | Dónde | Lo verifica |
|---|---|---|---|
| **TODO_1** | Mapear la tabla a una entidad | `domain/entity/ObservacionInterna.java` | `E1_EntidadMapeadaIT` |
| **TODO_2** | El repositorio y su consulta derivada | `infrastructure/repository/ObservacionInternaRepository.java` | `E2_ConsultaDerivadaIT` |
| **TODO_3** | Migrar el servicio, y mirar el SQL | `application/ObservacionInternaService.java` | `E3_EndpointMigradoIT` |
| **TODO_4** | Enterrar el DAO e instalar el guardián | `test/.../arquitectura/ReglasDelApostrofe.java` | `E4_GuardianJdbcTest` |

Los tests del enunciado viven en `src/test/java/cl/dgt/tramites/enunciado/`. **Léelos**: son el
contrato de la sesión, y cada `@DisplayName` es un compromiso en español.

---

## Ver el SQL que escribiste sin escribirlo

En el TODO_3, arranca así:

```bash
./bin/start-lab.sh --ver-sql
```

Pide las observaciones y mira el log (`.estado/dgt.log`): ahí está el `SELECT` que Hibernate
generó por ti, con su `JOIN` y con un `?` donde va el RUT.

**Ese signo de pregunta es el final del apóstrofe.** El RUT ya no se pega a la consulta: viaja
aparte, como dato. Y un dato no se puede ejecutar.

---

## Comandos

```bash
./bin/start-lab.sh                       # levanta starter/ (--dir solucion para la otra)
./bin/start-lab.sh --ver-sql             # …y muestra el SQL generado
./bin/91-demo-inyeccion.sh               # el golpe: dos consultas, un apóstrofe de diferencia
./bin/90-validar.sh                      # ¿está tu trabajo listo?
./bin/99-destruir.sh                     # deja la máquina como estaba
./bin/95-recuperar.sh --solo-enunciado   # restaura los tests del enunciado si los tocaste
```

Desde `starter/` o `solucion/`, la suite completa:

```bash
./mvnw verify
```

---

## Después de la sesión

`PARA-EL-SABADO.md` — la profundización: los cuatro pecados uno por uno, por qué concatenar
convierte datos en código, la trampa del `PreparedStatement`, cómo leer el SQL generado, y lo
que JPA **no** resuelve solo (que es de lo que trata el Lab 04).

Si algo falla: `../lab-00-estacion-base/docs/troubleshooting.md` tiene una tabla con números.
Cita el número.
