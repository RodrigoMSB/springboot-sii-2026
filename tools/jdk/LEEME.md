# El JDK que viaja en la maleta

Aquí dentro va **Eclipse Temurin 25**, el mismo JDK que el curso pide, para que el alumno no
tenga que instalarlo. Es la última pieza del material autocontenido: tras esto, lo único que
hace falta tener instalado es **Git**.

## Qué es exactamente

| | |
|---|---|
| **Distribución** | Eclipse Temurin (Adoptium) |
| **Versión** | `jdk-25.0.4+7` — la última GA de la línea 25 al 13 de agosto de 2026 |
| **Plataformas** | `windows-x64` (los alumnos) · `macos-aarch64` (donde se prepara el material) |
| **Origen** | https://github.com/adoptium/temurin25-binaries/releases/tag/jdk-25.0.4%2B7 |
| **Licencia** | GPLv2 + Classpath Exception — **permite redistribución** |

Los sha256 de abajo son los **publicados por Temurin**, verificados contra lo que hay aquí:

```
windows-x64    7caab7db43bf4b94a2e6252c699e70d90084f9aa7c943cd3414761fd540937ae
               OpenJDK25U-jdk_x64_windows_hotspot_25.0.4_7.zip

macos-aarch64  5a101c54abf5a9f16c0f70d8c38ba99e6567c1ba213378f0bb04497284f051bd
               OpenJDK25U-jdk_aarch64_mac_hotspot_25.0.4_7.tar.gz
```

## Por qué está partido en trozos

GitHub rechaza cualquier archivo de más de 100 MB, y estos pesan 134 y 130. Así que viajan
partidos en pedazos de 80 MB (`jdk.zip.part-00`, `part-01`, …) y el shim `mvnw` los junta la
primera vez que lo usas.

El `.sha256` que hay junto a los trozos es el del archivo **ensamblado**, no el de los pedazos:
es lo que le permite al shim comprobar que lo que reconstruyó es bit a bit el original de
Temurin. Si no cuadra, **aborta** — un JDK que no se puede verificar no se usa.

## Qué NO se versiona

`tools/jdk/runtime/` — donde el shim extrae el JDK ya ensamblado. Son ~300 MB derivables de lo
que sí está aquí, y cambiarían en cada máquina. El `.gitignore` los excluye.

## Cómo se actualiza (para quien mantiene el material)

```bash
# 1 · bajar el oficial y comprobar su firma contra la que publica Adoptium
curl -sSL -o jdk.tar.gz "<link de la release>"
shasum -a 256 jdk.tar.gz

# 2 · partir, sellar y anotar la versión
split -b 80m -d jdk.tar.gz jdk.tar.gz.part-
shasum -a 256 jdk.tar.gz | awk '{print $1}' > jdk.tar.gz.sha256
printf 'jdk-XX.Y.Z+B\n' > VERSION      # el nombre EXACTO del directorio raíz del archivo
```

El `VERSION` no es decorativo: el shim construye la ruta del JDK con él
(`runtime/<plataforma>/<VERSION>`), así que tiene que coincidir con el nombre del directorio que
hay dentro del comprimido. Al cambiar de versión, el sello viejo deja de cuadrar y el shim
re-ensambla solo.
