@REM ============================================================================
@REM  mvnw.cmd — el Maven Y el Java de este curso
@REM             (SPEC-022 · D-022-2, SPEC-024 · D-024-3)
@REM ----------------------------------------------------------------------------
@REM  Esto YA NO es el Maven Wrapper de Apache. El wrapper original descargaba una
@REM  distribucion de Maven de internet al primer uso, y en las maquinas del SII no
@REM  hay internet: se quedaba colgado en el primer comando del primer dia.
@REM
@REM  Este shim no descarga nada. Usa el Maven que viaja en el repositorio
@REM  (tools\maven), las dependencias que viajan con el (repo-maven) y el JDK 25
@REM  que tambien viaja (tools\jdk).
@REM
@REM  Ese ultimo es el que te ahorra la pelea: da igual que tu maquina tenga Java
@REM  17, Java 21, un JAVA_HOME apuntando a una carpeta que ya no existe, o ningun
@REM  Java. Este shim usa el suyo y no le pregunta a nadie.
@REM
@REM  Para ti no cambia nada:   mvnw.cmd test
@REM
@REM  La PRIMERA vez tarda unos segundos mas: ensambla el JDK desde los trozos en
@REM  que viaja (GitHub no acepta archivos de mas de 100 MB) y verifica su firma.
@REM
@REM  DGT_ONLINE=1 omite el modo offline y el repositorio embebido. Es solo para
@REM  quien PREPARA el material; el alumno no lo necesita nunca.
@REM ============================================================================
@echo off
setlocal EnableExtensions EnableDelayedExpansion

set "RAIZ=%~dp0"
if "%RAIZ:~-1%"=="\" set "RAIZ=%RAIZ:~0,-1%"
set "DIR_PROYECTO=%RAIZ%"

:buscar_raiz
if exist "%RAIZ%\tools\maven\bin\mvn.cmd" goto raiz_encontrada
for %%I in ("%RAIZ%\..") do set "PADRE=%%~fI"
if /I "%PADRE%"=="%RAIZ%" goto sin_raiz
set "RAIZ=%PADRE%"
goto buscar_raiz

:raiz_encontrada

@REM ---------------------------------------------------------------------------
@REM  El JDK que viaja en la maleta
@REM ---------------------------------------------------------------------------
set "JDK_DIR=%RAIZ%\tools\jdk\windows-x64"
if not exist "%JDK_DIR%\VERSION" goto sin_jdk

set /p JDK_VERSION=<"%JDK_DIR%\VERSION"
set /p JDK_ESPERADO=<"%JDK_DIR%\jdk.zip.sha256"
set "JDK_RUNTIME=%RAIZ%\tools\jdk\runtime\windows-x64"
set "JDK_SELLO=%JDK_RUNTIME%\.listo"
set "JAVA_HOME=%JDK_RUNTIME%\%JDK_VERSION%"

@REM Si ya esta ensamblado Y el sello es de ESTE paquete, no se toca nada.
if exist "%JDK_SELLO%" (
    set /p JDK_SELLADO=<"%JDK_SELLO%"
    if /I "!JDK_SELLADO!"=="%JDK_ESPERADO%" goto jdk_listo
)

echo [INFO]  Primera vez: ensamblando el JDK %JDK_VERSION% del repositorio...
echo         (solo ocurre una vez; despues arranca directo)

if exist "%JDK_RUNTIME%" rmdir /s /q "%JDK_RUNTIME%"
mkdir "%JDK_RUNTIME%" 2>nul

@REM `copy /b a + b destino` concatena en binario. El orden de los trozos lo fija
@REM el propio nombre (part-00, part-01, ...) y `for` los recorre ordenados.
set "JDK_TMP=%JDK_RUNTIME%\jdk.zip"
if exist "%JDK_TMP%" del /q "%JDK_TMP%"
set "LISTA="
for %%F in ("%JDK_DIR%\jdk.zip.part-*") do (
    if defined LISTA ( set "LISTA=!LISTA!+"%%~fF"" ) else ( set "LISTA="%%~fF"" )
)
copy /b !LISTA! "%JDK_TMP%" >nul
if errorlevel 1 (
    echo [ERROR] No pude juntar los trozos del JDK en %JDK_DIR%
    echo         Esta el clon completo? Prueba con un clon fresco.
    rmdir /s /q "%JDK_RUNTIME%" 2>nul
    exit /b 1
)

