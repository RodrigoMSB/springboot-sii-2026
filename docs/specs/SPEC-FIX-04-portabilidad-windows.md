# SPEC-FIX-04 · Blindar la portabilidad a Windows

| Campo | Valor |
|---|---|
| ID | SPEC-FIX-04 |
| Naturaleza | **Corrección de material ejecutado** — riesgo de bloqueo total en Windows |
| Título | `.gitattributes` con normalización de finales de línea + verificación en CI |
| Autor | Arquitecto |
| Ordena | PO (Rodrigo) |
| Rama / Tag | `fix/portabilidad-windows` → merge a `main` → tag `material-v0.2.2` |
| Estado | LISTA PARA EJECUCIÓN |

> **Instrucción de ejecución (mocito):** guardar en
> `docs/specs/SPEC-FIX-04-portabilidad-windows.md` y commitear en la rama antes de
> ejecutar. **No se toca el contenido de ningún script ni de ningún documento**: esto es
> configuración de repositorio y una verificación nueva en CI.

---

## §1 · El problema

**El repositorio no tiene `.gitattributes`.** Verificado: el archivo no existe en la raíz.

Sin él, Git decide los finales de línea según la configuración local de cada quien. En
Windows, la instalación por defecto de Git para Windows viene con
`core.autocrlf=true`, lo que significa que **al clonar convierte los archivos de texto a
CRLF** — incluidos los `.sh`.

Un script de bash con finales CRLF **no se ejecuta**. El intérprete lee `#!/usr/bin/env
bash\r` y busca un programa llamado `bash\r`, que no existe. El error que ve el alumno es
este:

```
bash: ./bin/00-verificar.sh: /usr/bin/env: bad interpreter: No such file or directory
```

**Es el peor error posible para el día 1:** no dice qué pasa, no dice cómo arreglarlo, no
apunta a nada accionable, y buscarlo en internet devuelve diez respuestas distintas. Un
alumno de Windows con esto queda bloqueado antes de escribir una línea de Java — y no por
culpa suya.

**Estado actual verificado:** los ~90 scripts del repo están hoy con finales LF correctos
y ninguno tiene CRLF. El problema no es lo que está guardado: **es lo que Git le entrega
al alumno al clonar.** Sin `.gitattributes`, esa conversión depende de la configuración de
cada máquina, que no controlamos.

## §2 · La corrección

### 2.1 · Crear `.gitattributes` en la raíz del repositorio

```gitattributes
# ─────────────────────────────────────────────────────────────────────────────
#  Normalización de finales de línea.
#
#  Sin esto, Git para Windows (core.autocrlf=true por defecto) convierte los
#  .sh a CRLF al clonar, y bash deja de poder ejecutarlos:
#      bash: ./bin/00-verificar.sh: /usr/bin/env: bad interpreter
#  El material del alumno tiene que llegar ejecutable a las tres plataformas.
# ─────────────────────────────────────────────────────────────────────────────

# Por defecto: Git normaliza a LF en el repositorio.
* text=auto eol=lf

# Scripts de shell: SIEMPRE LF, incluso en Windows (los corre Git Bash).
*.sh      text eol=lf
mvnw      text eol=lf

# Scripts nativos de Windows: SIEMPRE CRLF (cmd.exe los necesita así).
*.cmd     text eol=crlf
*.bat     text eol=crlf
mvnw.cmd  text eol=crlf

# Binarios: que Git no toque nada.
*.png     binary
*.jpg     binary
*.jpeg    binary
*.gif     binary
*.pdf     binary
*.zip     binary
*.jar     binary
*.docx    binary
*.pptx    binary
*.xlsx    binary
*.class   binary
```

**Nota sobre `mvnw` y `mvnw.cmd`:** el wrapper de Maven trae los dos, y cada uno necesita
su terminación. Están declarados por nombre exacto porque no tienen extensión (`mvnw`) o
porque el patrón general no bastaría.

### 2.2 · Re-normalizar lo ya versionado

Crear el archivo no reescribe lo que ya está en el índice de Git. Tras crearlo, ejecutar:

```bash
git add --renormalize .
git status
```

Si `git status` no muestra cambios, significa que todo estaba ya en LF y solo queda el
`.gitattributes` nuevo — que es el resultado esperado según la verificación previa.
**Si mostrara archivos modificados, revísalos antes de commitear** y repórtalo: significa
que algo estaba guardado con CRLF y hay que entender qué.

### 2.3 · Verificación en CI (que esto no se degrade)

Agregar al job `labs-sh` de `.github/workflows/material-ci.yml` un paso que falle si
aparece un `.sh` con CRLF. Debe correr en **ubuntu** (donde `file` está disponible) y ser
explícito en su mensaje:

