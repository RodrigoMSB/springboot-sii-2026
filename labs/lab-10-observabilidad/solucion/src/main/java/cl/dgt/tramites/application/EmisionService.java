package cl.dgt.tramites.application;

import cl.dgt.tramites.domain.entity.ContadorFolio;
import cl.dgt.tramites.domain.entity.Folio;
import cl.dgt.tramites.domain.entity.Tramite;
import cl.dgt.tramites.domain.exception.TramiteNoEncontradoException;
import cl.dgt.tramites.infrastructure.repository.ContadorFolioRepository;
import cl.dgt.tramites.infrastructure.repository.FolioRepository;
import cl.dgt.tramites.infrastructure.repository.TramiteRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
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
 *
 * <p><strong>M&eacute;tricas de NEGOCIO (M12).</strong> Este servicio se mide a s&iacute; mismo, y
 * mide lo que a Carolina le importa: cu&aacute;ntos folios se emiten y cu&aacute;nto tardan. Esas dos
 * cifras no se deducen del CPU ni de la memoria. Un servidor al 3 % de carga puede llevar dos horas
 * sin emitir un solo folio porque el validador de RUT rechaza todo: la m&aacute;quina est&aacute;
 * perfecta y el negocio est&aacute; detenido. El contador en cero lo grita; el CPU, no.
 *
 * <p>Se instrumenta con {@link MeterRegistry} —no con {@code @Timed}— a prop&oacute;sito: la
 * anotaci&oacute;n depende del proxy (misma familia que {@code @Transactional}, misma trampa que el
 * aspecto del Lab 09) y aqu&iacute; queremos separar dos resultados de NEGOCIO distintos, «nuevo» y
 * «reusado», que una anotaci&oacute;n no distingue.
 */
@Service
public class EmisionService {

    /** Nombre de la métrica. Constante y no literal suelto: los tests la nombran, y los tableros también. */
    public static final String METRICA_FOLIOS = "dgt.folios.emitidos";
    /** El tiempo de la emisión completa, incluido el bloqueo del contador. */
    public static final String METRICA_TARDANZA = "dgt.folios.emision";

    private final TramiteRepository tramites;
    private final FolioRepository folios;
    private final ContadorFolioRepository contadores;

    private final Counter emitidosNuevos;
    private final Counter emitidosReusados;
    private final Timer tardanzaDeEmision;

    public EmisionService(TramiteRepository tramites, FolioRepository folios,
                          ContadorFolioRepository contadores, MeterRegistry metricas) {
        this.tramites = tramites;
        this.folios = folios;
        this.contadores = contadores;

        // Los contadores se REGISTRAN en el constructor, no la primera vez que suben. Si se crearan
        // al vuelo, un folio jamás emitido sería una métrica AUSENTE, y una serie ausente no se
        // distingue de «el scrape falló». Registrada y en cero, la serie dice «vivo, sin emitir»:
        // eso es una alerta accionable. La ausencia es solo una duda.
        this.emitidosNuevos = Counter.builder(METRICA_FOLIOS)
                .description("Folios emitidos por la DGT")
                .tag("resultado", "nuevo")
                .register(metricas);
        this.emitidosReusados = Counter.builder(METRICA_FOLIOS)
                .description("Folios emitidos por la DGT")
                .tag("resultado", "reusado")   // RN-05: el reintento idempotente devuelve el mismo folio
                .register(metricas);
        this.tardanzaDeEmision = Timer.builder(METRICA_TARDANZA)
                .description("Tiempo de emisión de un folio, incluido el bloqueo del contador")
                .register(metricas);
    }

    @Transactional
    public ResultadoEmision emitir(Long tramiteId) {
        // El Timer envuelve la operación completa: lo que se mide es lo que el contribuyente espera.
        return tardanzaDeEmision.record(() -> {
            Tramite tramite = tramites.findById(tramiteId)
                    .orElseThrow(() -> new TramiteNoEncontradoException(tramiteId));

            // RN-05 · idempotencia: si el trámite ya tiene folio, se devuelve el mismo (200).
            // El UNIQUE (tramite_id) de la V1 es la red final si dos reintentos corren a la vez.
            return folios.findByTramiteId(tramiteId)
                    .map(f -> {
                        emitidosReusados.increment();
                        return ResultadoEmision.reusado(FolioDto.de(f));
                    })
                    .orElseGet(() -> {
                        ResultadoEmision resultado = ResultadoEmision.nuevo(FolioDto.de(emitirNuevo(tramite)));
                        emitidosNuevos.increment();
                        return resultado;
                    });
        });
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
