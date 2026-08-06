# Guía del instructor · Lab 01

## Antes de la sesión (la semana previa)

1. `./bin/91-e2e.sh`. Demuestra, de una pasada, que el `starter/` está genuinamente
   incompleto (el validador sale 1) y que tiene solución (sale 0). **Si esto falla, no des
   la clase.**
2. Revisa las estaciones: los `00-verificar.sh` que te mandaron los alumnos (Lab 00). Cada
   `ESTACIÓN INCOMPLETA` es una llamada de diez minutos que ahorras hoy.
3. Ensaya el crimen. Cronometrado. En serio.

---

## ORDEN PARA CLASE (180 min)

### 🔪 00:00 – 00:10 · La escena del crimen (en vivo, tú)

**No lo cuentes. Ejecútalo.** Proyecta la terminal, letra grande.

```bash
cd labs/lab-01-del-otro-lado-del-boton/starter
```

1. *«Esto lo dejó el practicante el viernes. Compila, arranca, responde.»*
   ```bash
   cat src/main/resources/application.yml
   ```
   Déjalo en pantalla tres segundos. *«Limpio. Ni una contraseña.»*

2. *«Pero yo soy Carolina, y llevo veinte años en esto.»*
   ```bash
   git log --oneline -- src/main/resources/application.yml
   ```
   Dos commits. Lee en voz alta el nombre del segundo: **"ajustes de conexión"**.

3. **Pausa.** Deja que alguien de la sala diga *"muéstralo"*.
   ```bash
   git show dc70ed6 -- src/main/resources/application.yml
   ```
   La contraseña, proyectada. **Cállate cinco segundos.** El silencio es la clase.

4. Cierra con la frase, sin adornos:

   > *«Alguien ya la "borró". Muéstrame que entiendes por qué eso no arregló nada.»*

Y solo entonces: *«En tres horas vas a saber qué se hace de verdad. Se llama rotar.»*

> ⚠️ Aclara **una vez, ahora**, que la credencial es de utilería y el host no existe. Si no
> lo dices, alguien va a levantar la mano en el minuto 40 y perderás el hilo.

### 📚 00:10 – 00:50 · Teoría (deck del M1)

Sigue `TEORIA.md`. Los momentos que no puedes saltarte:

- **§2, la autoconfiguración.** Corre `--debug` en vivo y muestra los *Negative matches*.
  Es lo que convierte la magia en ingeniería.
- **§6, fallar rápido y claro.** Muestra el mensaje de Hikari (`'url' must start with
  "jdbc"`) y pregunta a la sala: *«¿qué variable falta?»*. Nadie lo sabrá. Ese es el punto.
- **§7, rotar.** Es lo único que se van a llevar a casa aunque olviden todo lo demás.

### ☕ 00:50 – 01:00 · Descanso

### 🔧 01:00 – 02:50 · Laboratorio

Cuatro TODOs, ~15 min cada uno. Sobra tiempo: es a propósito. Los alumnos leen mucho código
en este lab, y leer también es trabajo.

**El error que cometerá la sala** (predicción, no advertencia):

> En el TODO_2, alguien exportará `DGT_DB_URL` en una terminal y correrá `./mvnw` en otra.
> El error dirá que la variable falta. **Culpará a Spring.** Luego a Maven.

Ten esto preparado, y no lo digas antes de que ocurra:

```bash
echo $DGT_DB_URL     # en la MISMA terminal donde corres ./mvnw
```

Es la fila `L1-03` del troubleshooting. Cuando pase —y va a pasar— no des la respuesta:
pregunta *«¿en qué terminal la exportaste?»*.

**Segundo error probable:** olvidar el `@Valid` sobre el record anidado (`L1-05`), y no
entender por qué `largo: 3` no revienta. Media hora perdida si no lo ves venir.

### ✅ 02:50 – 03:00 · Cierre

1. Que corran `./bin/90-validar.sh`.
2. Recuérdales el reporte, y en particular **la transcripción literal** del mensaje de
   `prod` (§3 del reporte) y **la casilla de honestidad** (§7).
3. **Siembra del Módulo 2**, con estas palabras o parecidas:

   > *«Tu endpoint devuelve un DTO. Cuatro campos, escritos a mano. La semana que viene,
   > alguien con prisa va a decir "para qué duplicar, devuelvo la entidad". Compilará. Los
   > tests pasarán. Y en esa respuesta viajará el puntaje de riesgo que la DGT calcula sobre
   > una persona.»*
   >
   > *«El Módulo 2 se llama "El folio que se filtró".»*

---

## Qué revisar en los reportes

Léelos en este orden. El código, al final.

1. **§3, la transcripción literal.** ¿Copió el mensaje o lo resumió? Quien lo resume no lo
   leyó. Quien pega el mensaje de Hikari *y* el suyo, entendió el módulo entero.
2. **§7, la honestidad.** Un *"consulté la solución en el TODO_2 porque no entendía qué era
   un BeanFactoryPostProcessor"* vale más que un lab perfecto y mudo. **Nunca penalices un
   sí.** Si penalizas uno, no vuelves a leer un sí honesto en trece semanas.
3. **§1 y §4, el crimen y la rotación.** Busca la frase clave: *la credencial sigue en el
   historial y por eso se rota*. Si alguien escribe *"la borré del archivo"*, no aprobó el
   módulo aunque el validador diga 🏆.
4. **§8, la siembra.** Si predijo el crimen del Lab 02, ya está pensando como ingeniero.

## Feedback que forma

- Nombra **una fortaleza real** antes de las mejoras.
- Convierte cada crítica en **una acción**: no *"no entendiste los perfiles"*, sino *"corre
  `--debug` y busca `application-prod.yml` en la lista de fuentes de configuración"*.
- El criterio se entrena. Un Suficiente hoy es un Competente en el Lab 05.
