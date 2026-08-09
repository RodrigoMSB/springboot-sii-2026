# INSTRUCTOR.md · Lab 14 — La DGT se parte en pedazos

*Sesión 14 de 14. La última. Léelo entero antes de la clase: este laboratorio no se
improvisa.*

---

## ⚠️ Lo que hay que hacer ANTES de que llegue la gente

**1. Levanta el sistema una vez, con antelación.**

```bash
cd labs/lab-14-la-dgt-se-parte-en-pedazos
./bin/start-lab.sh
```

La primera vez compila cinco proyectos y construye cinco imágenes: **4–6 minutos**. Eso no
se hace delante de la sala. Con las imágenes ya construidas, arranca en **56 s**.

**2. Avisa el día anterior de que hoy se necesita RAM.** Que cierren el IDE pesado y el
navegador de cuarenta pestañas. Son 1,54 GiB de contenedores.

**3. Ten el panel de Eureka abierto en una pestaña** (`localhost:8761`), y una terminal
grande con el tipo de letra subido. Hoy se lee mucho JSON en pantalla.

**4. Deja el sistema levantado y sano.** El crimen empieza matando una pieza, no
levantándolas.

---

## Minutado

| Bloque | Min | Acumulado | Quién |
|---|---|---|---|
| 🔪 El crimen | 10 | 0:10 | **Tú, en vivo** |
| 📚 Teoría | 35 | 0:45 | Tú |
| ☕ Descanso | 10 | 0:55 | — |
| 🔧 1 · Levantar y mirar | 20 | 1:15 | El alumno |
| 🔧 2 · Matar al proveedor ⭐ | 40 | 1:55 | El alumno |
| 🔧 3 · Escalar | 30 | 2:25 | El alumno |
| 🎬 4 · Matar al registro | 15 | 2:40 | **Demo tuya** |
| ✅ Cierre y despedida | 10 | 2:50 | Ambos |

Sobran 10 minutos. **No los rellenes:** son el colchón, y en la última sesión se gastan
solos en preguntas.

---

## 🔪 El crimen (10 min) — el guion

Sistema levantado y sano. Panel de Eureka proyectado.

**1.** Enseña el panel. *«Seis piezas. Cada una es un proceso independiente, con su
despliegue, su log y su ciclo de vida. Esto es lo que la industria llama microservicios.»*

**2.** Lanza una petición y proyéctala:

```bash
curl -s http://localhost:8099/api/v1/tramites/1
```

Señala `nombreContribuyente: "Valentina Rojas"`. *«Este nombre no está en la base de
trámites. Vino de otra pieza, por la red, mientras vosotros mirabais.»*

**3. La frase de Carolina.** Dilo con su voz:

> *«Funciona precioso. Ahora apaga el servicio de contribuyentes.»*

**4.** Ejecuta, y **no expliques nada mientras corre**:

```bash
./bin/start-lab.sh --matar-contribuyentes
```

**5.** Cuando salga el JSON degradado, **quédate callado unos segundos**. Deja que lo lean.
Alguien de la sala va a decir «sigue funcionando». Ese es el momento.

**6.** Entonces pregunta, una a una, sin responder tú:

- *«¿Qué código HTTP ha devuelto?»* — 200.
- *«¿Qué mensaje de error ha visto el usuario?»* — ninguno.
- *«¿Qué falta en esa respuesta?»* — el nombre del titular.
- *«Si tú fueras el contribuyente, ¿cómo te enterarías?»* — **silencio**.

**7.** Cierra:

> *«Un monolito caído es una pantalla en blanco: lo ves y sabes que estás jodido. Un
> sistema distribuido caído es peor — funciona a medias, y nadie sabe qué mitad. Y alguien
> va a firmar una declaración con datos incompletos sin enterarse. Hoy vais a aprender a
> mirar los pedazos.»*

**No expliques el circuit breaker todavía.** Eso es la teoría.

