# Guía 02 · El parche que funciona

**Acto 2 · El parche bruto** 🔨

Una salida tentadora, y muy común: *"la contraseña no debería estar en el `application.yml`
general — la muevo al perfil de desarrollo"*.

```yaml
# application-dev.yml
spring:
  datasource:
    password: Dgt2026Pr0d!     # 🟡 FUNCIONA, pero…
```

Hazlo. En serio: escríbelo, arranca la aplicación, y comprueba que **funciona
perfectamente**. No es un ejercicio retórico; necesitas verlo verde para entender el resto.

```bash
cd starter && ./mvnw spring-boot:run
```

## Ahora contesta

| Pregunta | Tu respuesta |
|---|---|
| ¿La aplicación arranca? | |
| ¿`application-dev.yml` está versionado? (`git ls-files \| grep dev`) | |
| ¿Quién puede leer ese archivo? ¿Los 18 alumnos? ¿El CI? ¿Un fork? | |
| ¿Qué cambió, exactamente, respecto al viernes? | |
| Y el historial del `application.yml`, ¿se movió a algún sitio? | |

<details>
<summary>💡 El costo del parche</summary>

Nada cambió. La credencial sigue **versionada**, sigue viajando a cada clon, y ahora está en
dos sitios del historial en vez de uno. Movimos el problema de habitación.

Lo peor es que ahora *parece* resuelto. Un archivo llamado `application-dev.yml` suena
inofensivo, y nadie vuelve a mirarlo.

Este es el **DON'T estrella** del módulo: confundir *"lo saqué del archivo principal"* con
*"lo saqué del repositorio"*.

Hay un test que caza justo esto. Míralo:
`src/test/java/cl/dgt/tramites/enunciado/T1_SinCredencialesEnElRepoTest.java`. No busca
"una contraseña" (imposible): busca **exactamente** la que el practicante filtró. Corre
`./mvnw test -Dtest='**/enunciado/T1_*.java'` con tu parche puesto y mira qué te dice.
</details>

**Borra ese parche antes de seguir.** No lo dejes ahí "por ahora": así es como llega a
producción.

➡️ Siguiente: [`03-la-forma-correcta.md`](03-la-forma-correcta.md)
