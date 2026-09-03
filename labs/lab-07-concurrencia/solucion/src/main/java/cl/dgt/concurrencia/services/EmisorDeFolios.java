package cl.dgt.concurrencia.services;

import cl.dgt.concurrencia.entities.Folio;
import cl.dgt.concurrencia.repositories.FolioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class EmisorDeFolios {

    private static final Logger log = LoggerFactory.getLogger(EmisorDeFolios.class);

    // Una sola línea por proceso: con veinte hilos, veinte líneas iguales no enseñan nada.
    private final AtomicBoolean turnoAnunciado = new AtomicBoolean();

    private final FolioRepository folios;

    public EmisorDeFolios(FolioRepository folios) {
        this.folios = folios;
    }

    // La transacción delimita el turno: fuera de ella no protege nada.
    @Transactional
    public Folio emitirIngenuo(int anio) {
        int ultimo = folios.maxNumeroDe(anio).orElse(0);
        return folios.save(new Folio(anio, ultimo + 1));
    }

    @Transactional
    public Folio emitirConTurno(int anio) {
        folios.tomarElTurnoDelAnio(anio);

        if (turnoAnunciado.compareAndSet(false, true)) {
            log.info("[TURNO] pg_advisory_xact_lock({}) · el turno vive en la base, no en Java", anio);
        }

        int ultimo = folios.maxNumeroDe(anio).orElse(0);
        return folios.save(new Folio(anio, ultimo + 1));
    }
}
