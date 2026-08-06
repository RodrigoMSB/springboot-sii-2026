# 03 · El parche que funciona (y por qué no basta)

## El arreglo obvio

Escribe un `HealthIndicator` que vaya a la base y vuelva:

```java
@Component("baseDeDatos")
public class BaseDeDatosHealthIndicator implements HealthIndicator {
    @Override
    public Health health() {
        try {
            jdbc.sql("SELECT 1").query(Integer.class).single();
            return Health.up().build();
        } catch (Exception fallo) {
            return Health.down().withDetail("causa", fallo.getMessage()).build();
        }
    }
}
```

Corre `--db-caida` otra vez. **Funciona:** `/actuator/health` ahora dice `DOWN`. El tablero dejó
de mentir sobre la base.

Es un progreso real. Y es donde se detiene mucha gente.

## Las tres preguntas que lo desarman

**1. ¿Y ahora qué hace Kubernetes con ese `DOWN`?**

Reiniciar la aplicación. Es lo único que sabe hacer cuando el health está en rojo.

Reiniciar **no levanta PostgreSQL**. Lo que consigues es tirar el tráfico en curso y arrancar un
bucle de reinicios mientras el problema sigue en otra máquina. Antes tenías una incidencia —la
base caída—. Ahora tienes dos: la base caída **y** una flota en `CrashLoopBackOff`, que además
tapa a la primera en el panel de alertas.

La aplicación no estaba rota. Estaba **sin base**. No son lo mismo, y tu health tiene que poder
decir la diferencia.

**2. ¿Y TESO? ¿Y el disco?**

Si el criterio es «todo lo que necesito para trabajar», TESO también entra. Pero el Lab 08 le
puso timeout, circuit breaker y degradación elegante **precisamente** para que la API siguiera
atendiendo con TESO caído. Meterlo en el health sacaría de rotación a toda la flota por un
servicio del que ya aprendimos a no depender: convertirías una degradación en una caída total,
por prudencia mal entendida.

**3. Chequear una cosa no es observabilidad.**

Arreglaste el agujero que te mordió hoy. El de mañana será otro — y el tablero volverá a estar en
verde mientras algo se cae, porque el problema nunca fue *qué* chequeabas: era que había **un solo
semáforo** para dos preguntas distintas.

## La forma correcta

**Separa las dos preguntas** con los grupos de Actuator:

```yaml
management:
  endpoint:
    health:
      probes:
        enabled: true
      group:
        liveness:
          include: livenessState              # ¿respira? -> reiniciar
        readiness:
          include: readinessState,baseDeDatos # ¿puede atender? -> no mandar tráfico
```

Con la base muerta:

```
/actuator/health/liveness   ->  UP     <- no reinicies: la app está bien
/actuator/health/readiness  ->  DOWN   <- no le mandes tráfico
                                           components.baseDeDatos.details.causa: ...
```

Y **nombra el componente**: el nombre del bean (`baseDeDatos`) es la clave que aparece en la
respuesta, y es lo que convierte «algo se cayó» en «se cayó la base». Añade la causa raíz en el
detalle: nombrar sin explicar deja al que llega a las 3 AM en la misma duda.

## Lo que falta, y por qué está en el mismo lab

Un tablero honesto te dice que **ya** estás mal. Lo que no hace es dejarte **verlo venir**.

- **TODO_2 · Métricas de negocio.** El CPU no te avisa que dejaste de emitir folios. Un contador
  en cero, sí.
- **TODO_3 · Exposición con criterio.** El `/env` de la guía anterior sigue abierto.
- **TODO_4 · Caché medido.** El reporte agregado es caro; cachearlo es fácil, y hacerlo sin
  invalidación es cambiar lentitud por mentira.

Una advertencia para el TODO_4, porque es el fallo más frustrante del lab: `@Cacheable` **sin**
`@EnableCaching` no hace nada, no falla y no avisa. El método sigue bajando a la base y tú crees
que tienes caché. Por eso `E4` no lee tu código: mide las estadísticas de Caffeine.

Y la trampa del proxy, por tercera vez en el curso (`@Transactional` en el Lab 06, el aspecto en
el Lab 09, el caché hoy): una llamada entre métodos de la misma clase no pasa por el proxy. A
estas alturas ya no es mala suerte — es cómo funciona Spring.

→ Cuando termines: `./bin/90-validar.sh --dir starter`
