# 02 · El aviso que se evaporó

## Vívelo

```bash
cd ..
./bin/start-lab.sh --avisos-caidos
```

El guion tiene dos fases, y el crimen solo se ve en el contraste.

**Fase 1 — el servicio de avisos está caído.** Se emiten tres folios:

```
     POST /api/v1/tramites/6/folio  ->  HTTP 201
     POST /api/v1/tramites/7/folio  ->  HTTP 201
     POST /api/v1/tramites/8/folio  ->  HTTP 201

     avisos entregados             ->  0
     mensajes esperando en la cola ->  0
```

**Fase 2 — se levanta el servicio de avisos.** El script reinicia la aplicación. Y no llega nada.

Compáralo con tu predicción.

## Lo que hay que ver aquí

**¿Qué falló?** Nada. Y ese es el problema.

Tres `201`. Cero excepciones en el log de la petición. Cero alertas. Un sistema que, mirado desde
fuera, funcionó perfectamente — mientras tres contribuyentes se quedaban sin enterarse de que
tienen folio.

> Un aviso que se pierde en silencio es peor que un error. El error se ve, alguien lo mira, alguien
> lo arregla. El silencio no tiene a quién despertar.

Y la segunda mitad, que es la que Carolina puso primero: **no hay lista**. No es solo que los tres
avisos se perdieran; es que no queda ningún sitio donde mirar para saber cuáles eran. Ni para
reenviarlos a mano.

## Por qué pasa

`NotificadorService` es un objeto en memoria. Cuando el destino no contesta, el método lanza; como
es `@Async`, esa excepción no vuelve a nadie —el llamador siguió su camino hace rato— y el
manejador la registra y se acabó.

El aviso existió durante unos milisegundos, en la memoria de un proceso, y desapareció.

**Mover el trabajo a otro hilo no lo hace sobrevivir a nada.** Un hilo distinto sigue siendo el
mismo proceso. Un reinicio, un despliegue, un pod que se recicla: todos se llevan lo que hubiera en
memoria.

## La frase del módulo

> Una llamada directa exige que el otro esté vivo **en el mismo instante**.
> Una cola solo exige que **exista**.

Dos sistemas rara vez tienen el mismo ritmo. La DGT emite folios a ráfagas; el servidor de correo
procesa a su paso, y a veces no procesa. Una llamada directa obliga a que los dos ritmos coincidan.
Una cola los separa y absorbe la diferencia.

Eso es un **amortiguador**.

## Míralo tú mismo

Corre la solución y compara la fase 2:

```bash
./bin/99-destruir.sh && ./bin/start-lab.sh --dir solucion --avisos-caidos
```

Los tres mensajes estaban en la cola —**sobrevivieron incluso al reinicio de la aplicación**— y se
entregan solos en cuanto hay quien los consuma. Nadie reenvió nada a mano.

Y ábrela por dentro mientras corre: <http://localhost:15672> (`dgt` / `dgt-dev`). Ver los mensajes
esperando en una pantalla enseña más que cualquier párrafo.

→ **[03 · El parche que funciona (y por qué no basta)](03-el-parche-que-funciona.md)**
