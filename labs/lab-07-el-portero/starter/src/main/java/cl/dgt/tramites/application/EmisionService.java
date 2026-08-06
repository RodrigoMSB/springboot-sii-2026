package cl.dgt.tramites.application;

import cl.dgt.tramites.domain.entity.ContadorFolio;
import cl.dgt.tramites.domain.entity.Folio;
import cl.dgt.tramites.domain.entity.Tramite;
import cl.dgt.tramites.domain.exception.TramiteNoEncontradoException;
import cl.dgt.tramites.infrastructure.repository.ContadorFolioRepository;
import cl.dgt.tramites.infrastructure.repository.FolioRepository;
import cl.dgt.tramites.infrastructure.repository.TramiteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Emite el folio de un tr&aacute;mite. Aqu&iacute; viven RN-01, RN-02 y RN-05 — las tres
 * reglas que se anotaron desde la SPEC-005 y que hoy, por fin, tienen suelo.
 *
 * <p><strong>Todo ocurre en UNA transacci&oacute;n</strong> (el proxy de {@code @Transactional}).
 * El n&uacute;mero se toma del contador CON BLOQUEO PESIMISTA y el folio se persiste en la misma
 * transacci&oacute;n: el candado vive en el DATO, no en el c&oacute;digo. Por eso funciona aunque
 * corran dos instancias de la app (a diferencia de {@code synchronized}) y por eso, si algo falla
 * despu&eacute;s, el rollback devuelve el n&uacute;mero (a diferencia de {@code REQUIRES_NEW}).
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

    @Transactional
    public ResultadoEmision emitir(Long tramiteId) {
        Tramite tramite = tramites.findById(tramiteId)
                .orElseThrow(() -> new TramiteNoEncontradoException(tramiteId));

        // RN-05 · idempotencia: si el trámite ya tiene folio, se devuelve el mismo (200).
        // El UNIQUE (tramite_id) de la V1 es la red final si dos reintentos corren a la vez.
        return folios.findByTramiteId(tramiteId)
                .map(f -> ResultadoEmision.reusado(FolioDto.de(f)))
                .orElseGet(() -> ResultadoEmision.nuevo(FolioDto.de(emitirNuevo(tramite))));
    }

    private Folio emitirNuevo(Tramite tramite) {
        // RN-01 + RN-02 · el número lo da el contador BLOQUEADO (SELECT ... FOR UPDATE), en
        // ESTA transacción. Los emisores concurrentes se serializan sobre la fila del contador:
        // ninguno lee un número que otro ya tomó, así que salen únicos y sin saltos.
        ContadorFolio contador = contadores.tomarConBloqueo().orElseThrow(
                () -> new IllegalStateException("El contador de folios no está inicializado (V1)."));
        long numero = contador.siguiente();
        return folios.save(new Folio(numero, tramite));
    }
}
