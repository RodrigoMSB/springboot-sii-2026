#!/usr/bin/env python3
# =============================================================================
#  verificar-pasos-copiables.py — que el guion no mienta sobre el código
# -----------------------------------------------------------------------------
#  La SPEC-038 puso en `labs/lab-04-jpa/PASOS.md` el código exacto que el
#  instructor pega en clase, extraído de `solucion/`. El informe de esa SPEC
#  decía la parte incómoda:
#
#      «Hoy los bloques cuadran porque se acaban de extraer, no porque nada lo
#       vigile. Nada impide que mañana alguien toque solucion/ y el guion quede
#       mintiendo.»
#
#  Esto es lo que lo vigila. Se ejecuta en el CI en cada PR.
#
#  QUÉ COMPRUEBA, y son dos cosas de distinta fuerza:
#
#    1. TODA línea de todo bloque ```java del guion existe en `solucion/`.
#       Caza el caso corriente: alguien renombra un método, cambia un literal
#       o toca una firma en la solución, y el guion se queda con lo viejo.
#
#    2. Todo bloque que contenga un MÉTODO COMPLETO es idéntico, línea por
#       línea, al método del mismo nombre en `solucion/`. Es la comprobación
#       fuerte: no basta con que las líneas existan sueltas por ahí, tienen
#       que estar en el mismo orden y sin sobras ni faltas.
#
#  Los comentarios y las líneas en blanco se ignoran en las dos: `practica/` va
#  sin documentación (regla de la casa) y `solucion/` lleva la suya, así que los
#  bloques del guion nunca van a traer los comentarios de la solución.
#
#  BLOQUES INTERMEDIOS. Algunos labs escriben en un paso una versión que un paso
#  posterior reescribe (el Lab 01 lo hace dos veces: los pasos 4 y 5 devuelven un
#  DTO y el paso 6 los envuelve en ResponseEntity). Esa versión intermedia es
#  código real que el instructor pega, pero NO está en `solucion/` — la solución
#  guarda el estado final. Se marcan así, en la línea de encima del bloque:
#
#      <!-- pasos:intermedio · lo reescribe el paso 6 -->
#
#  y este script las salta. **Las cuenta y las imprime**, para que saltarse un
#  bloque sea una decisión visible y no una forma barata de poner esto en verde.
#
#  QUÉ **NO** COMPRUEBA, y conviene saberlo: que el guion esté completo. Si
#  alguien añade un método a `solucion/` y no lo menciona en el guion, esto pasa
#  en verde. Vigila que lo que el guion DICE sea verdad, no que lo diga todo.
#
#  QUÉ BLOQUES MIRA. Solo los que cuelgan de un «**Se pega**». Un `PASOS.md`
#  también enseña código que YA ESTÁ en `practica/` —para mirarlo y criticarlo,
#  como el `.orElseThrow()` sin argumentos del Lab 03— y ese no se pega ni tiene
#  por qué estar en `solucion/`. Se resuelve por posición: se mira hacia atrás
#  desde el bloque, y solo cuenta si lo primero que aparece es un «Se pega» y no
#  un «Se explica», «En consola», «Se corre» o «Se prueba».
#
#  ALCANCE: cualquier lab cuyo `PASOS.md` contenga el marcador «**Se pega». Hoy
#  es solo el 04; si la SPEC-038 se extiende, los nuevos entran solos.
#
#  Uso:  python3 tools/verificar-pasos-copiables.py
# =============================================================================
import pathlib
import re
import sys

RAIZ = pathlib.Path(__file__).resolve().parent.parent
MARCADOR = "**Se pega"


def sin_ruido(texto):
    """Las líneas que de verdad son código: sin comentarios y sin blancos."""
    salida = []
    for linea in texto.split("\n"):
        limpia = linea.strip()
        if not limpia or limpia.startswith("//"):
            continue
        salida.append(limpia)
    return salida


def metodos_de(texto):
    """
    Los métodos de un archivo Java, por nombre -> lista de líneas limpias.

    Se localizan por su firma y se cierran contando llaves. Arrastra las
    anotaciones de encima (@Transactional, @GetMapping...), que son parte del
    método a efectos de lo que el instructor pega.
    """
    lineas = texto.split("\n")
    # Sin exigir modificador de acceso: los métodos de test de JUnit son
    # package-private (`void elPrecioConIva...()`) y sin esto quedaban fuera de
    # la comprobación fuerte, que es justo la que caza una línea añadida.
    firma = re.compile(
        r"^\s{4}(?!.*\b(?:class|interface|enum|record)\b)"
        r"(?:(?:public|private|protected|static|final|abstract|synchronized)\s+)*"
        r"[\w<>,\[\]\.\?]+\s+(\w+)\s*\([^;]*$|"
        r"^\s{4}(?!.*\b(?:class|interface|enum|record)\b)"
        r"(?:(?:public|private|protected|static|final|abstract|synchronized)\s+)*"
        r"[\w<>,\[\]\.\?]+\s+(\w+)\s*\(.*\)\s*\{"
    )
    encontrados = {}
    i = 0
    while i < len(lineas):
        m = firma.match(lineas[i])
        if not m:
            i += 1
            continue
        nombre = m.group(1) or m.group(2)
        inicio = i
        while inicio > 0 and lineas[inicio - 1].strip().startswith("@"):
            inicio -= 1
        nivel = 0
        fin = None
        for j in range(i, len(lineas)):
            nivel += lineas[j].count("{") - lineas[j].count("}")
            if nivel == 0 and "{" in "".join(lineas[i:j + 1]):
                fin = j
                break
        if fin is None:
            i += 1
            continue
        encontrados.setdefault(nombre, []).append(sin_ruido("\n".join(lineas[inicio:fin + 1])))
        i = fin + 1
    return encontrados


