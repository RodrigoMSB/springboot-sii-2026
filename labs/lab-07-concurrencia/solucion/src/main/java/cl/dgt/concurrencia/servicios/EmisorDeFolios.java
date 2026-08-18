package cl.dgt.concurrencia.servicios;

import cl.dgt.concurrencia.entities.Folio;
import cl.dgt.concurrencia.repositories.FolioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EmisorDeFolios {

    private final FolioRepository folios;

    public EmisorDeFolios(FolioRepository folios) {
        this.folios = folios;
    }

    // La transacción delimita el candado: fuera de ella no protege nada.
    @Transactional
    public Folio emitirIngenuo(int anio) {
        int ultimo = folios.maxNumeroDe(anio).orElse(0);
        return folios.save(new Folio(anio, ultimo + 1));
    }

    @Transactional
    public Folio emitirConCandado(int anio) {
        folios.bloquearLaApertura(anio)
                .orElseThrow(() -> new IllegalStateException(
                        "El año " + anio + " no tiene folio de apertura: no hay nada que bloquear."));

        int ultimo = folios.maxNumeroDe(anio).orElse(0);
        return folios.save(new Folio(anio, ultimo + 1));
    }
}
