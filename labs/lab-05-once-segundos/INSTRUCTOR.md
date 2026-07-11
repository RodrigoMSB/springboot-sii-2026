# Guía del instructor · Lab 05 — el clímax

## Antes de la sesión

1. `./bin/91-e2e.sh` — starter falla el contador, solución pasa. Si falla, no des la clase.
2. **Docker corriendo**, y ensaya la siembra: `--lotes 5000` tarda unos segundos.
3. Este es EL lab del curso. Ensaya el cronómetro en vivo hasta que salga limpio.

## ORDEN PARA CLASE (180 min)

### 🔪 00:00 – 00:10 · La escena del crimen (en vivo, cronometrada)

Proyecta la terminal. Siembra y cronometra, **de verdad, en vivo**:

```bash
./bin/start-lab.sh --dir starter --lotes 5000
{ time curl -s -o /dev/null "http://localhost:8099/api/v1/tramites?page=0&size=5000"; }
grep -c '    select' .estado/dgt.log
```

Que la sala vea el cursor parpadear los segundos. Que vea el número de consultas (miles). No
expliques todavía. Deja que el silencio y el número hablen. Y entonces, Carolina:

> *«Ayer el listado tardó once segundos. Hoy, veintitrés. No agregamos código: agregamos
> trámites. No quiero oír la palabra 'optimizar' hasta que me muestres un número.»*

### 📚 00:10 – 00:50 · Teoría

`TEORIA.md`. Imprescindibles:
- **§2, medir no confiar.** Muestra el contador (`E1`) en vivo.
- **§3, el acto 2.** EAGER baja el contador a 1 — muéstralo — y luego AU-04 cazándolo. La
  métrica no es "menos consultas".
- **§7, la mentira de H2.** Con el caso REAL: nuestras migraciones son PostgreSQL puro, H2 ni
  arranca. Mejor argumento imposible.

### ☕ 00:50 – 01:00 · Descanso

### 🔧 01:00 – 02:50 · Laboratorio

**El error que cometerá la sala:** intentarán EAGER (baja el contador). AU-04 los caza. NO des
la respuesta — pregunta *«¿esa 1 consulta es mejor que las N? ¿Qué trae?»*. Que descubran el
producto cartesiano.

**Segundo:** al hacer la proyección, alguien romperá `E2` (cambiará el shape). Recuérdales:
refactorizar es mismo comportamiento, distinto costo. `E2` es el juez de "mismo comportamiento".

Muestra las **dos soluciones** (`solucion-con-n1/` y `solucion/`) corriendo el `90` sobre cada
una: misma funcionalidad, distinto contador. Esa es la lección del lab en una imagen.

### ✅ 02:50 – 03:00 · Cierre — LA SIEMBRA DEL FISCALIZADOR

`90`, reporte, y la siembra del Lab 06 (`TEORIA §11`). Léela casi textual:

> *«El cronómetro ya no es tu problema. Pero mira el folio: un número que la regla dice
> irrepetible. La próxima semana, dos contribuyentes aprietan 'emitir' en el mismo
> milisegundo, los dos leen "el último es el 41", los dos escriben el 42. Dos declaraciones,
> el mismo folio. No es un bug de rendimiento — es un bug que solo aparece cuando dos cosas
> pasan a la vez, y no lo ves con un cronómetro. El cronómetro se cambia por un fiscalizador.
> "Un folio emitido dos veces no se borra. Se explica." Carolina se los dijo el primer día.»*

## Qué revisar en los reportes

1. **§1, el número.** ¿Transcribió los segundos Y las consultas?
2. **§2, el acto 2.** ¿Entendió por qué "menos consultas" no es la métrica? Es el corazón.
3. **§4, las dos soluciones.** ¿Vio que la diferencia es solo el costo?
4. **§7, la siembra.** ¿Anticipa la carrera del folio? Si lo hace, ya piensa en concurrencia.
5. **§6, honestidad.** Nunca penalices un "sí".