def revisar_lab(lab):
    pasos = lab / "PASOS.md"
    solucion = lab / "solucion"
    # `rglob` sobre solucion/ ya cubre src/main y src/test, y los sub-proyectos
    # de un lab multi-módulo (el 14 son cuatro servicios).
    fuentes = sorted(solucion.rglob("*.java")) if solucion.is_dir() else []
    if not fuentes:
        return [f"{lab.name}: tiene «{MARCADOR}» en PASOS.md pero no encuentro fuentes en solucion/"]

    lineas_solucion = set()
    metodos_solucion = {}
    for f in fuentes:
        texto = f.read_text()
        lineas_solucion.update(sin_ruido(texto))
        for nombre, cuerpos in metodos_de(texto).items():
            metodos_solucion.setdefault(nombre, []).extend(cuerpos)

    texto = pasos.read_text()
    # Marcadores de sección: SIEMPRE a principio de línea y en negrita. Buscarlos
    # con `rfind` sobre el texto crudo fallaba, porque «Se pega» también aparece
    # citado a media frase.
    MARCA = re.compile(r"^\*\*(Se pega|Se explica|Se escribe|En consola|Se corre|"
                       r"Se prueba|Se hace|En navegador|Se agrega|Lo que|Se descomenta)",
                       re.M)
    bloques, n_saltados, n_ajenos = [], 0, 0
    for m in re.finditer(r"```java\n(.*?)```", texto, re.S):
        antes = texto[:m.start()]
        marcas = MARCA.findall(antes)
        # ¿este bloque cuelga de un «Se pega»? La última marca manda.
        # «Se agrega al runner» también es pegar: esas líneas están en el
        # `Lab0NApplication` de solucion/ y conviene vigilarlas igual.
        if not marcas or marcas[-1] not in ("Se pega", "Se agrega"):
            n_ajenos += 1
            continue
        i_pega = antes.rfind("**Se pega")
        # ¿está marcado como version intermedia?
        cola = antes[antes.rfind("```") + 3:] if "```" in antes else antes
        if "pasos:intermedio" in antes[i_pega:]:
            n_saltados += 1
            continue
        bloques.append(m.group(1))
    fallos = []
    n_lineas = 0
    n_metodos = 0

    for numero, bloque in enumerate(bloques, start=1):
        limpio = sin_ruido(bloque)

        # (1) toda línea del bloque tiene que existir en solucion/
        for linea in limpio:
            n_lineas += 1
            if linea not in lineas_solucion:
                fallos.append(
                    f"{lab.name} · bloque {numero}: esta línea del guion NO está en solucion/\n"
                    f"        {linea}")

        # (2) si el bloque trae un método completo, tiene que coincidir entero
        for nombre, cuerpos in metodos_de(bloque).items():
            for cuerpo in cuerpos:
                n_metodos += 1
                if nombre not in metodos_solucion:
                    fallos.append(
                        f"{lab.name} · bloque {numero}: el método «{nombre}» del guion "
                        f"no existe en solucion/")
                elif cuerpo not in metodos_solucion[nombre]:
                    esperado = metodos_solucion[nombre][0]
                    fallos.append(
                        f"{lab.name} · bloque {numero}: el método «{nombre}» del guion NO coincide "
                        f"con el de solucion/\n"
                        f"      guion    ({len(cuerpo)} líneas): {cuerpo[:3]}\n"
                        f"      solucion ({len(esperado)} líneas): {esperado[:3]}")

    extra = (f" · {n_saltados} intermedios" if n_saltados else "") + \
            (f" · {n_ajenos} de solo mirar" if n_ajenos else "")
    print(f"  [{'OK' if not fallos else 'ERROR'}] {lab.name}: "
          f"{len(bloques)} bloques · {n_lineas} líneas · {n_metodos} métodos completos{extra}")
    return fallos


def main():
    labs = sorted(d for d in (RAIZ / "labs").iterdir()
                  if d.is_dir() and d.name.startswith("lab-") and (d / "PASOS.md").is_file())
    copiables = [d for d in labs if MARCADOR in (d / "PASOS.md").read_text()]

    print("Guiones con código para pegar (marcador «**Se pega»):")
    if not copiables:
        # Guarda de activación: si nadie usa el formato todavía, la regla queda
        # armada y no activa. Igual que el job `siembra` cuando nacieron los labs.
        print("  [INFO] ninguno. La regla queda armada, no activa.")
        return 0

    fallos = []
    for lab in copiables:
        fallos.extend(revisar_lab(lab))

    print()
    if fallos:
        print(f"[ERROR] {len(fallos)} desajuste(s) entre el guion y solucion/:")
        print()
        for f in fallos:
            print(f"  · {f}")
        print()
        print("  El guion promete un código que la solución ya no tiene. Se arregla")
        print("  volviendo a extraer los bloques desde solucion/ — nunca al revés,")
        print("  y nunca tecleándolos a mano (SPEC-038 §2).")
        return 1

    print(f"[OK] {len(copiables)} guion(es) verificado(s): todo lo que prometen está en solucion/.")
    return 0


if __name__ == "__main__":
    sys.exit(main())
