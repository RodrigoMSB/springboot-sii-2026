# Guía del instructor · Lab 11 — latidos

## Antes de la sesión

1. `./bin/91-e2e.sh` — el starter falla los 4 TODOs, la solución pasa. Si falla, no des la clase.
2. **Docker corriendo.** Ensaya las dos pasadas del crimen, que tardan ~1 min cada una en arrancar:
   `./bin/start-lab.sh --instancias 2` y
   `./bin/99-destruir.sh && ./bin/start-lab.sh --dir solucion --instancias 2`.
3. Ojo con el tiempo: dos instancias son dos JVM. Arráncalas **antes** de empezar a hablar y deja
   el terminal a la vista mientras presentas — el latido cae a los ~8 segundos del arranque.

## ORDEN PARA CLASE (180 min)

### 🔪 00:00 – 00:10 · La escena del crimen (en vivo)

```bash
./bin/start-lab.sh --instancias 2
```

El script arranca las dos instancias, espera el latido y consulta la base. Lo que sale:

```
Cierres registrados HOY en la base:
     id=1  instancia=dgt-1  tramites=5  total=15375000
     id=2  instancia=dgt-2  tramites=5  total=15375000
```

Deja esas dos líneas en pantalla. **Que alguien de la sala lea los dos `instancia=`.** Ese es todo
el crimen: mismo día, mismo trabajo, dos veces.

Y remata conectándolo con el lab anterior: fue el **contador del Lab 10** el que lo destapó. Sin
métricas, esto se descubre cuando llama un contribuyente enojado.

> *«Dos servidores, y los dos se creyeron el único.»*

### 📚 00:10 – 00:50 · Teoría

`TEORIA.md`. Imprescindibles:

- **§3, `fixedRate` vs `fixedDelay`.** Dibuja el diagrama del solapamiento en la pizarra. Es de las
  pocas cosas del curso que se entienden mejor con dos rayas que con un párrafo.
- **§5, el parche de la bandera.** Dedícale tiempo: es el acto 2 y **funciona**. Pregunta a la sala
  qué pasa la noche que esa instancia está caída, y espera a que alguien lo diga.
- **§6, el bloqueo distribuido.** Amárralo explícitamente al Lab 06: *«esto ya lo resolvimos, con
  otro disfraz»*. Los tres puntos —atomicidad, expiración, el reloj de quién— son el corazón.
- **§7, hilos virtuales.** Insiste en lo que **no** cambian: el límite del otro lado sigue ahí.
- **§9, `AFTER_COMMIT`.** «Avisar de algo que no ocurrió es peor que no avisar.»
- **§8, la trampa del proxy — tercera vez.** Aquí ya no la expliques tú: pregunta *«¿por qué esto no
  va a funcionar?»* y deja que la sala la reconozca sola. Si la reconocen, el arco cerró.

### ☕ 00:50 – 01:00 · Descanso

### 🔧 01:00 – 02:50 · Laboratorio

**El error que cometerá la sala — por orden de probabilidad:**

1. **El candado con dos sentencias.** `if (estaLibre()) tomar();`. Es lo natural, y es la carrera
   del Lab 06 otra vez. `E2` lo caza con ocho hilos. Cuando pase, pregunta: *«¿cuánto dura el hueco
   entre tus dos líneas?»*
2. **Olvidar la expiración.** Todo verde… hasta el tercer test. Aprovecha: *«tu candado funciona.
   Ahora mata la instancia a mitad del cierre. ¿Cuándo vuelve a correr?»* La respuesta es «nunca», y
   duele.
3. **`@Async` sin `@EnableAsync`.** No falla, no avisa, corre síncrono. Van tres veces en el curso
   con este mismo silencio (caché, scheduling, async): nómbralo como patrón.
4. **Liberar el candado fuera del `finally`.** Funciona hasta que el cierre lanza una excepción.
5. **Poner `AFTER_COMMIT` y esperar que el listener escriba en la base.** Ahí ya no hay transacción.
   Es un buen momento para el §9.

### ✅ 02:50 – 03:00 · Cierre — LA SIEMBRA DE LOS AVISOS PERDIDOS

Corre `90`, pide el reporte, y siembra el Lab 12 (`TEORIA §10`):

> *«El cierre ya corre una vez y las notificaciones no bloquean a nadie. Pero anoche el servicio de
> notificaciones estuvo caído dos horas, y esos avisos no existen: se perdieron en el aire.»*

El gancho está **en su propio código**: el notificador guarda lo enviado en una cola en memoria.
Muéstrasela. `@Async` mueve el trabajo a otro hilo; no lo hace sobrevivir a un reinicio.

## Qué revisar en los reportes

1. **§1, la doble ejecución.** La pregunta que separa: *¿cómo supiste que eran dos ejecuciones y no
   una registrada dos veces?* La respuesta buena nombra los dos `traceId` distintos (Lab 09) o los
   dos `instancia=`. Si dice «porque había dos líneas», no entendió la diferencia.
2. **§2, el parche de la bandera.** ¿Nombró **los dos** fallos? El de «esa instancia se cae» lo ve
   casi todo el mundo; el de «cinco réplicas idénticas» lo ve menos gente, y es el que importa en
   producción.
3. **§3, el candado.** ¿Por qué una sola sentencia? ¿Y el reloj de quién? Si no supo responder lo
   del reloj, vuelve al §6: es lo que separa entenderlo de copiarlo.
4. **§4, `AFTER_COMMIT`.** ¿Qué se pierde al cruzar la frontera del commit?
5. **§5, honestidad.** Nunca penalices un «usé `--todo`».
