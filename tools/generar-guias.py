#!/usr/bin/env python3
# =============================================================================
#  generar-guias.py — las guías del alumno, en PDF (SPEC-044)
# -----------------------------------------------------------------------------
#  QUÉ HACE
#
#  Toma un fuente Markdown de `docs/guias/fuente/`, sustituye sus marcadores
#  `{{codigo ...}}` por el código EXTRAÍDO de `labs/*/solucion/`, y produce el
#  PDF con pandoc y xelatex.
#
#  POR QUÉ EL CÓDIGO NO SE TECLEA EN EL FUENTE
#
#  Es la regla de la SPEC-038, y aquí vale igual: un bloque tecleado aparte se
#  desincroniza en cuanto alguien toque `solucion/`, y nadie se entera hasta que
#  un alumno pega algo que no compila. Con extracción, tocar la solución y
#  regenerar deja la guía al día por construcción.
#
#  Y por eso hay un modo `--verificar` que NO genera nada: comprueba que todo lo
#  que la guía va a imprimir sale, byte a byte, de `solucion/`.
#
#  LOS MODOS DE EXTRACCIÓN
#
#    modo=entero                          el archivo completo
#    modo=metodo   nombre=saludo          un método, cerrando llaves
#    modo=entre    desde="X" hasta="Y"    lo que hay ENTRE dos anclas (sin ellas)
#    modo=clave    clave=spring           un bloque de YAML de primer nivel
#    modo=xml      contiene=spring-boot-starter-web
#                                         el <dependency> o <plugin> que lo contiene
#
#  Uso:
#    python3 tools/generar-guias.py                 # todas las guías
#    python3 tools/generar-guias.py --verificar     # solo comprueba, no escribe
#    python3 tools/generar-guias.py lab-00          # una
# =============================================================================
import io
import pathlib
import re
import shlex
import subprocess
import sys

RAIZ = pathlib.Path(__file__).resolve().parent.parent
FUENTE = RAIZ / 'docs' / 'guias' / 'fuente'
# El PDF va A LA CARPETA DEL LAB, junto al README y al PASOS: el alumno abre la
# carpeta de su laboratorio y lo tiene todo junto, sin tener que saber que existe
# `docs/`. Aquí solo se queda el fuente y el estilo, que son del generador.
ESTILO = RAIZ / 'docs' / 'guias' / 'estilo'
BUILD = RAIZ / 'docs' / 'guias' / '.build'

MARCADOR = re.compile(r'\{\{codigo\s+(.*?)\}\}', re.S)


# -----------------------------------------------------------------------------
#  Extracción
# -----------------------------------------------------------------------------
def metodo_de(texto, nombre):
    """El método `nombre` entero, cerrando llaves. Devuelve las líneas tal cual."""
    lineas = texto.split('\n')
    # Sin `.` en el prefijo: si no, `SpringApplication.run(` se toma por la firma de `run`.
    firma = re.compile(r'^\s*(?:@\w+\s+)?(?:[\w<>\[\],\s]+\s+)?\b' + re.escape(nombre) + r'\s*\(')
    for i, l in enumerate(lineas):
        if firma.match(l) and not l.strip().startswith('//'):
            # Las anotaciones de encima son parte del método: `@Bean` es justo lo que
            # la guía señala con el dedo, y sin ella el bloque no enseña lo que dice.
            while i > 0 and lineas[i - 1].strip().startswith('@'):
                i -= 1
            profundidad = 0
            abierto = False
            for j in range(i, len(lineas)):
                profundidad += lineas[j].count('{') - lineas[j].count('}')
                if profundidad > 0:
                    abierto = True
                if abierto and profundidad == 0:
                    return lineas[i:j + 1]
                if not abierto and lineas[j].rstrip().endswith(';'):
                    return lineas[i:j + 1]
    raise SystemExit(f'[ERROR] no se encontró el método `{nombre}`')


def entre(texto, desde, hasta):
    """Lo que hay entre la línea que contiene `desde` y la siguiente que contiene
    `hasta`, sin incluir ninguna de las dos."""
    lineas = texto.split('\n')
    ini = None
    for i, l in enumerate(lineas):
        if desde in l:
            ini = i + 1
            break
    if ini is None:
        raise SystemExit(f'[ERROR] no se encontró el ancla de inicio: {desde!r}')
    for j in range(ini, len(lineas)):
        if hasta in lineas[j]:
            return lineas[ini:j]
    raise SystemExit(f'[ERROR] no se encontró el ancla de fin: {hasta!r}')