---

## 📚 Teoría (35 min) — reparto sugerido

| Min | Qué |
|---|---|
| 5 | §1–§2 · qué es un microservicio, y el **monolito distribuido** |
| 12 | §3 · los seis patrones, con la analogía de cada uno |
| 5 | §4 · el fallback como **decisión de negocio** |
| **10** | **§5 · cuándo NO usar microservicios** |
| 3 | §6 · estado del arte: Hystrix, Ribbon y Zuul están muertos |

**Los diez minutos del §5 son intocables.** Si vas mal de tiempo, recorta los patrones —el
alumno los va a ver funcionando en la práctica— pero no el §5. Es lo que separa a un
ingeniero de alguien que sabe configurar Spring Cloud, y probablemente lo más útil que se
lleven del curso entero.

Ten a mano la tabla de costos medidos (§5.1): 56 s de arranque frente a 8 s, 1,54 GiB
frente a 400 MiB. Los números convencen más que los adjetivos.

**Si alguien pregunta por Kubernetes:** sí, hace el discovery y el balanceo por ti, y
entonces no necesitas Eureka. No hace el circuit breaker, ni el fallback, ni decide qué
devolver cuando el vecino no contesta. Eso sigue siendo tuyo, y es de lo que va hoy.

---

## 🔧 Bloques 1–3 — dónde vas a tener que intervenir

### El error que va a cometer la sala, en el bloque 2

**Copiar los números de la solución sin entender la cuenta.** El síntoma es que alguien
pone `minimum-number-of-calls: 6` y no sabe decir por qué seis.

La pregunta que lo destapa, y que conviene hacer en voz alta a toda la sala:

> *«Con `max-attempts: 2`, ¿cuántas peticiones tuyas hacen falta para que el circuito
> registre seis llamadas?»*

Tres. Si no lo saben, no entendieron que **el circuito cuenta llamadas, no clics** — y ese
es exactamente el contenido del bloque.

### El segundo error, más sutil

**Sangría mal en el YAML.** El servicio arranca igual, Resilience4j crea la instancia con
los defaults, y la configuración simplemente no existe. No hay error en ninguna parte.

Enséñales el comando que lo destapa, y hazlo tú primero en pantalla:

```bash
cd sistema
docker compose exec -T dgt-tramites curl -s http://localhost:8081/actuator/circuitbreakers
```

Es T-7 del troubleshooting. Vale la pena proyectarlo antes de que empiecen a editar.

### En el bloque 3

Cuando lleguen al paso 3 y vean los ~4 segundos de degradación, **alguien va a decir que
está roto**. No lo está. Es la lección: el registro miente durante unos segundos, el
balanceador le hace caso, y el circuit breaker —que es por servicio, no por instancia—
acaba castigando también a la instancia sana.

Frase para cerrarlo:

> *«"El sistema no se entera" es una verdad a medias. Se entera, sufre un rato, y se cura
> sin ayuda. Lo último es lo que un monolito no hace.»*

---

## 🎬 Bloque 4 (15 min) — la demo, en dos actos

**Esto lo haces tú.** Son quince minutos, hay que apagar y levantar piezas en un orden
concreto, y la gracia está en el suspense.

### Preparación

```bash
./bin/95-recuperar.sh --solo-instancias     # dos instancias vivas
```

### Acto 1 — la intuición falla (~5 min)

Pregunta a la sala **antes** de tocar nada:

> *«Si apago el registro, ¿qué pasa? ¿Cuánto tarda en caerse el sistema?»*

Recoge dos o tres respuestas. Casi siempre dicen «se cae enseguida» o «no se encuentran».
**Anótalas en la pizarra.** Van a estar equivocadas y eso es el material del bloque.

```bash
./bin/start-lab.sh --matar-registro
```

El script apaga Eureka y sondea cada segundo durante 90 s. **No pasa nada.** El sistema
sigue entero, minuto y medio sin guía telefónica.

