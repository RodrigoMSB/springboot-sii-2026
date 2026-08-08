# Por qué este curso se enseña así

*Manifiesto pedagógico del Taller de Spring Boot. Léelo antes de dictar una sola sesión. Si no compartes lo que viene, este material no va a rendir en tus manos, porque no está pensado para dictarse como un curso normal.*

---

Hubo un tiempo en que saber era acordarse. El que llevaba la sentencia en la cabeza valía más que el que iba a buscarla al libro, porque el libro pesaba y estaba lejos. Ese tiempo se terminó mientras nosotros trabajábamos, sin aviso y sin ceremonia. Un día la máquina escribía peor que nosotros y al día siguiente escribía mejor, y nadie tocó una campana.

Enseñar como si eso no hubiera pasado es honrado y es inútil.

## La tesis, en una frase

Ya no vale enseñar la sentencia exacta. Vale enseñar el criterio para saber cuál pedir, y para darte cuenta cuando la máquina te la dio mal.

La IA escribe sintaxis mejor que cualquiera de nosotros, y esa distancia solo va a crecer. Memorizar la firma de `@Transactional`, el orden de los parámetros de un `@EntityGraph` o la anotación precisa de Mockito sirve hoy lo mismo que memorizar tablas de logaritmos después de la calculadora. Nadie llora esas tablas. Lo que la máquina todavía no hace por ti es avisarte cuándo `REQUIRES_NEW` te va a dejar huecos en un libro de folios, ni decirte por qué un `@JsonIgnore` que limpia el JSON no arregla nada de fondo. Eso es criterio, y el criterio es lo único que este curso persigue.

Formamos ingenieros que auditan a la máquina. No que compiten con ella en lo que ella ya ganó.

---

## Las cuatro convicciones que ordenan el material

### 1. El concepto por encima de la receta

Cada sesión persigue un modelo mental, no una lista de pasos. El alumno no sale sabiendo cómo se escribe un HealthIndicator. Sale sabiendo por qué un health que solo dice "el proceso está vivo" es una mentira piadosa que alguien va a creer a la hora equivocada. Lo primero lo genera la IA en tres segundos. Lo segundo es lo que lo hace empleable.

La receta se copia. El paladar no. Puedes darle a cualquiera la lista de ingredientes y las cantidades exactas, y aun así solo unos pocos van a saber que a esa olla le falta algo antes de probarla.

El control de calidad es simple y despiadado. Si le quitas la historia y el concepto no queda en pie solo, el material falló. La narrativa es el vehículo y el concepto es el destino, nunca al revés.

### 2. La memoria humana es narrativa, no indexada

Nadie recuerda una lista de viñetas. Todos recuerdan una historia con tensión. El cerebro no archiva por orden alfabético, archiva por lo que le dolió.

Por eso el curso vive dentro de un mundo, con la DGT, Carolina, el fiscalizador y el folio que se emitió dos veces, en vez de vivir dentro de un temario. En otros cursos aplicamos el mismo principio, con el puerto de Siracusa en Python o la panadería con Kafka. La historia es tecnología pedagógica, no decorado.

Cuando el alumno esté en producción a las tres de la mañana frente a un N+1, con la sala a oscuras y el teléfono sonando, no va a recordar la lámina catorce. Va a recordar los once segundos y la cara de Carolina exigiendo un número. La emoción es el índice con que el cerebro recupera el concepto, y nosotros lo usamos a propósito.

### 3. El aprendizaje ocurre en la sala, o no ocurre

Esta es la convicción más incómoda y también la más honesta. Todo tiene que pasar mientras el alumno está en clase. Después de la clase, el alumno no vuelve a abrir el curso.

No tiene tiempo, no tiene energía y tiene una vida. Diseñar como si fuera a estudiar el fin de semana es diseñar para un alumno que no existe, y ese alumno imaginario ha arruinado más cursos que la mala materia. Las tres horas de la sesión son una ventana que se abre una sola vez y se cierra sola. Todo el material está construido para exprimirla.

- **El crimen se vive** en los primeros diez minutos. No se cuenta después.
- **Los TODO se miden** en minutos de tecleo real, para que quepan en la sesión.
- **El deck es chico**, para mirarlo de reojo mientras se piensa. No para leerlo en casa.
- **El libro del alumno** es una referencia de escritorio para usar en clase, no un texto de estudio.

Si una pieza del material solo sirve para repasar después, esa pieza está mal diseñada. No hay después.

### 4. Un piso de sintaxis para poder conversar con la máquina

El concepto manda, pero hay un mínimo de fluidez que el alumno necesita en los dedos. Sin ese piso no puede pedirle bien a la IA ni auditar lo que le entrega. El que no tiene la palabra no tiene la cosa. Quien no sabe qué es un `SecurityFilterChain` no sabe pedirlo, y menos va a notar si se lo dieron mal.

Por eso el alumno sí teclea lo esencial, aunque lea el resto. La línea que cuidamos en cada lab es enseñar el concepto sin dar por supuesta una fluidez que todavía no tiene.

---

## Qué le pedimos a quien dicta

Este método rinde más que el tradicional, pero exige un relator que lo entienda. Un relator promedio puede convertir la escena del crimen en una anécdota sin sustancia, y ahí el curso se derrumba a un tutorial más. El método es afilado y por eso mismo corta para los dos lados.

El material de instructor no es un extra. El minutado, las coreografías de pizarra y el error que cometerá la sala son lo que hace transferible el método a alguien que no sea su autor. Si vas a dictar este curso, tu trabajo no es leer láminas. Tu trabajo es otro.

- **Abre con el crimen**, en vivo, y deja que la sala sienta el problema antes de dar una sola respuesta.
- **Deja que se equivoquen** en el parche fácil. El acto dos de cada lab funciona a propósito, y recién ahí muestras por qué está mal.
- **No des la sentencia de entrada.** Que la pidan, que la busquen, que la peguen mal y que la corrijan. El forcejeo es el aprendizaje, y el que le ahorra el forcejeo a la sala le está ahorrando también lo único que se iba a llevar.
- **Cuenta la historia como si te importara**, porque es el índice con que van a recuperar todo lo demás.

---

## La medida del éxito

No medimos si el alumno escribe el código de memoria, porque la IA lo hace mejor. Medimos si seis meses después, frente a un problema que nunca vio, reconoce cuál es el problema y sabe qué pedir. Si frente a un listado lento piensa "¿cuántas consultas está haciendo esto?" antes de tocar nada, el curso funcionó.

Ese reflejo es todo lo que dejamos instalado, y también es todo lo que hace falta.

Los frameworks envejecen. Las versiones se suceden y las anotaciones cambian de nombre. La máquina que hoy nos asombra va a parecer torpe en unos años, como nos parecen torpes las tablas que alguien memorizó con esmero. Enseñamos la capa que queda debajo de todo eso, la que no caduca cuando cambia la herramienta. Enseñamos a mirar.