def clave_yaml(texto, clave):
    """Un bloque de primer nivel de un YAML: la línea `clave:` y todo lo que
    cuelga de ella por indentación. Se saltan los comentarios y los blancos que
    haya dentro — la guía enseña el bloque, no la documentación de la solución."""
    lineas = texto.split('\n')
    salida, dentro = [], False
    for l in lineas:
        if not dentro:
            if l.startswith(clave + ':'):
                dentro = True
                salida.append(l)
            continue
        if l and not l[0].isspace():
            break
        if l.strip().startswith('#') or not l.strip():
            continue
        salida.append(l)
    if not salida:
        raise SystemExit(f'[ERROR] no se encontró la clave YAML `{clave}`')
    return salida


def bloque_xml(texto, contiene):
    """El <dependency> o el <plugin> que contiene el texto dado, entero."""
    lineas = texto.split('\n')
    for i, l in enumerate(lineas):
        if contiene in l:
            for a in range(i, -1, -1):
                m = re.match(r'^(\s*)<(dependency|plugin)>', lineas[a])
                if m:
                    etiqueta = m.group(2)
                    for b in range(i, len(lineas)):
                        if re.match(r'^\s*</' + etiqueta + r'>', lineas[b]):
                            return lineas[a:b + 1]
                    break
    raise SystemExit(f'[ERROR] no se encontró un bloque XML que contenga {contiene!r}')


def clase_con(texto, nombres, ruta):
    """
    Compone el estado INTERMEDIO de una clase: su `package`, los `import` que hacen
    falta, la declaración de la clase y solo los miembros que se piden.

    Existe porque el alumno construye la clase paso a paso y ese estado a medias no
    está en ningún archivo: `solucion/` guarda el estado final. Aun así **ni una línea
    se teclea aquí** — todas salen del archivo de la solución; lo único que hace este
    modo es elegir cuáles y en qué orden.
    """
    lineas = texto.split('\n')
    paquete = next((l for l in lineas if l.startswith('package ')), None)
    imports = [l for l in lineas if l.startswith('import ')]
    declaracion = next((l for l in lineas if re.match(r'^(public\s+)?(final\s+)?'
                                                     r'(class|record|interface|enum)\s', l)), None)
    if paquete is None or declaracion is None:
        raise SystemExit(f'[ERROR] {ruta}: no se encontró el package o la declaración de la clase')

    anotaciones = []
    i = lineas.index(declaracion) - 1
    while i >= 0 and lineas[i].strip().startswith('@'):
        anotaciones.insert(0, lineas[i])
        i -= 1

    cuerpo = []
    for nombre in nombres:
        if cuerpo:
            cuerpo.append('')
        cuerpo.extend(metodo_de(texto, nombre))

    # Solo los `import` que el trozo emitido usa de verdad.
    emitido = '\n'.join(anotaciones + [declaracion] + cuerpo)
    usados = [l for l in imports
              if re.search(r'\b' + re.escape(l.rstrip(';').split('.')[-1]) + r'\b', emitido)]

    return ([paquete, ''] + (usados + [''] if usados else [])
            + anotaciones + [declaracion, ''] + cuerpo + ['', '}'])


def desangrar(lineas):
    """Quita la sangría común, para que el bloque quepa en la página."""
    utiles = [l for l in lineas if l.strip()]
    if not utiles:
        return lineas
    comun = min(len(l) - len(l.lstrip()) for l in utiles)
    return [l[comun:] if l.strip() else '' for l in lineas]


