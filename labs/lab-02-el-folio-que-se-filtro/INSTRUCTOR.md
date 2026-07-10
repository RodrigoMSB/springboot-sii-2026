# Guía del instructor · Lab 02

## Antes de la sesión

1. `./bin/91-e2e.sh` — starter incompleto + solución 100%. Si falla, no des la clase.
2. **Ten Docker corriendo.** Este lab lo necesita; si tu propia máquina falla en el `curl`
   del crimen delante de 18 personas, ninguna guía te salva.

## ORDEN PARA CLASE (180 min)

### 🔪 00:00 – 00:10 · La escena del crimen (en vivo)

Proyecta la terminal. Levanta el starter y pide la ficha:

```bash
cd starter && ./mvnw spring-boot:run
curl http://localhost:8099/api/v1/contribuyentes/12345678-5/ficha
```

```json
{"rut":"12345678-5","razonSocial":"Comercial Andina SpA","puntajeRiesgoInterno":67,"id":2}
```

Señala el `67` con el cursor. **Silencio tres segundos.** *«Ese número decide a quién
fiscaliza la DGT. Acaba de salir a quien sepa un RUT.»*

Abre `FichaController.java`. Muestra el `return ResponseEntity.ok(c)`. *«Nadie lo hizo con
malicia. Estaba para ayer.»* Y cierra:

> *«No te llamé porque hay un bug. Te llamé porque nada lo impidió. Arréglalo — y después
> haz que sea imposible repetirlo.»*

### 📚 00:10 – 00:50 · Teoría (M2 resto + M3 mitad)

`TEORIA.md`. Los momentos que no puedes saltar:
- **§2, lista negra vs blanca.** Muestra en vivo el parche `@JsonIgnore` y el `id` que se
  escapa. Es el Acto 2, y es el más contraintuitivo.
- **§6, los guardianes y la trampa del genérico.** Escribe `haveRawReturnType` en la pizarra,
  muéstralo pasar en verde con el crimen presente, y luego `dependOnClassesThat()` cazándolo.

### ☕ 00:50 – 01:00 · Descanso

### 🔧 01:00 – 02:50 · Laboratorio

**El error que cometerá la sala** (predicción): al llegar al TODO_3, alguien escribirá AU-02
correctamente **sin haber hecho el TODO_1**, y `T3` fallará en "pasa sobre producción". Se
frustrará: *"¡pero mi regla está bien!"*. Y sí lo está — lo que falta es que su propio
controlador dejó de devolver la entidad. Es la fila `L2-02`. **No des la respuesta:** pregunta
*«¿qué está cazando tu guardián?»* y deja que lo descubra. Ese momento —el guardián cazando a
su propio autor— es la lección entera del lab.

**Segundo error probable:** `haveRawReturnType` (L2-03). Pasa todo menos T3. Recuérdales el
spike.

### ✅ 02:50 – 03:00 · Cierre

`./bin/90-validar.sh`, reporte, y la **siembra del Módulo 3**:

> *«Tus guardianes vigilan la estructura: que la web no toque la entidad. Pero un código con
> las capas perfectas puede calcular mal un folio. ¿Quién vigila el comportamiento? La
> próxima semana la suite te llega en rojo — catorce tests. Y no son un examen: son el
> enunciado.»*

## Qué revisar en los reportes

1. **§2** — ¿entendió por qué `@JsonIgnore` no basta? Busca "lista negra" / "campo nuevo nace
   expuesto". Quien solo dice "lo escondí" no aprobó el concepto.
2. **§4, la transcripción del mensaje de ArchUnit** — ¿copió el `with type argument depending
   on`? Esa frase es la prueba de que entendió la trampa del genérico.
3. **§7, honestidad.** Nunca penalices un "sí". 
4. **§8, la siembra.** Si predijo que los guardianes no ven el comportamiento, ya piensa como
   ingeniero.
