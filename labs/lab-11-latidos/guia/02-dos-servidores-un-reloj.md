# 02 · Dos servidores, un reloj

## Vívelo

```bash
cd ..
./bin/start-lab.sh --instancias 2
```

El script levanta **dos JVM de la misma aplicación**, en dos puertos, contra **una sola base**. No
es una simulación aproximada: es exactamente lo que hace Kubernetes cuando escalas a 2 — mismo
código, misma configuración, misma base, distinto proceso.

Lo que verás:

```
Cierres registrados HOY en la base:
     id=1  instancia=dgt-1  tramites=5  total=15375000
     id=2  instancia=dgt-2  tramites=5  total=15375000
```

Compáralo con tu predicción. Y fíjate en la tercera pregunta: **ninguna instancia dio error**. Las
dos hicieron su trabajo, correctamente, sin quejarse. Ese es el problema.

## Míralo en el log

```bash
grep "Cierre nocturno" .estado/dgt-1.log
grep "Cierre nocturno" .estado/dgt-2.log
```

Dos ejecuciones, cada una con su `traceId` distinto — el del Lab 09. Y esa es la pregunta de
criterio que va en tu reporte:

> **¿Cómo supiste que eran dos ejecuciones y no una que se registró dos veces?**

Piénsala. La respuesta está en lo que construiste hace dos sesiones.

## Por qué pasa

`@Scheduled` es **local a la JVM**. Cada instancia tiene su planificador, y ninguno sabe que el otro
existe. Con N instancias, N ejecuciones. No hay coordinación porque nadie la escribió.

> «Una vez al día» no significa nada hasta que alguien lo garantice. Y ese alguien no puede ser un
> archivo de configuración — lo verás en la guía siguiente.

## Y hay un segundo problema en el mismo archivo

Mira la anotación:

```java
@Scheduled(fixedRateString = "${dgt.cierre.intervalo-ms}")
```

`fixedRate` mide de **inicio a inicio**. Si el trabajo dura más que el intervalo, la siguiente
ejecución sale igual — y `application.yml` declara un pool de **4 hilos**, así que se solapan. Dos
cierres a la vez **en la misma instancia**, sin necesidad de un segundo servidor.

Hoy el cierre tarda milisegundos y no se nota. El día que la DGT lleve diez años de declaraciones,
sí.

Y un tercero, más silencioso todavía: el cron de producción no declara zona horaria. Búscalo.

## El daño real

Los totales duplicados se arreglan con un `DELETE`. El aviso duplicado, no: ya salió. Y hay algo
peor que el ruido — si mañana el cierre hiciera un cargo, o cerrara un período, ejecutarlo dos veces
no sería una molestia: sería dinero.

**La tarea no tiene que correr «una vez al día». Tiene que correr una vez, punto.**

→ **[03 · El parche que funciona (y por qué no basta)](03-el-parche-que-funciona.md)**
