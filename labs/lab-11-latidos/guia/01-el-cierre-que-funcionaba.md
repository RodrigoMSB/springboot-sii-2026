# 01 · El cierre que funcionaba

## Dónde estamos

El Lab 10 dejó el tablero diciendo la verdad y midiendo lo que importa. Funcionó de inmediato: el
contador de cierres del último viernes marcaba **2**.

Debía marcar 1.

## El encargo de Carolina

> *«El viernes el cierre corrió dos veces y a doscientos contribuyentes les llegó el mismo aviso
> duplicado. Dos servidores, y los dos se creyeron el único. El reloj no tiene la culpa: la culpa
> es de quien programó una tarea sin preguntarse cuántos la iban a escuchar.»*

Fíjate en que **el código no está roto**. Léelo entero:

```java
@Scheduled(fixedRateString = "...")
public void latido() {
    cierre.ejecutarCierre(LocalDate.now(), instancia);
}
```

No hay un bug ahí. Con una instancia hace exactamente lo que dice. Funcionó meses.

Se rompió el día que operaciones escaló a dos servidores para aguantar la carga — y esa persona no
tocó una línea de este archivo. **El código no cambió; cambió el mundo en el que corre.** Ese es el
tipo de fallo más caro que hay, porque no aparece en ninguna revisión de código: no hay nada que
revisar.

## Lo que hay hoy

Tres piezas, y conviene que las mires antes de romper nada:

| Archivo | Qué hace |
|---|---|
| `config/scheduling/CierreNocturnoJob.java` | El reloj. Dispara el cierre. |
| `application/CierreService.java` | El trabajo: consolida el día y avisa. |
| `application/NotificadorService.java` | El aviso al contribuyente. |

Están separados a propósito: el que sabe **hacer** el trabajo no sabe **cuándo** hacerlo, ni cuántos
tienen derecho a hacerlo. Vas a agradecerlo en el TODO_2.

## Antes de seguir: predice

Con **dos** instancias de la aplicación corriendo contra **la misma** base, escribe tu apuesta:

| | Tu predicción |
|---|---|
| ¿Cuántas filas en `cierre_diario` para hoy? | ______ |
| ¿Cuántos avisos recibe el mismo contribuyente? | ______ |
| ¿Alguna de las dos instancias da error? | ______ |

La tercera es la interesante. Guárdala.

→ **[02 · Dos servidores, un reloj](02-dos-servidores-un-reloj.md)**
