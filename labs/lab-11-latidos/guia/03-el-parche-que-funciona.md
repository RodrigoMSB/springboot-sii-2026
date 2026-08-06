# 03 · El parche que funciona (y por qué no basta)

## El arreglo obvio

Si el problema es que las dos instancias ejecutan, que ejecute solo una. Una bandera:

```yaml
# application.yml de la instancia 1
dgt:
  cierre:
    habilitado: true     # …y en la instancia 2, false
```

```java
public void latido() {
    if (!habilitado) return;
    cierre.ejecutarCierre(LocalDate.now(), instancia);
}
```

Pruébalo. **Funciona.** El cierre corre una vez, los totales cuadran, el aviso sale una sola vez.

Es un progreso real, y es donde se detiene mucha gente. Hay sistemas en producción, ahora mismo,
funcionando así.

## Las dos preguntas que lo desarman

**1. ¿Qué pasa la noche que esa instancia está caída?**

No corre **nadie**.

Y date cuenta de lo que acabas de hacer: cambiaste un problema **ruidoso** —dos cierres, que se ven
en la base y en la bandeja del contribuyente— por uno **silencioso**: ningún cierre, que no se ve
hasta que alguien pregunta por el resumen del martes. Tres días después.

El silencioso es peor. Siempre. Un fallo que grita se arregla el mismo día; uno que calla se
descubre cuando ya hizo daño.

**2. ¿Y cuando escalas a cinco réplicas en Kubernetes?**

Todas tienen la **misma** configuración. Eso *es* una réplica: el mismo contenedor, la misma imagen,
el mismo `application.yml`, multiplicado por cinco. No hay dónde poner el `false`.

Podrías inventar algo —«la instancia cuyo hostname termina en `-0`»—, y ahora tu lógica de negocio
depende del nombre que le puso el orquestador. El día que cambie el StatefulSet a Deployment, deja
de correr y nadie sabrá por qué.

## Lo que el parche revela

El parche funciona porque **tú** sabes cuántas instancias hay y cómo se llaman. Deja de funcionar en
cuanto eso lo decide otro — y en producción siempre lo decide otro.

> El candado no puede vivir en la configuración, porque la configuración es idéntica en todas las
> réplicas. Tiene que vivir donde todas miran.

## La forma correcta

Donde todas miran es **la base de datos**.

Y esto ya lo resolviste, en el Lab 06. Allí dos emisiones concurrentes se llevaban el mismo folio, y
la solución no fue `synchronized` —que solo sabe de su JVM— sino poner el candado **en el dato**.
Aquí es idéntico con otro disfraz: entonces eran dos hilos, ahora son dos servidores. La escala
cambió; el razonamiento, no.

```sql
INSERT INTO candado_tarea (nombre, tomado_por, expira_en)
VALUES (:nombre, :quien, now() + make_interval(secs => :segundos))
ON CONFLICT (nombre) DO UPDATE
   SET tomado_por = EXCLUDED.tomado_por, expira_en = EXCLUDED.expira_en
 WHERE candado_tarea.expira_en < now()
```

Tres decisiones, y las tres se te van a preguntar en el reporte:

- **Atomicidad.** Mirar y tomar son la **misma** sentencia. Escrito como `if (libre) tomar()`, entre
  las dos líneas cabe la otra instancia entera. Es la carrera del Lab 06, otra vez.
- **Expiración.** Si el que lo tomó muere a mitad del cierre —se cae el pod, alguien reinicia—, sin
  expiración el candado queda tomado por un muerto y **el cierre no vuelve a correr jamás**.
- **El reloj es el de la base.** Dos servidores con dos minutos de desfase no se ponen de acuerdo
  sobre si algo expiró. El único reloj que ambos comparten es el del motor.

Y el compromiso, que hay que declarar en vez de esconder: **si el trabajo tarda más que la
expiración, dos instancias pueden solaparse igual**. El TTL se elige con holgura sobre la duración
real. No hay bloqueo distribuido sin esta decisión.

## Lo que falta, y por qué está en el mismo lab

Con el candado, el cierre corre una vez. Lo que todavía está mal es **cómo** avisa:

- **TODO_3 · La notificación bloquea.** El contribuyente espera al servidor de correo antes de ver
  su folio. Y si el correo falla, el folio —que ya se emitió y es válido— parece haber fallado.
- **TODO_4 · La notificación miente.** Sale *dentro* de la transacción. Si esa transacción revierte,
  el folio nunca existió y el correo ya salió.

Una advertencia para el TODO_3, porque es el silencio número tres del curso: `@Async` **sin**
`@EnableAsync` no hace nada, no falla y no avisa. Igual que `@EnableCaching` en el Lab 10 y que
`@EnableScheduling` aquí mismo. A estas alturas, cuando una anotación de Spring «no hace nada»,
busca primero el `@Enable`.

Y la trampa del proxy, por **tercera vez** (`@Transactional` en el Lab 06, el aspecto en el Lab 09,
`@Async` hoy): una llamada entre métodos de la misma clase no pasa por el proxy. Ya no es mala
suerte — es cómo funciona Spring.

→ Cuando termines: `./bin/90-validar.sh --dir starter`
