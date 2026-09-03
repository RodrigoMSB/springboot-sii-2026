#!/usr/bin/env python3
# =============================================================================
#  verificar-demo-docker.py — que la demostración con Docker no se separe del lab
# -----------------------------------------------------------------------------
#  `demos-instructor/microservicios-docker/sistema/` es una COPIA de
#  `labs/lab-microservicios/solucion/`. Copiar código es aceptar que se
#  separe, y la separación no avisa: el día que alguien arregle un defecto en el
#  laboratorio, la demostración se queda con el defecto y nadie se entera hasta
#  que el PO lo proyecta en una sala.
#
#  Esto es lo que lo vigila. **No necesita Docker** — compara archivos— así que
#  puede correr en el CI, que es justo donde Docker no está (SPEC-047 §5.2).
#
#  EL CONTRATO. La copia es idéntica al laboratorio SALVO en cuatro sitios, y
#  los cuatro son exactamente las piezas que el orquestador reemplaza:
#
#    1. `infra/MotorDePostgres.java`, `infra/PuertoLibre.java` y
#       `infra/CandadoLibre.java` NO ESTÁN. La base ya no la levanta el servicio:
#       es un contenedor, y `depends_on` espera a que esté sana.
#    2. Las tres clases `*Application.java` con base pierden la línea que
#       levantaba esa base.
#    3. Los `pom.xml` pierden Zonky y encienden el `repackage` (el jar ejecutable
#       ES lo que se despliega).
#    4. Los `application.yml` cambian `localhost:puerto` por nombres de servicio.
#
#  Todo lo demás —controladores, entidades, repositorios, servicios, clientes,
#  el filtro de correlación, las migraciones— tiene que ser IDÉNTICO byte a byte.
#  Si alguien toca uno de esos archivos en un lado y no en el otro, esto lo caza.
#
#  QUÉ NO COMPRUEBA: que la demostración levante. Eso necesita Docker y se hace a
#  mano, en la máquina donde se va a proyectar. Aquí se vigila la deriva del
#  código, que es lo que se puede vigilar sin Docker.
#
#  Uso:  python3 tools/verificar-demo-docker.py
# =============================================================================
import pathlib
import sys

RAIZ = pathlib.Path(__file__).resolve().parent.parent
LAB = RAIZ / 'labs' / 'lab-microservicios' / 'solucion'
DEMO = RAIZ / 'demos-instructor' / 'microservicios-docker' / 'sistema'

SERVICIOS = ('gateway', 'contribuyentes', 'tramites', 'auditoria')

# Lo que no es material y por tanto no se compara: lo que compila Maven, el estado
# local del PostgreSQL embebido del laboratorio —que el PO deja en su copia de
# trabajo después de dictar— y el propio `.gitignore`. Los tres están ignorados
# por git; mirarlos daría rojos falsos en cualquier clon donde alguien haya
# corrido el lab.
FUERA_DE_LA_COMPARACION = ('target/', '.datos-pg/', '.estado/')

# El shim de Maven: el laboratorio lleva una copia POR SERVICIO, porque cada uno
# es un proyecto que el alumno arranca por separado desde su propia terminal. La
# demostración lleva UNA sola en `sistema/`, porque `construir.sh` los compila los
# cuatro seguidos. Es una diferencia de montaje, no de código.
SHIM_DE_MAVEN = ('mvnw', 'mvnw.cmd', '.mvn/wrapper/maven-wrapper.properties')

# Los archivos que el orquestador reemplaza y que, por eso, NO están en la copia.
AUSENTES_ESPERADOS = {
    'infra/MotorDePostgres.java',
    'infra/PuertoLibre.java',
    'infra/CandadoLibre.java',
}

# Los que sí están y a los que se les permite diferir. Cada uno con la razón, para
# que ampliar esta lista sea una decisión visible y no un parche.
DIFERENCIAS_PERMITIDAS = {
    'pom.xml': 'sin Zonky y con el `repackage` encendido',
    'src/main/resources/application.yml': 'las direcciones son nombres de servicio',
    'ContribuyentesApplication.java': 'ya no levanta su base embebida',
    'TramitesApplication.java': 'ya no levanta su base embebida',
    'AuditoriaApplication.java': 'ya no levanta su base embebida',
}


