package cl.dgt.concurrencia.servicios;

import cl.dgt.concurrencia.entities.Folio;
import cl.dgt.concurrencia.repositories.FolioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Quien emite los folios. Dos versiones del mismo método, y esa es toda la diferencia entre un
 * sistema correcto y uno que reparte folios repetidos.
 *
 * <p>Los dos son {@code @Transactional}: cada llamada abre su transacción, hace lo suyo, y confirma
 * al salir. Eso es lo que quiere decir «todo o nada».
 */
@Service
public class EmisorDeFolios {

    private final FolioRepository folios;

    public EmisorDeFolios(FolioRepository folios) {
        this.folios = folios;
    }

    // =========================================================================
    //  LA VERSIÓN INGENUA — paso 1 y paso 2
    // -------------------------------------------------------------------------
    //  Leer el último, sumar uno, guardar. Es exactamente lo que haría cualquiera
    //  y funciona perfecto mientras haya un solo hilo.
    //  El agujero está entre la primera línea y la segunda: si otro hilo lee el
    //  mismo «último» antes de que este guarde, los dos calculan el mismo número.
    //  Qué se espera ver: bien con 10 seguidas, mal con 20 a la vez.
    //  Para pensar: ¿cuánto dura esa rendija? (Microsegundos. Sobran.)
    // =========================================================================
    @Transactional
    public Folio emitirIngenuo(int anio) {
        int ultimo = folios.maxNumeroDe(anio).orElse(0);
        return folios.save(new Folio(anio, ultimo + 1));
    }

    // =========================================================================
    //  LA VERSIÓN CON CANDADO — paso 4
    // -------------------------------------------------------------------------
    //  El mismo método de arriba con UNA línea antes: pedir la fila de apertura
    //  del año con el método @Lock que agregaste al repositorio.
    //  El orden importa: primero el candado, DESPUÉS leer el máximo. Al revés no
    //  serviría de nada, porque se leería antes de tener el turno.
    //  Qué se espera ver: cero repetidos con 20 hilos a la vez.
    //  Para pensar: ¿qué pasa con el que espera si el primero tarda un minuto?
    // =========================================================================
    // escribe aquí — paso 4
}