def extraer(args):
    lab = args['lab']
    if args.get('modo') == 'pasos':
        lineas, ruta = bloque_de_pasos(lab, args['ancla'])
        return lineas, ruta
    archivo = args['archivo']
    ruta = RAIZ / 'labs' / lab / 'solucion' / archivo
    if not ruta.is_file():
        raise SystemExit(f'[ERROR] no existe {ruta.relative_to(RAIZ)}')
    texto = ruta.read_text(encoding='utf-8')

    modo = args.get('modo', 'entero')
    if modo == 'entero':
        lineas = texto.rstrip('\n').split('\n')
    elif modo == 'metodo':
        lineas = metodo_de(texto, args['nombre'])
    elif modo == 'entre':
        lineas = entre(texto, args['desde'], args['hasta'])
    elif modo == 'clave':
        lineas = clave_yaml(texto, args['clave'])
    elif modo == 'clase':
        lineas = clase_con(texto, [n.strip() for n in args['miembros'].split(',')], ruta)
    elif modo == 'xml':
        lineas = bloque_xml(texto, args['contiene'])
    else:
        raise SystemExit(f'[ERROR] modo desconocido: {modo}')

    if args.get('sangria', 'quitar') == 'quitar' and modo != 'clase':
        lineas = desangrar(lineas)
    # Los comentarios de `solucion/` no van a la guía: la guía los explica en prosa.
    # Los comentarios de `solucion/` no van a la guía: están escritos para quien
    # prepara la clase, y la guía ya lo explica en prosa a su manera.
    marca = {'java': '//', 'sql': '--'}.get(args.get('lenguaje'))
    if marca and args.get('comentarios', 'quitar') == 'quitar':
        lineas = [l for l in lineas if not l.strip().startswith(marca)]
        # y los huecos que dejan, si quedan dos seguidos
        limpias = []
        for l in lineas:
            if not l.strip() and limpias and not limpias[-1].strip():
                continue
            limpias.append(l)
        lineas = limpias
    while lineas and not lineas[0].strip():
        lineas.pop(0)
    while lineas and not lineas[-1].strip():
        lineas.pop()
    return lineas, ruta


def bloque_de_pasos(lab, ancla):
    """
    Un bloque de código de `PASOS.md`, el que va justo debajo del ancla dada.

    Existe SOLO para los estados INTERMEDIOS: el código que un paso escribe y otro
    posterior reescribe. Ese código no está en `solucion/` —la solución guarda el
    estado final— y `PASOS.md` es el único sitio del repositorio que lo tiene. La
    alternativa sería teclearlo en la guía, que es justo lo que no se quiere.

    `PASOS.md` marca esos bloques con `<!-- pasos:intermedio · ... -->`, y el job
    `pasos` del CI los cuenta y los imprime en vez de comprobarlos. Aquí se hace lo
    mismo: se extraen, y se declaran aparte en el informe.
    """
    ruta = RAIZ / 'labs' / lab / 'PASOS.md'
    lineas = ruta.read_text(encoding='utf-8').split('\n')
    for i, l in enumerate(lineas):
        if ancla in l:
            for j in range(i, len(lineas)):
                if lineas[j].startswith('```'):
                    cuerpo = []
                    for k in range(j + 1, len(lineas)):
                        if lineas[k].startswith('```'):
                            return cuerpo, ruta
                        cuerpo.append(lineas[k])
            break
    raise SystemExit(f'[ERROR] {ruta.name}: no se encontró un bloque tras el ancla {ancla!r}')


def parsear(cuerpo):
    args = {}
    for trozo in shlex.split(cuerpo):
        if '=' not in trozo:
            raise SystemExit(f'[ERROR] argumento sin `=` en un marcador: {trozo!r}')
        k, v = trozo.split('=', 1)
        args[k] = v
    obligatorios = ('lab', 'lenguaje') if args.get('modo') == 'pasos' else ('lab', 'archivo', 'lenguaje')
    for obligatorio in obligatorios:
        if obligatorio not in args:
            raise SystemExit(f'[ERROR] falta `{obligatorio}` en un marcador: {cuerpo!r}')
    return args


# -----------------------------------------------------------------------------
#  Resolución y comprobación
# -----------------------------------------------------------------------------
def resolver(md, comprobaciones):
    def sustituir(m):
        args = parsear(m.group(1))
        lineas, ruta = extraer(args)
        comprobaciones.append((args, lineas, ruta))
        return '``` ' + args['lenguaje'] + '\n' + '\n'.join(lineas) + '\n```'
    return MARCADOR.sub(sustituir, md)


