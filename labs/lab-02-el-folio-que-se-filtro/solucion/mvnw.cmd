@REM ============================================================================
@REM  mvnw.cmd — el Maven de este curso (SPEC-022 · D-022-2)
@REM ----------------------------------------------------------------------------
@REM  Esto YA NO es el Maven Wrapper de Apache. El wrapper original descargaba
@REM  una distribucion de Maven de internet al primer uso, y en las maquinas del
@REM  SII no hay internet: se quedaba colgado en el primer comando del primer dia.
@REM
@REM  Este shim no descarga nada. Usa el Maven que viaja DENTRO del repositorio
@REM  (tools\maven) y el repositorio de dependencias que viaja con el
@REM  (repo-maven). Por eso funciona en modo avion.
@REM
@REM  Para ti no cambia nada:   mvnw.cmd test
@REM
@REM  DGT_ONLINE=1 omite el modo offline y el repositorio embebido. Es solo para
@REM  quien PREPARA el material; el alumno no lo necesita nunca.
@REM ============================================================================
@echo off
setlocal EnableExtensions

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
