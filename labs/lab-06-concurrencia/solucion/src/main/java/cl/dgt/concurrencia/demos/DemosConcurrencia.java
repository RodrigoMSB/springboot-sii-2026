package cl.dgt.concurrencia.demos;

import cl.dgt.concurrencia.entities.Folio;
import cl.dgt.concurrencia.repositories.FolioRepository;
import cl.dgt.concurrencia.servicios.EmisorDeFolios;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.IntFunction;
import java.util.stream.Collectors;

/**
 * Las tres demos del laboratorio.
 *
 * <p>La 1 emite folios de uno en uno y sale bien. La 2 emite veinte a la vez con el mismo código y
 * sale mal. La 3 emite veinte a la vez con una línea más y vuelve a salir bien.
 *
 * <p>Lo único que cambia entre la 1 y la 2 es <strong>cuántos a la vez</strong>. Ese es el
 * laboratorio.
 */
@Component
public class DemosConcurrencia {

    /** El año que se usa en todas las demos. Fijo, para que los números se repitan. */
    private static final int ANIO = 2026;

    /** Cuántas emisiones simultáneas. Veinte bastan para que la carrera se vea siempre. */
    private static final int EN_PARALELO = 20;

    private final EmisorDeFolios emisor;
    private final FolioRepository folios;

    public DemosConcurrencia(EmisorDeFolios emisor, FolioRepository folios) {
        this.emisor = emisor;
        this.folios = folios;
    }

    // =========================================================================
    //  1 · DE UNO EN UNO
    // -------------------------------------------------------------------------
    //  Diez emisiones, una detrás de otra, con el emisor ingenuo. Salen diez
    //  folios correlativos y no falla nunca.
    //  Esto es importante decirlo en voz alta: el código de emitirIngenuo() NO
    //  tiene ningún error visible. Se puede revisar línea a línea y está bien.
    //  Qué se espera ver: 2026-0002 … 2026-0011, sin huecos ni repetidos.
    //  Para pensar: si esto funciona, ¿qué es lo que va a fallar en la demo 2?
    // =========================================================================
    public void deUnoEnUno() {
        seccion(1, "DE UNO EN UNO · secuencial");

        prepararElAnio();
        for (int i = 0; i < 10; i++) {
            emisor.emitirIngenuo(ANIO);
        }
        informe();
    }

    // =========================================================================
    //  2 · EL CRIMEN
    // -------------------------------------------------------------------------
    //  El mismo método, veinte veces A LA VEZ. Cada hilo abre su transacción, lee
    //  el último folio y suma uno; varios leen el mismo «último» antes de que
    //  ninguno haya guardado, y calculan el mismo número.
    //  Qué se espera ver: menos folios distintos que emisiones, con repetidos
    //  marcados. Y cuando exista la restricción del paso 5, rechazos de la base.
    //  Para pensar: ¿cuántas veces habría que probar esto a mano para verlo?
    // =========================================================================
    public void elCrimen() {
        seccion(2, "EL CRIMEN · " + EN_PARALELO + " emisiones a la vez, sin candado");

        prepararElAnio();
        enParalelo(i -> emisor.emitirIngenuo(ANIO));
        informe();
    }

    // =========================================================================
    //  3 · CON CANDADO
    // -------------------------------------------------------------------------
    //  Otra vez veinte a la vez, con el emisor que bloquea la fila de apertura
    //  antes de contar. Los hilos siguen saliendo todos a la vez; lo que cambia
    //  es que ahora hacen cola en la base.
    //  Qué se espera ver: veinte folios distintos, cero repetidos, cero rechazos.
    //  Para pensar: ¿esto es más lento? (Sí. ¿Comparado con repartir folios
    //  repetidos?)
    // =========================================================================
    public void conCandado() {
        seccion(3, "CON CANDADO · " + EN_PARALELO + " a la vez, con bloqueo pesimista");

        prepararElAnio();
        enParalelo(i -> emisor.emitirConCandado(ANIO));
        informe();
    }

    // -------------------------------------------------------------------------
    //  Lo de abajo es andamiaje: lanzar hilos y contar. No es materia del lab.
    // -------------------------------------------------------------------------

    /** Cuántas emisiones fueron rechazadas por la base en la última tanda. */
    private int rechazadas;

    // -------------------------------------------------------------------------
    //  Deja el año con un solo folio: el de apertura, número 1. Hace falta por
    //  dos motivos: que cada demo empiece desde el mismo punto, y que exista la
    //  fila que el candado del paso 4 bloquea.
    //
    //  OJO con lo que NO tiene: un @Transactional. Y no es un olvido — sería
    //  INÚTIL. Este método lo llaman las demos de esta misma clase, y una llamada
    //  entre métodos del mismo objeto no pasa por el proxy de Spring, así que la
    //  anotación no se aplicaría. La transacción la pone cada método del
    //  repositorio, que sí está detrás de su proxy.
    // -------------------------------------------------------------------------
    public void prepararElAnio() {
        folios.deleteByAnio(ANIO);
        folios.save(new Folio(ANIO, 1));
        System.out.println("  año " + ANIO + " reiniciado: solo el folio de apertura 2026-0001");
    }

    /** Lanza EN_PARALELO tareas de golpe y espera a que terminen todas. */
    private void enParalelo(IntFunction<Folio> emision) {
        rechazadas = 0;
        CountDownLatch salida = new CountDownLatch(1);
        List<Exception> fallos = new ArrayList<>();

        try (ExecutorService hilos = Executors.newFixedThreadPool(EN_PARALELO)) {
            for (int i = 0; i < EN_PARALELO; i++) {
                final int n = i;
                hilos.submit(() -> {
                    try {
                        salida.await();          // todos esperan aquí, y salen juntos
                        emision.apply(n);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } catch (Exception e) {
                        synchronized (fallos) {
                            fallos.add(e);
                        }
                    }
                });
            }
            salida.countDown();                  // ¡ya!
        }
        rechazadas = fallos.size();
    }

    /** Imprime el resultado de la tanda: cuántos salieron, cuántos distintos, cuáles repetidos. */
    private void informe() {
        List<Folio> emitidos = folios.findByAnioOrderByNumero(ANIO);
        List<Integer> numeros = emitidos.stream().map(Folio::getNumero).toList();
        long distintos = numeros.stream().distinct().count();

        Map<Integer, Long> vecesPorNumero = numeros.stream()
                .collect(Collectors.groupingBy(n -> n, Collectors.counting()));
        List<String> repetidos = vecesPorNumero.entrySet().stream()
                .filter(e -> e.getValue() > 1)
                .map(e -> ANIO + "-" + String.format("%04d", e.getKey()) + " (x" + e.getValue() + ")")
                .sorted()
                .toList();

        System.out.println("  folios en la tabla : " + emitidos.size());
        System.out.println("  números distintos  : " + distintos);
        System.out.println("  REPETIDOS          : " + (repetidos.isEmpty() ? "ninguno" : repetidos));
        System.out.println("  rechazados por la base : " + rechazadas);
        System.out.println("  emitidos: " + emitidos);
    }

    private void seccion(int numero, String titulo) {
        System.out.println();
        System.out.println("=== " + numero + " · " + titulo + " ===");
    }
}
