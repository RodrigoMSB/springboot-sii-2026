# Guía 01 · La carrera por el folio

## La escena del crimen

Levanta el starter y dispara dos emisiones a la vez:

```bash
./bin/start-lab.sh --dir starter --concurrencia 2
```

Cada emisión crea un trámite nuevo y le pide un folio, **al mismo tiempo**. Mira la
salida. Tarde o temprano —a veces a la primera, a veces al tercer intento— verás una de
dos cosas:

```
     trámite 12   -> HTTP 201   folio 42
     trámite 13   -> HTTP 500   ¡EMISIÓN CAÍDA! (la carrera la reventó)
```

o bien dos folios con el mismo número. Los dos trámites leyeron «el último es el 41». Los
dos quisieron el 42. La PK del folio dejó pasar a uno y tumbó al otro: una emisión
**válida** —un contribuyente real, apretando «emitir»— terminó en un error 500.

## Por qué pasa

La emisión ingenua (`application/EmisionService.java`) hace:

```java
ContadorFolio contador = contadores.leerSinBloqueo().orElseThrow(); // lee 41
long numero = contador.siguiente();                                 // 42
contadores.save(contador);
folios.save(new Folio(numero, tramite));                            // escribe 42
```

Sin bloqueo y sin transacción, entre el «lee 41» de un hilo y su «escribe 42», otro hilo
ya leyó 41 también. Los dos escriben 42. La base te salva del duplicado con la PK — pero
"salvarte con un 500" no es una feature: es una emisión perdida.

## Lo que Carolina no puede explicar

> *«Un folio emitido dos veces no se borra. Se explica. Ante un fiscalizador. Y de paso:
> el folio 8 no existe — ¿dónde está? Los folios no se saltan.»*

Un libro foliado tiene dos reglas de hierro: **cada número existe una sola vez** (RN-01) y
**no hay huecos** (RN-02). La emisión ingenua rompe las dos bajo carrera. Y no lo verías
nunca en tu máquina probando de a un clic: solo aparece cuando dos pasan a la vez.

Por eso el test que lo prueba —`E1`— no llama a la emisión una vez. Lanza **12 hilos** que
emiten simultáneamente, coordinados por un *latch* (sin `Thread.sleep`: dormir es adivinar,
y AU-05 lo prohíbe), y exige el resultado que un libro foliado exige: folios únicos **y**
secuencia sin saltos.

Sigue con [`02-el-candado-en-el-dato.md`](02-el-candado-en-el-dato.md).
