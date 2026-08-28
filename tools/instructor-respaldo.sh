#!/usr/bin/env bash
# =============================================================================
#  instructor-respaldo.sh — el puente con el repositorio privado
# -----------------------------------------------------------------------------
#  `labs/*/instructor/` y `proyecto-final/instructor/` NO viajan en este
#  repositorio (D-031-2: si viajaran, el alumno tendria la chuleta delante). El
#  costo aceptado de esa decision era que el material que mas trabajo tiene
#  encima vivia en UNA sola maquina, sin respaldo (SPEC-041, informe §6).
#
#  La SPEC-042 lo resuelve sin tocar D-031-2: un repositorio PRIVADO aparte que
#  guarda solo esas carpetas, con la misma estructura de rutas. Este script es
#  el puente entre los dos.
#
#  USO
#
#      tools/instructor-respaldo.sh estado      # que diferencias hay
#      tools/instructor-respaldo.sh respaldar   # disco  -> repositorio privado
#      tools/instructor-respaldo.sh restaurar   # privado -> disco
#
#      ... [--destino RUTA]   donde esta el clon del repositorio privado.
#                             Por defecto, al lado de este:
#                             ../springboot-sii-2026-instructor
#
#  QUE COPIA Y QUE NO
#
#  Copia todo lo que cuelgue de una carpeta `instructor/`, MENOS:
#    · `target/`      — salida de compilacion. `instructor/` no es un proyecto y
#                       no se compila; los `target/` que habia eran herencia de
#                       cuando la carpeta se genero copiando `solucion/` entera,
#                       y llevaban dentro copias rancias de los recursos
#                       (INFORME-SPEC-041 §7). Se borraron en la SPEC-042.
#    · `.DS_Store`    — basura de macOS.
#    · `.datos-pg/`   — el directorio de datos de PostgreSQL. Estado local.
#
#  POR QUE UN SCRIPT DE COPIA Y NO UN SUBMODULO NI UN REMOTE MAS
#
#  Se descartaron las dos alternativas obvias:
#    · Un submodulo de git dentro de `labs/` — apareceria en el clon del alumno
#      como una entrada del arbol. Aunque no pudiera clonarlo, le estaria
#      diciendo que existe y donde. D-031-2 quiere lo contrario.
#    · Un segundo `remote` en este mismo repositorio, con una rama huerfana —
#      un `git push --all` o un despiste manda `instructor/` al repositorio
#      publico. Es un cargador con bala.
#  Copiar archivos entre dos clones no puede hacer ninguna de las dos cosas: los
#  dos repositorios quedan independientes y el error mas caro posible es tener
#  que volver a copiar.
#
#  IMPRIME TODO LO QUE CUENTA (A-04) y no declara sano lo que no ha mirado
#  (A-02): cada operacion termina comparando las huellas sha256 de los dos lados
#  y diciendo cuantas cuadran.
# =============================================================================
set -euo pipefail

DESTINO_POR_DEFECTO="../springboot-sii-2026-instructor"

# -----------------------------------------------------------------------------
#  La raiz del repositorio publico: donde esta este script, un nivel arriba.
#  Se resuelve con `pwd -P` (fisica, no logica) para que un symlink no haga que
#  las comparaciones de la guarda de borrado fallen. Es la leccion de la
#  SPEC-FIX-05: el guard de `borrar_seguro` comparaba /tmp contra /private/tmp y
#  abortaba borrados legitimos.
# -----------------------------------------------------------------------------
RAIZ="$(cd "$(dirname "$0")/.." && pwd -P)"

# -----------------------------------------------------------------------------
#  sha256 se llama distinto segun la maquina: `shasum -a 256` en macOS,
#  `sha256sum` en Linux y en Git Bash. Se elige una y se avisa si no hay ninguna
#  en vez de seguir y dar un resultado vacio que pareceria "sin diferencias".
# -----------------------------------------------------------------------------
if command -v sha256sum >/dev/null 2>&1; then
  huella() { sha256sum "$1" | cut -d' ' -f1; }
elif command -v shasum >/dev/null 2>&1; then
  huella() { shasum -a 256 "$1" | cut -d' ' -f1; }
else
  echo "[ERROR] No hay ni sha256sum ni shasum: no se puede verificar nada." >&2
  exit 1
fi

# -----------------------------------------------------------------------------
#  Las carpetas del respaldo, en un solo sitio. Son 16: los quince labs y el
#  proyecto final. Se listan mirando el disco, no una lista escrita a mano: un
#  lab nuevo entra solo.
# -----------------------------------------------------------------------------
carpetas_de() {
  ( cd "$1" 2>/dev/null || return 0
    find labs -maxdepth 2 -type d -name instructor 2>/dev/null
    find proyecto-final -maxdepth 1 -type d -name instructor 2>/dev/null
    find examen-huecos -maxdepth 1 -type d -name instructor 2>/dev/null
  ) | LC_ALL=C sort
}

archivos_de() {
  # $1 = raiz, $2 = carpeta relativa. Rutas relativas a la raiz, ordenadas.
  ( cd "$1" && find "$2" -type f \
      -not -path '*/target/*' \
      -not -path '*/.datos-pg/*' \
      -not -name '.DS_Store' 2>/dev/null ) | LC_ALL=C sort
}

