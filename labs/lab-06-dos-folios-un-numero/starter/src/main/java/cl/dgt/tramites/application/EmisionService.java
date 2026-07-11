package cl.dgt.tramites.application;

import cl.dgt.tramites.domain.entity.ContadorFolio;
import cl.dgt.tramites.domain.entity.Folio;
import cl.dgt.tramites.domain.entity.Tramite;
import cl.dgt.tramites.domain.exception.TramiteNoEncontradoException;
import cl.dgt.tramites.infrastructure.repository.ContadorFolioRepository;
import cl.dgt.tramites.infrastructure.repository.FolioRepository;
import cl.dgt.tramites.infrastructure.repository.TramiteRepository;
import org.springframework.stereotype.Service;

/**
 * Emite el folio de un trámite. Con un usuario a la vez, PERFECTA. La escena del crimen empieza
 * cuando dos aprietan "emitir" en el mismo milisegundo.
 *
 * <p>Aquí viven —o van a vivir— RN-01, RN-02 y RN-05. Hoy no. Léelo con cuidado: lo que falta es
 * el laboratorio de esta semana.
 */
@Service
public class EmisionService {

    private final TramiteRepository tramites;
    private final FolioRepository folios;
    private final ContadorFolioRepository contadores;

    public EmisionService(TramiteRepository tramites, FolioRepository folios,
                          ContadorFolioRepository contadores) {
        this.tramites = tramites;
        this.folios = folios;
        this.contadores = contadores;
    }

    // TODO_4 (la transacción): este método NO es @Transactional. Cada save commitea por su cuenta,
    //   así que si algo falla DESPUÉS de emitir, el folio ya quedó y el número ya se gastó: no hay
    //   rollback que lo devuelva. Además, sin transacción el bloqueo del TODO_1 no se sostiene: un
    //   candado se suelta al cerrar la transacción, y aquí no hay ninguna que lo sostenga.
    public ResultadoEmision emitir(Long tramiteId) {
        Tramite tramite = tramites.findById(tramiteId)
                .orElseThrow(() -> new TramiteNoEncontradoException(tramiteId));

        // TODO_2 (idempotencia, RN-05): falta la comprobación. Un reintento sobre el mismo trámite
        //   crea un SEGUNDO folio y revienta contra el UNIQUE (tramite_id). Debería devolver el que
        //   ya existe, con 200.

        // TODO_1 (el contador bloqueado, RN-01 + RN-02): se lee SIN bloqueo. Dos emisores leen el
        //   mismo número, los dos escriben el mismo folio, y la carrera deja duplicados o huecos.
        ContadorFolio contador = contadores.leerSinBloqueo().orElseThrow(
                () -> new IllegalStateException("El contador de folios no está inicializado (V1)."));
        long numero = contador.siguiente();
        contadores.save(contador);

        Folio folio = folios.save(new Folio(numero, tramite));
        return ResultadoEmision.nuevo(FolioDto.de(folio));
    }
}