Explícalo:

> *«Cada cliente se había bajado una COPIA de la guía. Mientras esa copia siga siendo
> cierta, el registro puede estar muerto y a nadie le importa.»*

### Acto 2 — qué SÍ se rompió (~7 min)

El mismo script sigue solo: mata una instancia de contribuyentes **con el registro aún
apagado**.

```
     t+0  s  degradadas: 0 de 1
     t+10 s  degradadas: 7 de 11
     t+20 s  degradadas: 16 de 21

     30 segundos después: 24 de 30 peticiones degradadas.

[OK]    Y NO se cura. Esta vez no hay recuperación automática.
```

**Aquí está la lección entera, y no antes.** Compáralo en voz alta con el bloque 3: allí
esto se curaba solo en cuatro segundos. Ahora no se cura nunca.

> *«El registro NO es un punto único de fallo para el TRÁFICO. Sí lo es para el CAMBIO:
> nadie nuevo entra, nadie muerto sale. El sistema se quedó congelado en la última foto que
> alguien le sacó.»*

**No te saltes el acto 2.** Con solo el acto 1, la sala se va con la idea de que el registro
es prescindible, que es justo lo contrario de lo que hay que aprender.

### Restaurar

```bash
cd sistema && docker compose start dgt-registro && cd ..
./bin/95-recuperar.sh --solo-instancias
```

---

## 🆘 Plan B — si la máquina del alumno no aguanta

**El síntoma:** el arranque no llega a las seis piezas, o alguna JVM muere. Es T-2 del
troubleshooting.

**El dato:** el sistema completo pide **1,54 GiB** medidos, con techo de 2,94 GiB. Un
portátil de 8 GB llega bien; uno de 8 GB con el IDE, Teams y cuarenta pestañas, no.

### Plan B1 — cerrar cosas (probar esto primero)

Cerrar el IDE y el navegador suele bastar. Cuesta un minuto y no recorta el laboratorio.

### Plan B2 — cinco piezas en vez de seis

```bash
cd sistema && docker compose up -d --scale dgt-contribuyentes=1
```

Ahorra ~275 MiB. **Los bloques 1, 2 y 4 funcionan igual.** El bloque 3 se hace como demo
tuya, proyectada, y ellos miran.

Lo que se pierde: la práctica de escalar. Lo que **no** se pierde: ningún concepto —lo ven
en tu pantalla— y ninguna nota, porque el bloque 3 no entra en el criterio de aceptación.

### Plan B3 — emparejar

Dos alumnos por máquina. Es la peor opción para quien no teclea, pero en la última sesión,
donde el trabajo es sobre todo **mirar y discutir**, funciona mejor que en cualquier otro
lab del curso. Y discutir los cuatro números entre dos suele producir mejores respuestas
que decidirlos solo.

### Lo que NO hay que hacer

**No relajes los tests ni el criterio de aceptación** para que a alguien le salga verde. El
gate es el mismo para todos; lo que se adapta es el escenario, nunca la vara.

---

## Cierre (10 min) — cerrar el arco, no solo la sesión

Es la última sesión del curso. Reserva los últimos cinco minutos para el §9 de la teoría y
míralo hacia atrás con ellos:

- Empezaron con un endpoint que devolvía una lista (Lab 01).
- Terminan decidiendo si conviene partir un sistema en seis piezas.
- Por el camino aprendieron a no confiar en la entrada, ni en el reloj, ni en el vecino, ni
  en su propio tablero de salud.

La frase de cierre del curso:

> *«Lo único que os lleváis de verdad no es Spring Cloud. Es el hábito de preguntar "¿y
> esto qué pasa cuando se cae?" antes de que se caiga.»*

Y el recordatorio práctico: **`./bin/99-destruir.sh`** antes de irse. Este laboratorio deja
siete contenedores y cinco imágenes, y es el que más huella deja de todo el curso.