manifiesto() {
  # $1 = raiz. Lista `huella  ruta` de TODO el material, para comparar arboles.
  local raiz="$1" carpeta f
  for carpeta in $(carpetas_de "$raiz"); do
    for f in $(archivos_de "$raiz" "$carpeta"); do
      printf '%s  %s\n' "$(huella "$raiz/$f")" "$f"
    done
  done
}

# -----------------------------------------------------------------------------
#  El borrado, con guarda. Solo se acepta una ruta que:
#    1. este DENTRO de la raiz de destino (comparando rutas fisicas), y
#    2. se llame, literalmente, `instructor`.
#  Cualquier otra cosa aborta sin borrar. Y si despues del `rm` la carpeta sigue
#  ahi, se dice: un borrado que no borro y uno que borro no se distinguen desde
#  fuera si nadie mira (SPEC-FIX-05).
# -----------------------------------------------------------------------------
borrar_carpeta_instructor() {
  local raiz_destino="$1" objetivo="$2" fisica
  [ -d "$objetivo" ] || return 0
  fisica="$(cd "$objetivo" && pwd -P)"
  case "$(basename "$fisica")" in
    instructor) : ;;
    *) echo "[ABORTA] no se llama 'instructor': $fisica" >&2; exit 1 ;;
  esac
  case "$fisica" in
    "$raiz_destino"/*) : ;;
    *) echo "[ABORTA] cae fuera del destino ($raiz_destino): $fisica" >&2; exit 1 ;;
  esac
  rm -rf "$objetivo"
  if [ -e "$objetivo" ]; then
    echo "[ERROR] no se pudo borrar: $objetivo" >&2
    exit 1
  fi
}

copiar_arbol() {
  # $1 = raiz origen, $2 = raiz destino. Devuelve por eco el total de archivos.
  local origen="$1" destino="$2" carpeta f total=0 n
  for carpeta in $(carpetas_de "$origen"); do
    borrar_carpeta_instructor "$destino" "$destino/$carpeta"
    n=0
    for f in $(archivos_de "$origen" "$carpeta"); do
      mkdir -p "$destino/$(dirname "$f")"
      cp "$origen/$f" "$destino/$f"
      n=$((n + 1))
    done
    printf '  [OK] %-46s %3s archivos\n' "$carpeta" "$n"
    total=$((total + n))
  done
  printf '  %-46s %3s archivos en %s carpetas\n' "TOTAL" "$total" "$(carpetas_de "$origen" | wc -l | tr -d ' ')"
}

comparar() {
  # Compara los dos arboles por huella y dice el numero. Sale 1 si difieren.
  local a b iguales
  a="$(mktemp)"; b="$(mktemp)"
  manifiesto "$RAIZ"    > "$a"
  manifiesto "$DESTINO" > "$b"
  iguales=$(comm -12 "$a" "$b" | wc -l | tr -d ' ')
  echo "  disco   : $(wc -l < "$a" | tr -d ' ') archivos"
  echo "  respaldo: $(wc -l < "$b" | tr -d ' ') archivos"
  echo "  huellas que cuadran en los dos: $iguales"
  if diff -q "$a" "$b" >/dev/null; then
    echo "  [OK] los dos arboles son identicos."
    rm -f "$a" "$b"; return 0
  fi
  echo "  [DIFERENCIAS]"
  diff "$a" "$b" | grep -E '^[<>]' | sed 's|^< |    solo en el disco    : |; s|^> |    solo en el respaldo : |' \
    | sed 's/  [0-9a-f]\{64\}  / /' | head -40
  rm -f "$a" "$b"; return 1
}

# ----------------------------------- main ------------------------------------
ACCION="${1:-}"; shift || true
DESTINO="$RAIZ/$DESTINO_POR_DEFECTO"
while [ $# -gt 0 ]; do
  case "$1" in
    --destino) DESTINO="$2"; shift 2 ;;
    *) echo "[ERROR] argumento desconocido: $1" >&2; exit 1 ;;
  esac
done

case "$ACCION" in
  estado|respaldar|restaurar) : ;;
  *)
    echo "Uso: tools/instructor-respaldo.sh <estado|respaldar|restaurar> [--destino RUTA]" >&2
    exit 1 ;;
esac

if [ ! -d "$DESTINO" ]; then
  echo "[ERROR] No encuentro el clon del repositorio privado en:" >&2
  echo "        $DESTINO" >&2
  echo >&2
  echo "        Clonalo al lado de este repositorio:" >&2
  echo "          git clone git@github.com:RodrigoMSB/springboot-sii-2026-instructor.git \\" >&2
  echo "              \"$DESTINO\"" >&2
  echo "        o pasa su ruta con --destino." >&2
  exit 1
fi
DESTINO="$(cd "$DESTINO" && pwd -P)"

echo "publico : $RAIZ"
echo "privado : $DESTINO"
echo

case "$ACCION" in
  estado)
    echo "ESTADO"
    comparar
    ;;
  respaldar)
    echo "RESPALDAR · disco -> repositorio privado"
    copiar_arbol "$RAIZ" "$DESTINO"
    echo
    echo "COMPROBACION"
    comparar
    echo
    echo "  Falta el commit. En $DESTINO:"
    echo "    git add -A && git commit -m \"...\" && git push"
    ;;
  restaurar)
    echo "RESTAURAR · repositorio privado -> disco"
    copiar_arbol "$DESTINO" "$RAIZ"
    echo
    echo "COMPROBACION"
    comparar
    echo
    echo "  Y para comprobar que quedo al dia con solucion/:"
    echo "    python3 tools/verificar-instructor.py"
    ;;
esac
