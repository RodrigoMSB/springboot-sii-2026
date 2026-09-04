# INFORME-SPEC-049 · La clave de ana y luis pasa a `dgt2026`

**Ejecuta:** mocito · **Rama:** `spec-049-clave-dgt2026` · **Fecha:** 3 de septiembre de 2026
**Origen:** SPEC-049 del PO. Se dicta el Lab 09 el viernes 4.

---

## 0 · Resumen

Un renombre de dato, hecho en **nueve archivos versionados** más tres copias de `instructor/`.

**Lo medido, con la aplicación en marcha:**

```
[semilla] usuarios ana/dgt2026 (ADMIN) y luis/dgt2026 (USUARIO)

401  sin token
200  token de ana   -> /productos
200  token de luis  -> /productos
200  token de ana   -> /productos/administracion
403  token de luis  -> /productos/administracion
```

**La matriz que pedía la spec, clavada: 401, 200, 200, 200, 403.** Y la clave vieja ya no entra:
`{"usuario":"ana","clave":"secreta"}` devuelve **401**.

**Dos cosas que la spec no listaba y sí hacían falta.** Están en §3, y las dos son el mismo defecto
que la spec persigue: el guion impreso del Lab 09 (§3.1) y los hashes precalculados del proyecto
final (§3.2).

---

## 1 · Dónde se cambió

| archivo | qué |
|---|---|
| `labs/lab-09-seguridad/solucion/…/soporte/SembradorDeUsuarios.java` | el `encode` y el `println` |
| `labs/lab-09-seguridad/PASOS.md` | los `curl` de login, la salida del sembrador y la frase del paso |
| `labs/lab-09-seguridad/README.md` | la tabla de usuarios |
| `docs/guias/fuente/guia-lab-09-seguridad.md` + su PDF | **no estaba en la spec** — §3.1 |
| `proyecto-final/base`, `ejemplo` y `instructor/solucion-referencia` · migración | los **hashes**, regenerados — §3.2 |
| `proyecto-final/README.md`, `brief/requerimientos.md`, `rubrica/rubrica.md` | las tablas y los `curl` |
| `instructor/` del Lab 09: `SembradorDeUsuarios.java`, `AuthController.java` | las mismas frases, en su versión comentada |

**El gateway del lab de microservicios no se tocó**, y la spec ya lo preveía («solo si siembra los
mismos usuarios»): siembra **`carolina` y `jefatura`**, que son otras personas — y su clave **ya
era `dgt2026`**. El cambio, más que romper esa coherencia, la completa: ahora el arco entero usa la
misma convención.

---

## 2 · Lo que NO se tocó, y una trampa

`lab09.jwt.secreto` y `LAB09_JWT_SECRETO` **están intactos**, que es justo el punto de la spec:

```
labs/lab-09-seguridad/PASOS.md:139           @Value("${lab09.jwt.secreto}")
…/solucion/src/main/resources/application.yml:23   secreto: ${LAB09_JWT_SECRETO:clave-de-laboratorio-…}
…/practica/src/main/resources/application.yml:18   secreto: ${LAB09_JWT_SECRETO:clave-de-laboratorio-…}
```

**Y la trampa, que se sorteó a mano.** Hay una línea que dice `secreta` y **no** es la contraseña:

```
instructor/…/config/SeguridadConfig.java:280
//  POR QUÉ · Firma simetrica con clave secreta (HMAC), y no un par de claves
```

Ahí `secreta` es el adjetivo de «clave secreta compartida», el secreto del HMAC. **Un
buscar-y-reemplazar ciego se la habría llevado por delante** y habría dejado escrito «clave dgt2026
compartida». Se revisó una por una.

Los nombres `ana` y `luis` y sus roles siguen igual.

---

## 3 · Lo que la spec no listaba y sí hacía falta

### 3.1 · El guion impreso decía la clave vieja

La verificación de la spec es `grep -rn secreta labs proyecto-final`, y **el guion no vive ahí**:
vive en `docs/guias/fuente/`. Tenía la clave en tres sitios y los hashes de ejemplo en otros tres.

Si se deja, **el PDF que el alumno tiene impreso delante dice `secreta` mientras la aplicación pide
`dgt2026`** — que es exactamente el tipo de contradicción que esta SPEC existe para quitar. Se
corrigió y se regeneró el PDF.

**De paso, un aviso sobre `generar-guias.py`:** regenera los catorce PDF, así que el `git status`
salió con trece PDF tocados sin que su contenido cambiara. Se devolvieron con `git checkout`. En el
commit va **sólo el del Lab 09**.

### 3.2 · El proyecto final siembra con hashes precalculados

Esto es el hallazgo que la spec pedía buscar en el otro frente y estaba aquí. El Lab 09 siembra
pasando la clave por el codificador en el arranque, así que cambiar la palabra basta. **El proyecto
final no**: sus tres copias de `V1__esquema_y_datos.sql` traen el hash escrito a mano.

