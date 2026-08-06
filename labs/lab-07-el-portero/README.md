# Lab 07 · El portero

> *«Acabo de emitir un folio desde la micro. Con el celular. Yo ni siquiera trabajo en
> emisión. El Lab 06 hizo los folios únicos e idempotentes — perfectos. Perfectamente
> disponibles para cualquier ser humano con internet.»*
>
> — **Carolina Espinoza**

La semana pasada los folios quedaron impecables: únicos, secuenciales, idempotentes. Y
completamente abiertos. No hay quién pregunte en la puerta *«¿y usted quién es?»*. Hoy la
puerta tiene portero: la API se cierra por defecto, hay login real, y el token que abre no
se cree — **se verifica su firma**.

**Sesión 7** · Módulo 9 (seguridad: autenticación y autorización, 3,0 h) · **3 horas**

---

## Lo que vas a hacer

| | Actividad | Tiempo |
|---|---|---|
| 🔪 | **La escena del crimen** — emitir un folio sin ser nadie, y fabricar un "token" | 10 min |
| 📚 | Teoría: la cadena de filtros, BCrypt, la anatomía del JWT, firma simétrica/asimétrica | ~40 min |
| ☕ | Descanso | 10 min |
| 🔧 | **Laboratorio: los TODOs** | ~110 min |
| ✅ | Validar, reporte, y la siembra del Lab 08 | 10 min |

## Los TODOs

| | Qué | Dónde | ≈ |
|---|---|---|---|
| **TODO_1** | Cerrar la puerta: `SecurityFilterChain` con **denegar por defecto** | `config/SeguridadConfig.java` | 15 min |
| **TODO_2** | El login real: `UserDetailsService` + BCrypt, y emitir el JWT **firmado** | `application/`, `config/` | 15 min |
| **TODO_3** | El validador que no cree: Resource Server valida la firma; roles → authorities | `config/SeguridadConfig.java` | 15 min |
| **TODO_4** | Cada rol a su puerta: `@PreAuthorize` — emitir es solo de FUNCIONARIO | `web/controller/` | 15 min |

Los cuatro tests del enunciado —`E1` a `E4`— no los escribes: los lees. `E1` prueba la
puerta cerrada, `E2` el login, `E3` que un token adulterado muere, `E4` que cada rol abre
solo su puerta. El mismo criterio juzga tu trabajo y a la solución.

> **La clave de firma no vive en el repo.** Llega por `DGT_JWT_SECRET`, con un default de
> **utilería solo en dev** (como la credencial del `compose.yaml`). En prod la variable es
> obligatoria: sin ella, la app no arranca. Es la misma doctrina del Lab 01, cobrada de nuevo.

## El pre-vuelo — el crimen en tus manos

```bash
cd ..
./bin/start-lab.sh --dir starter --crimen
```

Verás dos golpes: un `curl` **anónimo** emitiendo un folio, y un "token" **fabricado a
mano** (`echo -n 'ladron:FUNCIONARIO' | base64`) que el starter acepta sin chistar. En la
solución, los dos reciben **401**. Un token sin firma es una opinión.

Este lab **necesita Docker**. Cuando termines: `./bin/90-validar.sh --dir starter`.

## Para el Instructor

`./bin/91-e2e.sh` antes de la sesión. El guion está en [`INSTRUCTOR.md`](INSTRUCTOR.md).
La siembra del Lab 08 —Tesorería que se demora— está en `TEORIA.md §12`.
