# `archivo/` — material que ya no se dicta

Aquí viven, **enteros y sin tocar**, los laboratorios que salieron del curso. No se borraron: se
apartaron, porque un material que costó escribirse vale más archivado que perdido en un tag.

| | por qué no se dicta |
|---|---|
| `lab-12-tareas/` | El calendario se cerró en los labs 08 a 11 más la demostración con Docker (SPEC-038) |
| `lab-13-empaquetado/` | Su tema —empaquetar y desplegar— lo cubre ahora el proyecto final, que se entrega con su imagen OCI |

**Nada de aquí entra en la maleta ni en el CI.** El job `labs` recorre `labs proyecto-final`, y
`archivo/` no está en esa lista — la exclusión es **estructural**, no una excepción escrita en
alguna parte. Estos proyectos **no se compilan, no se verifican y pueden quedar desactualizados**
respecto del resto del repositorio: quedan como estaban el día que salieron.

Lo que **no** está aquí, y dónde encontrarlo:

- **`examen-huecos/`** — sólo en el tag `material-v1.11.1`. Era un instrumento de evaluación, no un
  laboratorio, y la evaluación de conocimientos lo reemplazó.
- **El lab 14 antiguo** (`lab-14-la-dgt-se-parte-en-pedazos`) — en `material-v0.8.0`. No hace falta
  archivarlo: su versión viva es `labs/lab-microservicios/` y la demostración con Docker.
- **El arco antiguo completo** (labs 07 a 14 de la primera versión del curso, el tronco
  `dgt-tramites-api`) — en los tags `material-v0.4.0` a `material-v0.8.0`. Ver `ESTADO.md` §1.a.

```bash
# recuperar cualquier cosa que no esté en esta carpeta
git show material-v1.11.1:examen-huecos/README.md
git checkout material-v0.8.0 -- dgt-tramites-api/
```

**Los números de los labs archivados no se reutilizan.** Un lab nuevo tomaría el siguiente libre,
para que las referencias del historial y de los informes sigan queriendo decir lo que decían.
