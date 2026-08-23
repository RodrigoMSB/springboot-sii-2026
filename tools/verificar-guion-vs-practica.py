#!/usr/bin/env python3
# =============================================================================
#  verificar-guion-vs-practica.py — que el guion no mienta sobre `practica/`
# -----------------------------------------------------------------------------
#  Hermano del `verificar-pasos-copiables.py`. Aquel vigila que el CODIGO que el
#  guion promete siga estando en `solucion/`. Este vigila lo otro: que el ESTADO
#  DE PARTIDA que el guion promete sea el que `practica/` trae de verdad.
#
#  Nace de la SPEC-040, que al renombrar paquetes encontró esto en el Lab 11:
#
#      observabilidad/                ->  pasos 2 y 4 (llega vacía)
#
#  La carpeta no llegaba vacía: traía `CandadoLibre`, `MotorDePostgres` y
#  `PuertoLibre`. El instructor la abre en clase esperando el vacío y se
#  encuentra tres archivos. Es un defecto que SOLO aparece cuando alguien sigue
#  el guion literalmente — y por eso vivió sin que nadie lo viera.
#
#  QUE COMPRUEBA, y son cuatro promesas de distinta forma:
#
#    1. «Se pega: archivo **nuevo** `practica/X`» -> X **NO** existe en
#       `practica/` (si ya estuviera, se le pide al alumno crear algo que ya
#       tiene) y **SI** existe en `solucion/`.
#
#    2. «Se pega: en `practica/X`»                -> X **SI** existe en
#       `practica/`. No se puede pegar dentro de un archivo que no está.
#
#    3. «Se pega: `practica/X` — el archivo»      -> X **SI** existe. Es el caso
#       del archivo que llega declarado y vacío para que el alumno lo llene.
#
#    4. Carpetas que el guion o el README declaran **vacías** -> en `practica/`
#       no tienen ni un `.java`. Un `.gitkeep` sí: es lo que las sostiene en git.
#
#  DE DONDE LEE. De `git`, no del disco: `git ls-tree` del HEAD. Quien trabaja
#  en el repositorio suele tener los labs a medio hacer en su copia de trabajo
#  —el PO los recorre a mano, que es la prueba de aceptación— y esos archivos NO
#  son el material. Mirar el disco daría rojos falsos en cada clon donde alguien
#  esté siguiendo un lab.
#
#  QUE **NO** COMPRUEBA: que el guion esté completo, ni que lo que llega hecho
#  esté bien. Vigila que lo que el guion DICE de `practica/` sea verdad.
#
#  Uso:  python3 tools/verificar-guion-vs-practica.py
# =============================================================================
import pathlib
import re
import subprocess
import sys

RAIZ = pathlib.Path(__file__).resolve().parent.parent

# «Se pega ...» hasta el final de la línea. El «(1 de 3)» y demás caben dentro.
SE_PEGA = re.compile(r'\*\*Se pega[^*]*\*\*:?(.*)')
RUTA_PRACTICA = re.compile(r'`(practica/[^`]+)`')
# Una línea de árbol del tipo «programadas/   ->  pasos 1 y 2 (llega vacía)»
TREE_VACIA = re.compile(r'^\s*([A-Za-z0-9_/]+)/\s*(?:→|->)')
# Un `nombre/` entrecomillado dentro de una frase que habla de vacío
DIR_BACKTICK = re.compile(r'`([a-z][A-Za-z0-9_]*)/`')


def archivos_en_git(prefijo):
    """Los archivos que git tiene bajo `prefijo`, en HEAD. No mira el disco."""
    r = subprocess.run(['git', 'ls-tree', '-r', '--name-only', 'HEAD', prefijo],
                       cwd=RAIZ, capture_output=True, text=True)
    return set(r.stdout.split('\n')) - {''}


