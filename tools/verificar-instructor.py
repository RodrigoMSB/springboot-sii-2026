#!/usr/bin/env python3
# =============================================================================
#  verificar-instructor.py — la carpeta que el CI no puede ver
# -----------------------------------------------------------------------------
#  `labs/*/instructor/` NO viaja en el repositorio (D-031-2: si viajara, el
#  alumno tendría la chuleta delante). El costo aceptado de esa decisión es que
#  **ningún job del CI la mira**, porque en el runner no existe. Durante meses
#  eso significó que no la miraba nadie.
#
#  La SPEC-040 destapó lo que crece en un sitio sin vigilancia: los seis
#  `instructor/pom.xml` de los labs 08 a 13 llevaban una regla decorativa de
#  guiones DENTRO de un comentario XML, y eso es ilegal:
#
#      Non-parseable POM .../lab-09-seguridad/instructor/pom.xml:
#      in comment after two dashes (--) next character must be > not -
#
#  Un `pom.xml` que no es XML válido lo marca en rojo cualquier editor, y quien
#  prepara la clase lo abre creyendo que el material está roto.
#
#  ESTO NO ES UN JOB DEL CI, Y NO PUEDE SERLO. Un job que corre donde los
#  archivos no existen siempre pasa: es el «gate decorativo» que el propio ADN
#  del curso castiga (P-05). Se corre a mano, y se corre en la máquina donde
#  `instructor/` sí está: la de quien prepara la sesión.
#
#  QUE COMPRUEBA:
#
#    1. Todo XML bajo `instructor/` **parsea**. Caza el `--` dentro de un
#       comentario y cualquier otra cosa que lo rompa.
#    2. Ningún comentario XML contiene `--`, aunque el parser de turno lo
#       tolere. La regla de la casa para las reglas decorativas en XML es `=`,
#       nunca `-`; y una bandera larga como `--offline` se escribe `-o` o se
#       nombra sin guiones.
#    3. `instructor/` está **al día con `solucion/`**: mismo juego de archivos
#       `.java`. Es lo que la SPEC-040 tuvo que arreglar a mano al renombrar
#       paquetes, y lo que se vuelve a romper en el siguiente renombre.
#    4. Cada `.java` declara el `package` que le toca por su ruta.
#    5. Cada `instructor/` tiene su `LEEME.md`.
#
#  SI NO HAY `instructor/` EN ESTE CLON, lo dice y sale en verde — pero lo dice,
#  con el número de carpetas encontradas. No declara sano lo que no ha mirado
#  (A-02), e imprime todo lo que cuenta (A-04).
#
#  Uso:  python3 tools/verificar-instructor.py
# =============================================================================
import pathlib
import re
import sys
import xml.etree.ElementTree as ET

RAIZ = pathlib.Path(__file__).resolve().parent.parent
COMENTARIO_XML = re.compile(r'<!--(.*?)-->', re.S)
# Un `pom.xml` no tiene DOCTYPE ni entidades propias. Se rechazan antes de
# parsear: `ElementTree` expande entidades internas, así que un documento con
# entidades anidadas lo haría reventar por memoria («billion laughs»). No se usa
# `defusedxml` porque el curso corre sin red y sin instalar nada (D-022-1), y
# para archivos que escribe el propio material esta guarda basta.
DECLARACION_PELIGROSA = re.compile(r'<!(DOCTYPE|ENTITY)\b', re.I)


def paquete_esperado(java):
    """El `package` que corresponde a la ruta: lo que hay entre `java/` y el archivo."""
    partes = java.parts
    if 'java' not in partes:
        return None
    i = len(partes) - 1 - partes[::-1].index('java')
    return '.'.join(partes[i + 1:-1])


