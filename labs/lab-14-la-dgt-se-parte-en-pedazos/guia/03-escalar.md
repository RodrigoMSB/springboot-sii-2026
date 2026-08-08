# Bloque 3 · Escalar (~30 min)

*Dos instancias. El balanceo. Y qué pasa de verdad cuando matas una.*

---

## §1 · El guion completo

```bash
./bin/95-recuperar.sh --solo-velocidad     # deja al proveedor rápido otra vez
./bin/start-lab.sh --escalar
```

El script hace tres cosas seguidas. Míralas de una en una.

---

## §2 · Paso 1 — una sola instancia

```
  ----------  paso 1 · UNA sola instancia  ----------

       10 dgt-contribuyentes:fe4a02c6b219
```

Diez peticiones, una instancia, todo a la misma. Normal.

---

## §3 · Paso 2 — se levanta la segunda

```
[OK]    Seis piezas anotadas: la nueva instancia entró sola en el registro
[OK]    El balanceador ya reparte entre las DOS

     20 peticiones, quién atendió cada una:

         10 dgt-contribuyentes:5b549f15a43f
         10 dgt-contribuyentes:fe4a02c6b219
```

**Diez y diez, exacto.** Round-robin: por turnos.

Y ahora la pregunta que hay que hacerse:

> **¿Cuántos archivos de configuración tocaste para que eso pasara?**

Ninguno. No reiniciaste el portal. No editaste una lista de servidores. No hay un
balanceador central al que avisar. La instancia arrancó, se anotó en la guía telefónica, y
el balanceador de `dgt-tramites` se enteró solo.

**Compáralo con el monolito de las trece sesiones anteriores.** Escalarlo era levantar otra
copia entera —con su base, su cierre nocturno, su consumidor de la cola— y poner algo
delante que repartiera. Aquí escalas **la pieza que lo necesita**, y solo esa.

Esa es la ventaja de verdad de esta arquitectura, y probablemente la única que compensa
todo lo demás. Vale la pena verla funcionar antes de leer el resto de este documento.

### Un detalle que no viste, y que es media hora de tu vida en el futuro

Fíjate en que el script imprime *«El balanceador ya reparte entre las DOS»* como un paso
aparte, **después** de comprobar que el registro tiene las seis piezas. No es adorno.

Entre «el registro sabe que existe la instancia nueva» y «tu servicio le manda tráfico» hay
**dos cachés**:

1. la del cliente de Eureka — se baja la lista cada `registry-fetch-interval`;
2. la del **LoadBalancer** — guarda esa lista otros **35 segundos** por defecto.

Bajar solo la primera no sirve de nada: el balanceador sigue repartiendo con la lista
vieja. Durante la construcción de este laboratorio se midió una instancia nueva que aparecía
en el panel de Eureka desde el primer segundo y tardaba **más de medio minuto** en recibir
su primera petición.

El síntoma —*«el registro la ve, pero no le llega tráfico»*— parece un fallo del
balanceador. Es exactamente lo que se le pidió que hiciera. Está bajado a 5 s en
`config-repo/application.yml`, con la explicación entera.

Es el error de razonamiento clásico con las cachés: se optimiza la que se conoce y se
olvida la que hay detrás.

---

## §4 · Paso 3 — se mata una

Aquí es donde el laboratorio te va a llevar la contraria.

Lo que dice el guion habitual de una demo de microservicios: *«matas una instancia y el
sistema ni se entera»*. Lo que ocurre de verdad:

```
[OK]    Instancia detenida. Contando cuánto tarda el sistema en no notarlo.

     Ojo a lo que viene: NO es instantáneo, y ahí está la lección.

[OK]    El sistema se recuperó SOLO, a los ~4 s. Nadie tocó nada.
```

**Cuatro segundos de degradación.** No cero. Y con los valores por defecto de Eureka
—que este laboratorio baja a propósito— habrían sido **noventa**.

### Por qué

Tres cosas pasan a la vez, y ninguna es un fallo:

1. **El registro miente.** La instancia murió sin avisar. El registro espera a que deje de
   renovar su contrato y solo entonces la tacha. Durante esa ventana, la guía telefónica
   anuncia a un muerto.
2. **El balanceador le sigue mandando tráfico**, porque está usando esa lista. La mitad de
   tus peticiones van a un cadáver.
3. **Y aquí está lo interesante:** el circuit breaker cuenta esos fallos. Pero el circuito
   es **por servicio**, no por instancia. Así que cuando abre, deja de llamar a
   `dgt-contribuyentes` **entero** — incluida la instancia que estaba perfectamente sana.

> Tener dos instancias no te protege de que se caiga una. Te protege de que se caiga una
> **si el resto del sistema se entera a tiempo**. Y enterarse tarda.

### Lo que sí es una ventaja

Se recuperó **solo**. Nadie tocó nada, nadie recibió una llamada, nadie entró por SSH. El
sistema se degradó, aguantó y volvió.

Un monolito con el proceso muerto no hace eso. Se queda muerto hasta que alguien lo
levanta.

Esa es la frase honesta, y la que conviene llevarse:

> **«El sistema no se entera» es una verdad a medias. Se entera, sufre un rato, y se cura
> sin ayuda. Lo último es lo que un monolito no hace.**

---

## §5 · Para pensar (y para el reporte)

1. Los cuatro segundos de degradación, ¿son aceptables para la DGT? ¿Y si en vez de un
   nombre fuera el importe a pagar?
2. ¿Qué se te ocurre para acortarlos? *(Pista: hay al menos tres palancas —los relojes del
   registro, las cachés del cliente, y hacer que el fallo de una instancia no cuente
   contra el servicio entero.)*
3. Si el circuito es por servicio y castiga a la instancia sana, **¿qué harías distinto?**
   No hay respuesta única; hay respuestas defendibles. Piénsalo antes de mirar el desafío.

---

## Déjalo como estaba

```bash
./bin/95-recuperar.sh --solo-instancias
```

El bloque 4 lo hace el relator: son quince minutos, hay que apagar y levantar piezas en un
orden concreto, y la gracia está en el suspense. Guarda el teclado y mira.
