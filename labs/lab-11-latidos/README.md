# Lab 11 · Latidos

> *«El viernes el cierre corrió dos veces y a doscientos contribuyentes les llegó el mismo aviso
> duplicado. Dos servidores, y los dos se creyeron el único. El reloj no tiene la culpa: la culpa
> es de quien programó una tarea sin preguntarse cuántos la iban a escuchar.»*
>
> — **Carolina Espinoza**

El Lab 10 dejó el tablero diciendo la verdad. Y midiendo, apareció esto: el contador de cierres
marcaba **2** donde debía marcar 1.

`@Scheduled` es local a la JVM. En un mundo de muchas instancias, **«una vez al día» no significa
nada hasta que alguien lo garantice** — y ese alguien no puede ser un archivo de configuración.

**Sesión 11** · Módulo 10 (procesamiento asíncrono, tareas programadas y eventos, 3 h) · **3 horas**

---

## Lo que vas a hacer

| | Actividad | Tiempo |
|---|---|---|
| 🔪 | **La escena del crimen** — el cierre ejecutándose dos veces | 10 min |
| 📚 | Teoría: scheduling, bloqueo distribuido, hilos virtuales, eventos | ~40 min |
| ☕ | Descanso | 10 min |
| 🔧 | **Laboratorio: los TODOs** | ~110 min |
| ✅ | Validar, reporte, y la siembra del Lab 12 | 10 min |

## Los TODOs

| | Qué | Dónde | ≈ |
|---|---|---|---|
| **TODO_1** | `fixedDelay` en vez de `fixedRate`, y el cron con zona horaria | `config/scheduling/` | 10 min |
| **TODO_2** | El candado distribuido: tomar o irse, con expiración | `application/CandadoDistribuido.java` | 18 min |
| **TODO_3** | `@Async` sobre hilos virtuales de Java 25 | `application/NotificadorService.java` | 12 min |
| **TODO_4** | El evento transaccional: avisar solo si hubo commit | `application/evento/` | 10 min |

Los cuatro tests del enunciado —`E1` a `E4`— no los escribes: los lees. `E<n>` corresponde a
`TODO_<n>`. Verifican que el latido no pueda solaparse, que **ocho instancias produzcan un solo
cierre**, que el aviso salga en un hilo virtual sin bloquear la petición, y que un rollback deje al
contribuyente **sin** el aviso de un folio que nunca existió.

## El pre-vuelo — mira correr el cierre dos veces

```bash
cd ..
./bin/start-lab.sh --instancias 2
```

Levanta **dos instancias** de la misma aplicación, en dos puertos, contra **una sola base** — que
es exactamente lo que hace Kubernetes cuando escalas a 2. Verás dos filas de cierre para el mismo
día, con dos instancias distintas, y los totales duplicados.

Corre lo mismo con `--dir solucion` y compara: **un solo cierre**, y la instancia que no ganó
diciéndolo en su log — *«no tomé el candado, otra instancia lo tiene. Me voy a dormir.»*

Este lab **necesita Docker**. Cuando termines: `./bin/90-validar.sh --dir starter`.

## Para el Instructor

`./bin/91-e2e.sh` antes de la sesión. El guion está en [`INSTRUCTOR.md`](INSTRUCTOR.md).
La siembra del Lab 12 —los avisos que se perdieron en el aire— está en `TEORIA.md §10`.