def main():
    raices = sorted([p for p in RAIZ.glob('labs/*/instructor') if p.is_dir()] +
                    [p for p in RAIZ.glob('proyecto-final/instructor') if p.is_dir()])

    if not raices:
        print('No hay ninguna carpeta `instructor/` en este clon.')
        print('Es lo normal: no viaja en el repositorio (D-031-2). La genera quien')
        print('prepara la sesión, a partir de `solucion/`.')
        print('\n[OK] 0 carpetas encontradas · 0 comprobadas · nada que verificar.')
        return 0

    fallos = []
    n_xml = n_java = n_leeme = 0

    print(f'Carpetas `instructor/` encontradas: {len(raices)}\n')

    for inst in raices:
        lab = inst.parent
        rel = inst.relative_to(RAIZ)
        # `proyecto-final/instructor` no es la copia explicada de un lab: es el
        # material del examen (la solución de referencia y la guía de defensa).
        # No le tocan ni el `LEEME.md` ni el espejo con `solucion/`.
        es_lab = inst.parent.name.startswith('lab-')
        loc = []

        # --- 1 y 2 · los XML ------------------------------------------------
        for xml in sorted(inst.rglob('*.xml')):
            if 'target' in xml.parts:
                continue
            n_xml += 1
            texto = xml.read_text(encoding='utf-8', errors='replace')
            for cuerpo in COMENTARIO_XML.findall(texto):
                if '--' in cuerpo:
                    linea = texto[:texto.find(cuerpo)].count('\n') + 1
                    fallos.append(f'{xml.relative_to(RAIZ)}:~{linea} · un comentario XML '
                                  f'contiene «--», que es ilegal. Reglas con «=»; una '
                                  f'bandera larga, con su forma corta')
                    break
            peligro = DECLARACION_PELIGROSA.search(texto)
            if peligro:
                fallos.append(f'{xml.relative_to(RAIZ)} · declara `<!{peligro.group(1)}`, '
                              f'que este material no usa y que no se va a parsear')
                continue
            try:
                ET.fromstring(texto)
            except ET.ParseError as e:
                fallos.append(f'{xml.relative_to(RAIZ)} · no parsea como XML: {e}')

        # --- 3 · al día con solucion/ ---------------------------------------
        # El lab 14 reparte sus fuentes en cuatro servicios; el glob los cubre.
        sol = lab / 'solucion'
        if sol.is_dir():
            def javas(base):
                return {p.relative_to(base).as_posix()
                        for p in base.rglob('*.java') if 'target' not in p.parts}
            en_sol, en_ins = javas(sol), javas(inst)
            faltan = sorted(en_sol - en_ins)
            sobran = sorted(en_ins - en_sol)
            for f in faltan:
                fallos.append(f'{rel} · `solucion/` tiene un archivo que `instructor/` no: {f}')
            for f in sobran:
                fallos.append(f'{rel} · `instructor/` tiene un archivo que `solucion/` ya no: {f}')
            loc.append(f'{len(en_ins)}/{len(en_sol)} .java')

        # --- 4 · el package coincide con la ruta -----------------------------
        for java in sorted(inst.rglob('*.java')):
            if 'target' in java.parts:
                continue
            n_java += 1
            esperado = paquete_esperado(java)
            m = re.search(r'^package\s+([\w.]+);', java.read_text(encoding='utf-8',
                                                                 errors='replace'), re.M)
            real = m.group(1) if m else None
            if esperado and real != esperado:
                fallos.append(f'{java.relative_to(RAIZ)} · declara `{real}` y por su ruta '
                              f'le toca `{esperado}`')

        # --- 5 · el documento que abre la carpeta -----------------------------
        # En un lab es `LEEME.md`; en el examen son `NOTA.md` y `guia-defensa.md`.
        doc = (inst / 'LEEME.md') if es_lab else next(iter(sorted(inst.glob('*.md'))), None)
        if doc is not None and doc.is_file():
            n_leeme += 1
        else:
            fallos.append(f'{rel} · no tiene ' +
                          ('`LEEME.md`' if es_lab else 'ningún documento `.md` que la abra'))

        malos = [f for f in fallos if str(rel) in f]
        print(f'  [{"ERROR" if malos else "OK"}] {rel}  ·  ' +
              ' · '.join(loc or ['examen: sólo XML y paquetes']))

    print(f'\nComprobados: {n_xml} XML · {n_java} .java · '
          f'{n_leeme}/{len(raices)} carpetas con su documento de entrada')

    if fallos:
        print(f'\n[ERROR] {len(fallos)} problema(s) en `instructor/`:\n')
        for f in fallos:
            print(f'  · {f}')
        return 1

    print('\n[OK] `instructor/` está al día con `solucion/` y su XML es válido.')
    return 0


if __name__ == '__main__':
    sys.exit(main())