def relativos(base, servicio):
    """Los archivos de un servicio, como rutas relativas a su carpeta."""
    raiz = base / servicio
    if not raiz.is_dir():
        return {}
    salida = {}
    for f in raiz.rglob('*'):
        if not f.is_file():
            continue
        rel = f.relative_to(raiz).as_posix()
        if any(rel.startswith(x) for x in FUERA_DE_LA_COMPARACION):
            continue
        if rel == '.gitignore' or rel in SHIM_DE_MAVEN:
            continue
        salida[rel] = f
    return salida


def permitida(rel):
    for clave in DIFERENCIAS_PERMITIDAS:
        if rel == clave or rel.endswith('/' + clave):
            return DIFERENCIAS_PERMITIDAS[clave]
    return None


def main():
    if not DEMO.is_dir():
        print(f'[ERROR] no existe {DEMO.relative_to(RAIZ)}')
        return 1
    if not LAB.is_dir():
        print(f'[ERROR] no existe {LAB.relative_to(RAIZ)}')
        return 1

    print('La demostración con Docker contra el laboratorio que copia:')
    print(f'  lab  : {LAB.relative_to(RAIZ)}')
    print(f'  demo : {DEMO.relative_to(RAIZ)}')
    print()

    fallos = []
    n_iguales = n_permitidas = n_ausentes = 0

    for servicio in SERVICIOS:
        en_lab = relativos(LAB, servicio)
        en_demo = relativos(DEMO, servicio)
        iguales = permitidas = ausentes = 0

        for rel, f_lab in sorted(en_lab.items()):
            f_demo = en_demo.get(rel)
            if f_demo is None:
                if any(rel.endswith(a) for a in AUSENTES_ESPERADOS):
                    ausentes += 1
                    continue
                fallos.append(f'{servicio}: el laboratorio trae `{rel}` y la demostración no. '
                              f'Si es a propósito, va en AUSENTES_ESPERADOS con su razón.')
                continue
            if f_lab.read_bytes() == f_demo.read_bytes():
                iguales += 1
                continue
            razon = permitida(rel)
            if razon:
                permitidas += 1
                continue
            fallos.append(f'{servicio}: `{rel}` DIFIERE entre el laboratorio y la demostración, '
                          f'y no es una de las diferencias declaradas.')

        # y al revés: nada nuevo que el laboratorio no tenga
        for rel in sorted(set(en_demo) - set(en_lab)):
            fallos.append(f'{servicio}: la demostración trae `{rel}` y el laboratorio no. '
                          f'La copia no inventa archivos.')

        n_iguales += iguales
        n_permitidas += permitidas
        n_ausentes += ausentes
        estado = 'OK' if not any(servicio + ':' in f for f in fallos) else 'ERROR'
        print(f'  [{estado}] {servicio:<15} {iguales:>2} idénticos · '
              f'{permitidas} con diferencia declarada · {ausentes} retirados')

    print()
    print(f'Comprobados: {n_iguales} archivos idénticos byte a byte · '
          f'{n_permitidas} con diferencia declarada · {n_ausentes} retirados a propósito')
    print()
    print('Las diferencias que se permiten, y por qué:')
    for clave, razon in DIFERENCIAS_PERMITIDAS.items():
        print(f'  · {clave:<38} {razon}')
    print('Los archivos que se retiran, porque el orquestador los reemplaza:')
    for a in sorted(AUSENTES_ESPERADOS):
        print(f'  · {a}')
    print('Fuera de la comparación (no son material): '
          + ', '.join(FUERA_DE_LA_COMPARACION) + ' y el shim de Maven.')

    if fallos:
        print()
        print(f'[ERROR] {len(fallos)} deriva(s) entre el laboratorio y su demostración:')
        print()
        for f in fallos:
            print(f'  · {f}')
        print()
        print('  El laboratorio manda: es lo que dictan los alumnos. Se arregla')
        print('  llevando el cambio a la demostración, no al revés.')
        return 1

    print()
    print('[OK] la demostración con Docker dice el mismo código que el laboratorio.')
    return 0


if __name__ == '__main__':
    sys.exit(main())