```sql
insert into usuario (nombre, clave_hash, rol) values
  ('ana',  '$argon2id$v=19$m=16384,t=2,p=1$…', 'FISCALIZADOR'),
```

Cambiar sólo los documentos habría dejado el brief prometiendo `dgt2026` y la base validando
`secreta`. **Los dos hashes se regeneraron** con el mismo codificador del curso —
`Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8()`, contra el classpath offline del Lab 09 —
y se comprobó lo que había que comprobar antes de pegarlos:

```
largo 97 / 97
distintos (la sal): true
ana valida: true · luis valida: true
clave vieja ya no entra: true
```

**Sales distintas a propósito**, porque la migración enseña eso mismo dos líneas más arriba: «los
dos hashes son distintos: es la sal».

---

## 4 · Validación

### 4.1 · El Lab 09, con la aplicación arrancada

`Started Lab09Application in 3.39 seconds`, y la semilla:

```
[semilla] usuarios ana/dgt2026 (ADMIN) y luis/dgt2026 (USUARIO)
[semilla] ana   ADMIN    $argon2id$v=19$m=16384,t=2,p=1$4/hkUnFEeRln96MWwoMq5Q$JmwNO6b+wwI93/ntep1WjGkEH08BGkHamGvglmffpHQ
[semilla] luis  USUARIO  $argon2id$v=19$m=16384,t=2,p=1$eFPVYfr596eNE7tc1P6nrw$QjSIE1stNTlgVFptCXgWBNlKY/smK01UcD4dn0lsw54
```

**Esa salida es la que ahora está pegada en el guion**, hashes incluidos, junto con el diagrama que
despieza el formato. Antes eran los de `secreta`; ahora son los que la clase va a ver de verdad.

La matriz de cinco, en orden:

```
401   ·   200   ·   200   ·   200   ·   403
```

Y el control que la spec no pedía pero cierra el asunto: **la clave vieja devuelve 401**.

### 4.2 · El proyecto final

```
base     Tests run: 1, Failures: 0, Errors: 0    BUILD SUCCESS
ejemplo  Tests run: 4, Failures: 0, Errors: 0    BUILD SUCCESS
```

Y con el ejemplo en marcha, contra los **hashes regenerados de verdad**:

```
login de ana con dgt2026   ->  200
login de ana con secreta   ->  401

1 sin token             ->  401
2 token CONTRIBUYENTE   ->  403
3 código inexistente    ->  404
4 el caso bueno         ->  200   {"codigo":"SCL-CEN","nombre":"Santiago Centro",…
```

> **Una corrección propia, para que conste.** La primera vez corrí contra el ejemplo las rutas del
> **encargo** (`/consolidados/{rut}`) y me salieron tres 404. No era un defecto: el ejemplo resuelve
> el problema **paralelo** —oficinas, `/resumenes/{codigo}`— precisamente para no regalar el
> encargo. La matriz de arriba es la del ejemplo, con su ruta.

### 4.3 · Los seis verificadores

```
pasos-copiables     [OK] 14 guion(es) verificado(s)
guion-vs-practica   [OK] Todo lo que los guiones prometen sobre `practica/` es verdad
instructor          [OK] `instructor/` está al día con `solucion/`
temario             VEREDICTO: las 5 verificaciones PASAN
demo-docker         [OK] la demostración dice el mismo código que el laboratorio
generar-guias       [OK] 78 bloque(s) · 0 líneas que la solución no tiene
```

---

## 5 · El `grep` de verificación

La spec pide que `grep -rn secreta labs proyecto-final` devuelva sólo `lab09.jwt.secreto`,
`LAB09_JWT_SECRETO` y sus equivalentes. **Devuelve una línea, y no es ninguna de esas:**

```
labs/lab-09-seguridad/instructor/…/config/SeguridadConfig.java:280
//  POR QUÉ · Firma simetrica con clave secreta (HMAC), y no un par de claves
```

**Y es correcto que no salgan las otras: `secreta` no es subcadena de `secreto`.** El patrón del
enunciado no podía encontrarlas nunca. Se comprobaron aparte y siguen enteras — §2.

Con `-i` y la raíz común el resultado es el mismo: la propiedad y la variable, intactas; ni una
credencial.

---

## 6 · Nota de entrega

Cambian los hashes de una migración de Flyway ya aplicada, así que **quien tenga un `.datos-pg` de
antes verá un error de *checksum mismatch*.** Se arregla borrando la carpeta:

```bash
rm -rf .datos-pg
```

Aquí se hizo en los tres proyectos antes de medir. **Conviene decirlo en la sala** el viernes, por
si alguien arrastra datos de una sesión anterior.

## 7 · Anotado, no tocado

**El respaldo privado tiene tres carpetas huérfanas** de la SPEC-039: `labs/lab-12-tareas`,
`labs/lab-13-empaquetado` y `labs/lab-14-microservicios`. El material vivo se respaldó bien —244
archivos en 15 carpetas, con el Lab 09 dentro—, pero el respaldo conserva además los nombres
viejos. **No se borró nada**: es el repositorio privado del PO y esta SPEC no lo autoriza.