```yaml
      - name: CRLF · ningún script puede tener finales de Windows
        if: matrix.os == 'ubuntu-latest'
        run: |
          malos=$(find labs -name '*.sh' -exec file {} \; | grep -c CRLF || true)
          if [ "$malos" -gt 0 ]; then
            echo "[ERROR] Hay $malos script(s) con finales CRLF."
            echo "        En Windows, bash no puede ejecutarlos:"
            echo "        'bad interpreter: No such file or directory'."
            echo "        Revisa el .gitattributes y ejecuta: git add --renormalize ."
            find labs -name '*.sh' -exec file {} \; | grep CRLF
            exit 1
          fi
          echo "[OK] Los scripts tienen finales LF."
```

Ajusta la ruta y la indentación a la estructura real del workflow. Si el job `labs-sh`
tiene una guarda de existencia de `labs/`, respétala.

## §3 · Documentar el riesgo para el alumno de Windows

En `labs/lab-00-estacion-base/docs/troubleshooting.md`, agregar una fila con el error
literal, porque aunque el `.gitattributes` lo previene, un alumno puede tener una
configuración local rara o haber clonado antes de esta corrección:

| Código | Síntoma | Causa | Solución |
|---|---|---|---|
| T-NN | `bad interpreter: No such file or directory` al correr cualquier script | El repositorio se clonó convirtiendo los finales de línea a formato Windows | Volver a clonar el repositorio, o ejecutar en la raíz: `git config core.autocrlf false` y luego `git rm --cached -r . && git reset --hard` |

Usa el número que corresponda en la numeración existente del archivo, y respeta su
formato de tabla.

## §4 · Lo que NO se toca

- **Ningún script.** Su contenido está correcto; el problema es de configuración del repo.
- **Ninguna teoría, guía ni documento del alumno**, salvo la fila de troubleshooting del §3.
- **El job `app` del CI**, que sigue solo en ubuntu por la limitación documentada de
  Docker en runners Windows. **Eso no cambia en esta SPEC.**

## §5 · Verificación (citada en el reporte)

1. `.gitattributes` existe en la raíz y su contenido coincide con §2.1.
2. `git add --renormalize .` ejecutado; el `git status` resultante **citado**. Si movió
   archivos, explicar cuáles y por qué.
3. **Prueba negativa — que el gate muerda:** crea un `.sh` de prueba con finales CRLF
   (`printf 'echo hola\r\n' > /tmp/prueba.sh`, o `unix2dos` sobre una copia), colócalo
   bajo `labs/` temporalmente, corre el chequeo del §2.3 en local y **cita que falla**.
   Bórralo después. Un gate sin prueba de que muerde es un adorno.
4. Tras el push: el job `labs-sh` **verde** con el paso nuevo. Run citado.
5. La fila de troubleshooting agregada, con su número correcto.
6. `ESTADO.md` y `decisiones.md` con su fila:

| Fecha | Decisión | Razón |
|---|---|---|
| (fecha) | Se agrega `.gitattributes` con normalización de finales de línea (LF para `.sh`, CRLF para `.cmd`) y un chequeo de CRLF en el job `labs-sh` del CI. | Sin él, Git para Windows convierte los scripts a CRLF al clonar y bash deja de ejecutarlos con un error incomprensible («bad interpreter»). Es un bloqueo total del alumno de Windows en el día 1, y no depende de nada que él haya hecho mal. |

## §6 · Criterios de aceptación

- [ ] SPEC-FIX-04 commiteada antes de sus cambios, en rama propia.
- [ ] `.gitattributes` en la raíz según §2.1.
- [ ] `git add --renormalize .` ejecutado y su resultado citado.
- [ ] Chequeo de CRLF en el CI, **con su prueba negativa citada** (§5.3).
- [ ] Fila de troubleshooting agregada al Lab 00.
- [ ] Ningún script ni documento modificado más allá de lo anterior
      (`git diff --stat` citado).
- [ ] CI verde, run citado.
- [ ] `ESTADO.md` y bitácora al día.
- [ ] Commits `SPEC-FIX-04:`; PR a main; tag `material-v0.2.2`.

## §7 · Reporte

Las seis verificaciones de §5, `git diff --stat`, `git log --oneline`, discrepancias y
hallazgos.

**Y un encargo aparte, de diagnóstico, que no bloquea esta SPEC:** revisa si hay algún
otro punto donde el material asuma un sistema operativo. Ya verifiqué y están limpios los
bashismos de bash 4+, los comandos GNU-only (`sed -i`, `date -d`, `stat -c`, `timeout`),
las rutas absolutas y el manejo de `lsof` (que tiene fallback a `curl` para Git Bash).
**Si encuentras algo más que se me haya pasado, repórtalo** — no lo corrijas en esta SPEC.
