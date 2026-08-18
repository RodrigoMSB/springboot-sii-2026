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

@Component
public class DemosConcurrencia {

    private static final int ANIO = 2026;

    private static final int EN_PARALELO = 20;

    private final EmisorDeFolios emisor;
    private final FolioRepository folios;

    public DemosConcurrencia(EmisorDeFolios emisor, FolioRepository folios) {
        this.emisor = emisor;
        this.folios = folios;
    }

    public void deUnoEnUno() {
        seccion(1, "DE UNO EN UNO · secuencial");

        prepararElAnio();
        for (int i = 0; i < 10; i++) {
            emisor.emitirIngenuo(ANIO);
        }
        informe();
    }

    public void elCrimen() {
        seccion(2, "EL CRIMEN · " + EN_PARALELO + " emisiones a la vez, sin candado");

        prepararElAnio();
        enParalelo(i -> emisor.emitirIngenuo(ANIO));
        informe();
    }

    public void conCandado() {
        seccion(3, "CON CANDADO · " + EN_PARALELO + " a la vez, con bloqueo pesimista");

        prepararElAnio();
        enParalelo(i -> emisor.emitirConCandado(ANIO));
        informe();
    }

    private int rechazadas;

    public void prepararElAnio() {
        folios.deleteByAnio(ANIO);
        folios.save(new Folio(ANIO, 1));
        System.out.println("  año " + ANIO + " reiniciado: solo el folio de apertura 2026-0001");
    }

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