@REM certutil viene en todo Windows desde XP. Su salida son tres lineas: titulo,
@REM el hash, y un "completed successfully". La segunda es la que interesa.
set "JDK_REAL="
for /f "skip=1 tokens=* delims=" %%H in ('certutil -hashfile "%JDK_TMP%" SHA256') do (
    if not defined JDK_REAL set "JDK_REAL=%%H"
)
set "JDK_REAL=%JDK_REAL: =%"

if /I not "%JDK_REAL%"=="%JDK_ESPERADO%" (
    echo [ERROR] El JDK ensamblado NO coincide con su firma.
    echo           esperado: %JDK_ESPERADO%
    echo           obtenido: %JDK_REAL%
    echo.
    echo         No voy a usar un JDK que no puedo verificar. Lo mas probable es
    echo         un clon incompleto o un antivirus que toco los archivos.
    echo         Solucion: clon fresco del repositorio.
    rmdir /s /q "%JDK_RUNTIME%" 2>nul
    exit /b 1
)

@REM Por RUTA EXPLICITA, no por PATH. En cmd.exe el PATH suele resolver al tar de
@REM System32 (que es bsdtar y abre ZIP), pero "suele" no es una garantia: basta que
@REM el alumno tenga otro tar delante — Git, MSYS, chocolatey — para caer en un GNU
@REM tar que NO abre ZIP. Es exactamente lo que paso en Git Bash (SPEC-024 · A2.1).
@REM Nombrar el binario cuesta cero y cierra la puerta.
set "TAR_WIN=%SYSTEMROOT%\System32\tar.exe"
if not exist "%TAR_WIN%" (
    echo [ERROR] No encuentro %TAR_WIN%, que es lo que descomprime el JDK.
    echo         En Windows 10 y posteriores viene de fabrica. Avisale al
    echo         instructor con esta pantalla.
    rmdir /s /q "%JDK_RUNTIME%" 2>nul
    exit /b 1
)
pushd "%JDK_RUNTIME%"
"%TAR_WIN%" -xf "jdk.zip"
set "TAR_ERR=%ERRORLEVEL%"
popd
if not "%TAR_ERR%"=="0" (
    echo [ERROR] No pude extraer el JDK en %JDK_RUNTIME%
    rmdir /s /q "%JDK_RUNTIME%" 2>nul
    exit /b 1
)
del /q "%JDK_TMP%" 2>nul

if not exist "%JAVA_HOME%\bin\java.exe" (
    echo [ERROR] El JDK se extrajo pero no encuentro su java en:
    echo         %JAVA_HOME%\bin
    rmdir /s /q "%JDK_RUNTIME%" 2>nul
    exit /b 1
)

@REM > sin salto de linea final: el sello guarda el sha y nada mas.
<nul set /p ="%JDK_ESPERADO%" > "%JDK_SELLO%"
echo [INFO]  JDK %JDK_VERSION% listo en tools\jdk\runtime\

:jdk_listo
@REM Solo para ESTE proceso: el `setlocal` de arriba garantiza que ni JAVA_HOME ni
@REM PATH sobreviven al cierre. El entorno del alumno queda como estaba.
set "PATH=%JAVA_HOME%\bin;%PATH%"

:sin_jdk
set "MVN=%RAIZ%\tools\maven\bin\mvn.cmd"
if "%DGT_ONLINE%"=="1" (
    call "%MVN%" %*
) else (
    call "%MVN%" --offline "-Dmaven.repo.local=%RAIZ%\repo-maven" %*
)
exit /b %ERRORLEVEL%

:sin_raiz
echo [ERROR] No encuentro tools\maven\ subiendo desde:
echo         %DIR_PROYECTO%
echo.
echo         Este proyecto tiene que vivir DENTRO del repositorio del curso.
echo         Si copiaste la carpeta a otro sitio, vuelve a trabajar sobre el
echo         clon original.
exit /b 1
