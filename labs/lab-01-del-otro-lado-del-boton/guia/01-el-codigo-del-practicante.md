# Guía 01 · El código del practicante

**Acto 1 · El choque** 💥

El practicante se fue el viernes. Dejó el proyecto funcionando: compila, arranca, responde.
Carolina te pide que lo revises antes de subirlo.

## Lo que ves

```bash
cd starter
cat src/main/resources/application.yml
```

Está limpio. No hay ninguna contraseña. Bien.

## Lo que Carolina ve

```bash
git log --oneline -- src/main/resources/application.yml
```

```
e213fa0 quita credenciales del yml
dc70ed6 ajustes de conexión
```

Dos commits. Uno dice "quita credenciales". El otro se llama *ajustes de conexión*.

```bash
git show dc70ed6 -- src/main/resources/application.yml
```

```yaml
+  datasource:
+    url: jdbc:postgresql://prod-db.dgt.gob.cl:5432/dgt
+    username: dgt_app
+    password: Dgt2026Pr0d!
```

Ahí está. En la pantalla. Proyectada.

> *«Alguien ya la "borró". Muéstrame que entiendes por qué eso no arregló nada.»*

## Responde antes de seguir

| Pregunta | Tu respuesta |
|---|---|
| ¿La contraseña está en el archivo de hoy? | |
| ¿Está en el repositorio? | |
| ¿Cuántas personas clonaron este repo desde el viernes? | |
| Si borras el commit, ¿desaparece del portátil de quien clonó ayer? | |

<details>
<summary>💡 La respuesta que duele</summary>

Un `git commit` que borra una línea no borra la línea: la deja en el commit anterior. En
cada clon. En cada fork. En la caché de tu proveedor de git. En el portátil del practicante
que ya no trabaja aquí.

**El archivo está limpio. El secreto no.**

Y ojo con el reflejo: *"pues reescribo el historial"*. Eso cambia todos los SHA, rompe cada
rama abierta, y obliga a que dieciocho personas vuelvan a clonar. La credencial ya está en
la máquina de quien clonó ayer, de todas formas. La respuesta profesional es otra, y llega
en la Guía 03.
</details>

> La contraseña es **de utilería**: el host `prod-db.dgt.gob.cl` no existe y la clave no
> abre nada. Está plantada para que aprendas a reconocer el problema.

➡️ Siguiente: [`02-el-parche-que-funciona.md`](02-el-parche-que-funciona.md)
