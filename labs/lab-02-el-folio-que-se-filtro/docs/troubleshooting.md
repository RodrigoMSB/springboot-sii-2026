# Troubleshooting · Lab 02

| # | Síntoma | Qué pasa | Qué haces |
|---|---|---|---|
| **L2-01** | El `90-validar.sh` dice "El demonio de Docker no responde" | Este lab prueba contra base real (Testcontainers) | Abre Docker Desktop (T-03 del Lab 00). Sin Docker no se puede validar. |
| **L2-02** | `T3.au02` falla en "pasa sobre producción" y no entiendo por qué | Escribiste AU-02 bien, pero tu controlador **todavía devuelve la entidad** | AU-02 te está cazando a ti. Haz el TODO_1 primero: devuelve el DTO. |
| **L2-03** | Escribí AU-02 con `haveRawReturnType` y pasa todo… menos T3 | Es la trampa del spike S-1: el genérico se le escapa a esa regla | Reescribe AU-02 con `dependOnClassesThat()`. Lee el Javadoc de `AU_02`. |
| **L2-04** | `T3` falla: "Expecting code to raise a throwable" | Tu regla no muerde al fixture: sigue siendo el cascarón, o está mal escrita | Un guardián que no caza a su violador no protege nada. Revisa que la regla apunte al patrón correcto. |
| **L2-05** | `T1` pasa el 404 pero falla `containsOnlyKeys` | Tu ficha lleva un campo de más (`id`, `puntaje`…) | La lista blanca es EXACTA: solo `rut` y `razonSocial`. |
| **L2-06** | Swagger UI da 404 en `/swagger-ui` | Falta la dependencia springdoc, o la URL correcta | La URL es `/swagger-ui/index.html`. La dependencia ya está en el `pom.xml`. |
| **L2-07** | Corriste `./mvnw verify` y aparecieron contenedores raros | Son de Testcontainers, de los tests de integración | Ver **T-11** del Lab 00. `99-destruir.sh` no los toca a propósito; te dice cómo limpiarlos. |
| **L2-08** | El puerto 8099 está ocupado | Algo escucha ahí | `./bin/start-lab.sh --puerto 8100`. El script te dice quién lo ocupa. |