def comprobar(comprobaciones):
    """V1: cada línea de cada bloque tiene que estar, tal cual, en `solucion/`.

    Los bloques intermedios (`modo=pasos`) no se comparan: por definición no están en
    `solucion/`. Se CUENTAN y se imprimen, para que saltárselos sea una decisión visible
    y no una forma barata de poner esto en verde."""
    fallos = intermedios = 0
    for args, lineas, ruta in comprobaciones:
        if args.get('modo') == 'pasos':
            intermedios += 1
            print(f'  [INTERMEDIO] {ruta.relative_to(RAIZ)} · {len(lineas)} líneas · '
                  f'no está en solucion/ por ser un estado a medias, y se declara')
            continue
        fuente = ruta.read_text(encoding='utf-8')
        sueltas = {l.strip() for l in fuente.split('\n')}
        for l in lineas:
            if l.strip() and l.strip() not in sueltas:
                print(f'  [ERROR] {ruta.relative_to(RAIZ)} · la guía imprime una línea '
                      f'que la solución no tiene: {l.strip()!r}')
                fallos += 1
    return fallos


# -----------------------------------------------------------------------------
#  PDF
# -----------------------------------------------------------------------------
def a_pdf(md_resuelto, destino, titulo):
    BUILD.mkdir(parents=True, exist_ok=True)
    intermedio = BUILD / (destino.stem + '.md')
    intermedio.write_text(md_resuelto, encoding='utf-8')
    orden = [
        'pandoc', str(intermedio), '-o', str(destino),
        '--pdf-engine=xelatex',
        '--from', 'markdown+fenced_divs+pipe_tables+yaml_metadata_block',
        '--toc', '--toc-depth=2',
        '--number-sections',
        '--highlight-style=tango',
        '--lua-filter', str(ESTILO / 'recuadros.lua'),
        '--include-in-header', str(ESTILO / 'preambulo.tex'),
        '-V', 'documentclass=article',
        '-V', 'papersize=a4',
        '-V', 'geometry:margin=2.4cm',
        '-V', 'fontsize=11pt',
        '-V', 'mainfont=Helvetica',
        '-V', 'monofont=Menlo',
        '-V', 'monofontoptions=Scale=0.82',
        '-V', 'colorlinks=true',
        '-V', 'linkcolor=azuldgt',
        '-V', 'toccolor=azuldgt',
        '-V', 'lang=es',
    ]
    r = subprocess.run(orden, capture_output=True, text=True)
    if r.returncode != 0:
        print(r.stdout[-3000:])
        print(r.stderr[-3000:])
        raise SystemExit(f'[ERROR] pandoc falló para {titulo}')


def destino_de(fuente):
    """`guia-lab-04-jpa.md` -> `labs/lab-04-jpa/guia-lab-04-jpa.pdf`."""
    lab = fuente.stem.removeprefix('guia-')
    carpeta = RAIZ / 'labs' / lab
    if not carpeta.is_dir():
        raise SystemExit(f'[ERROR] {fuente.name}: no existe la carpeta labs/{lab}')
    return carpeta / (fuente.stem + '.pdf')


def main():
    solo_verificar = '--verificar' in sys.argv
    filtros = [a for a in sys.argv[1:] if not a.startswith('--')]

    fuentes = sorted(FUENTE.glob('guia-*.md'))
    if filtros:
        fuentes = [f for f in fuentes if any(x in f.name for x in filtros)]
    if not fuentes:
        raise SystemExit('[ERROR] no hay fuentes que procesar')

    total_bloques = fallos = 0
    for f in fuentes:
        md = f.read_text(encoding='utf-8')
        comprobaciones = []
        resuelto = resolver(md, comprobaciones)
        total_bloques += len(comprobaciones)

        print(f'{f.name}  ·  {len(comprobaciones)} bloque(s) extraído(s) de solucion/')
        for args, lineas, ruta in comprobaciones:
            print(f'    {args["modo"] if "modo" in args else "entero":<7} '
                  f'{len(lineas):>3} líneas  <-  {ruta.relative_to(RAIZ)}')
        fallos += comprobar(comprobaciones)

        if not solo_verificar:
            destino = destino_de(f)
            a_pdf(resuelto, destino, f.name)
            kb = destino.stat().st_size / 1024
            paginas = subprocess.run(['pdfinfo', str(destino)], capture_output=True, text=True)
            n = ''
            for l in paginas.stdout.split('\n'):
                if l.startswith('Pages:'):
                    n = l.split(':')[1].strip()
            print(f'    -> {destino.relative_to(RAIZ)}  ·  {n} páginas  ·  {kb:.0f} KB')
        print()

    print(f'[{"ERROR" if fallos else "OK"}] {total_bloques} bloque(s) comprobado(s) '
          f'contra solucion/ · {fallos} línea(s) que la solución no tiene')
    return 1 if fallos else 0


if __name__ == '__main__':
    sys.exit(main())
