# 01 · El aviso al aire

## Dónde estamos

El Lab 11 sacó la notificación del hilo de la petición. El contribuyente ya no espera al servidor de
correo, y si el correo tarda, su folio llega igual. Progreso real.

Pero mira dónde acaba el aviso:

```java
@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
public void alEmitirseUnFolio(FolioEmitido evento) {
    notificador.notificar(evento.rut(), "Su folio N° " + evento.numero() + " fue emitido");
}
```

Una llamada. En memoria. A un objeto del mismo proceso.

`@Async` hizo exactamente lo que promete: mover el trabajo a otro hilo. **Mover no es guardar.** Un
hilo distinto sigue siendo memoria del mismo proceso, y la memoria de un proceso se va con el
proceso.

## El encargo de Carolina

> *«Anoche el servicio de avisos estuvo caído dos horas. Doscientos contribuyentes tienen su folio y
> ninguno lo sabe. Y lo peor: nadie puede decirme cuáles doscientos, porque esos avisos no quedaron
> en ninguna parte. Un aviso que se pierde en silencio es peor que un error: el error, al menos, se
> ve.»*

Son **dos** encargos, y el segundo es el que la tiene despierta:

1. Que no se pierdan.
2. Que quede **lista** de los que estaban pendientes.

Fíjate en que el segundo no se arregla con reintentos. Se arregla teniendo el mensaje **en algún
sitio**.

## Lo que hay hoy

| Pieza | Qué hace |
|---|---|
| `EscuchaDeFolios` | Reacciona al folio emitido, tras el commit |
| `NotificadorService` | «Envía» el aviso. Es el doble de laboratorio del servidor de correo |
| `TesoreriaAdapter` | La llamada síncrona a TESO que queda, del Lab 08 |

El `AFTER_COMMIT` está bien y **no se toca**: sigue siendo lo que impide avisar de un folio que no
existe. Lo que está mal es a quién se le entrega el aviso.

## Antes de seguir: predice

Con el servicio de avisos caído, se emiten 3 folios. Escribe tu apuesta:

| | Tu predicción |
|---|---|
| ¿Qué responde la API? | HTTP ______ |
| ¿Cuántos errores ves en pantalla? | ______ |
| Cuando el servicio vuelva, ¿llegan los 3 avisos? | ______ |
| ¿Podrías decir **cuáles** contribuyentes se quedaron sin aviso? | ______ |

La segunda es la que más gente falla. La cuarta es la que le importa a Carolina.

→ **[02 · El aviso que se evaporó](02-el-aviso-que-se-evaporo.md)**
