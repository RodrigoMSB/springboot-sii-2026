package cl.dgt.concurrencia.services;

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

    @Transactional
    public Folio emitirIngenuo(int anio) {
        int ultimo = folios.maxNumeroDe(anio).orElse(0);
        return folios.save(new Folio(anio, ultimo + 1));
    }

    // Paso 4 · emite pidiendo primero el turno del año.
    // escribe aquí
}