def main():
    labs = sorted(p for p in (RAIZ / 'labs').glob('lab-*') if p.is_dir())
    fallos = []
    n_nuevo = n_en = n_entero = n_vacias = 0

    print('Guiones comprobados contra lo que `practica/` trae de verdad (según git):\n')

    for lab in labs:
        bajo = f'labs/{lab.name}/'
        todos = archivos_en_git(bajo)
        practica = {f[len(bajo):] for f in todos if f.startswith(bajo + 'practica/')}
        solucion = {f[len(bajo):] for f in todos if f.startswith(bajo + 'solucion/')}

        loc_nuevo = loc_en = loc_entero = loc_vacias = 0

        for doc in ('PASOS.md', 'README.md'):
            ruta = lab / doc
            if not ruta.exists():
                continue
            lineas = ruta.read_text().split('\n')

            # Un archivo que un paso ANTERIOR manda crear no está en `practica/`
            # y no tiene por qué: a esa altura de la clase el alumno ya lo tiene
            # delante. Se lleva la cuenta en orden de lectura.
            creados = set()

            # --- promesas 1, 2 y 3: los «Se pega» que nombran un archivo ------
            for i, linea in enumerate(lineas, 1):
                m = SE_PEGA.search(linea)
                if not m:
                    continue
                # El encabezado puede seguir en las líneas de abajo: la ruta es
                # larga y a menudo cae en la siguiente. Se lee hasta la valla de
                # código o hasta la línea en blanco que cierra el párrafo.
                resto = m.group(1)
                for extra in lineas[i:i + 3]:
                    if extra.strip().startswith('```') or not extra.strip():
                        break
                    resto += ' ' + extra
                rm = RUTA_PRACTICA.search(resto)
                if not rm:
                    continue
                rel = rm.group(1)
                # La frase exacta, no la palabra suelta: «un endpoint nuevo» en la
                # prosa de continuación y el archivo `ProductoNuevoDto.java` también
                # dicen «nuevo», y no anuncian un archivo por crear.
                es_nuevo = bool(re.search(r'archivo\s+\*\*nuevo\*\*', resto, re.I))
                dentro_de = bool(re.search(r'\ben\s+`practica/', resto))

                if es_nuevo:
                    n_nuevo += 1; loc_nuevo += 1
                    if rel in practica:
                        fallos.append(f'{lab.name}/{doc}:{i} · dice «archivo nuevo» '
                                      f'pero `practica/` YA lo trae: {rel}')
                    creados.add(rel)
                    sol = rel.replace('practica/', 'solucion/', 1)
                    if sol not in solucion:
                        fallos.append(f'{lab.name}/{doc}:{i} · promete crear {rel} '
                                      f'pero `solucion/` no lo tiene: {sol}')
                elif dentro_de:
                    n_en += 1; loc_en += 1
                    if (rel not in practica and rel not in creados
                            and not rel.endswith(('pom.xml', '.yml', '.properties'))):
                        fallos.append(f'{lab.name}/{doc}:{i} · manda pegar DENTRO de un archivo '
                                      f'que `practica/` no trae y que ningún paso anterior '
                                      f'manda crear: {rel}')
                else:
                    n_entero += 1; loc_entero += 1
                    if (rel not in practica and rel not in creados
                            and not rel.endswith(('pom.xml', '.yml', '.properties'))):
                        fallos.append(f'{lab.name}/{doc}:{i} · nombra un archivo que '
                                      f'`practica/` no trae y que ningún paso anterior '
                                      f'manda crear: {rel}')

            # --- promesa 4: las carpetas declaradas vacías --------------------
            for i, linea in enumerate(lineas, 1):
                if not re.search(r'vac[íi][oa]s?\b', linea, re.I):
                    continue
                cands = set()
                mt = TREE_VACIA.match(linea)
                if mt and 'vac' in linea.lower():
                    cands.add(mt.group(1))
                cands |= set(DIR_BACKTICK.findall(linea))
                # `src/test/` y compañía llegan con la barra dentro del backtick
                cands |= {m.rstrip('/') for m in re.findall(r'`(src/test/?)`', linea)}
                for d in cands:
                    d = d.strip('/')
                    if not d or d in ('practica', 'solucion', 'instructor'):
                        continue
                    con_java = [f for f in practica
                                if re.search(rf'(^|/){re.escape(d)}/[^/]+\.java$', f)]
                    if not con_java:
                        # ¿existe siquiera la carpeta? si no, no es una promesa sobre ella
                        existe = any(re.search(rf'(^|/){re.escape(d)}/', f) for f in practica)
                        if existe:
                            n_vacias += 1; loc_vacias += 1
                        continue
                    n_vacias += 1; loc_vacias += 1
                    fallos.append(f'{lab.name}/{doc}:{i} · declara `{d}/` vacía y `practica/` '
                                  f'trae {len(con_java)}: ' +
                                  ', '.join(sorted(pathlib.Path(f).name for f in con_java)))

        print(f'  [{"OK" if not any(lab.name in f for f in fallos) else "ERROR"}] {lab.name}: '
              f'{loc_nuevo} nuevos · {loc_en} dentro-de · {loc_entero} archivo-entero · '
              f'{loc_vacias} carpetas-vacías')

    print(f'\nPromesas comprobadas: {n_nuevo} «archivo nuevo» · {n_en} «pegar dentro de» · '
          f'{n_entero} «el archivo entero» · {n_vacias} «llega vacía» '
          f'= {n_nuevo + n_en + n_entero + n_vacias} en total')

    if fallos:
        print(f'\n[ERROR] {len(fallos)} promesa(s) que `practica/` no cumple:\n')
        for f in fallos:
            print(f'  · {f}')
        print('\n  El guion describe un punto de partida que no es el que el alumno')
        print('  tiene delante. Se arregla en el guion o en `practica/`, según cuál')
        print('  de los dos esté equivocado — y eso lo decide quien escribió el paso.')
        return 1

    print('\n[OK] Todo lo que los guiones prometen sobre `practica/` es verdad.')
    return 0


if __name__ == '__main__':
    sys.exit(main())
