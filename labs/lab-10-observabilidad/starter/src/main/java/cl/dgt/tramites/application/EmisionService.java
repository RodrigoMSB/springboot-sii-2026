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
 * <p><strong>M&eacute;tricas de NEGOCIO (M12).</strong> Actuator ya publica solo el CPU, la
 * memoria, los hilos y la latencia HTTP. Nada de eso sabe cu&aacute;ntos folios emiti&oacute; la
 * DGT hoy. Un servidor al 3 % de carga puede llevar dos horas sin emitir uno solo porque un
 * validador rechaza todo: la m&aacute;quina, perfecta; el negocio, detenido. Ese es el hueco que
 * cierra el TODO_2.
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

    // El andamio está puesto: estos tres medidores son los que el TODO_2 tiene que registrar y
    // mover. Los tipos ya dicen casi todo — un Counter que solo sabe subir, y un Timer que
    // cronometra. Micrometer viaja dentro de spring-boot-starter-actuator; no hay nada que instalar.
    private final Counter emitidosNuevos;
    private final Counter emitidosReusados;
    private final Timer tardanzaDeEmision;

    public EmisionService(TramiteRepository tramites, FolioRepository folios,
                          ContadorFolioRepository contadores, MeterRegistry metricas) {
        this.tramites = tramites;
        this.folios = folios;
        this.contadores = contadores;

        // TODO_2 — Registra las métricas de negocio en el MeterRegistry.
        //
        //   · Dos contadores sobre el MISMO nombre (METRICA_FOLIOS), distinguidos por la etiqueta
        //     `resultado`: "nuevo" y "reusado". ¿Por qué separarlos? Porque un pico en el total no
        //     distingue "hoy se declaró mucho" (buena noticia) de "un cliente reintenta en bucle"
        //     (incidencia). RN-05 hace esa distinción real; la métrica debe conservarla.
        //   · Un Timer sobre METRICA_TARDANZA para el tiempo de la emisión completa.
        //
        // Y regístralos AQUÍ, en el constructor, no la primera vez que suben: una serie AUSENTE no
        // se distingue de "el scrape falló". Una serie registrada y en cero dice "vivo, sin
        // emitir", que es justo la alerta que Carolina necesita.
        //
        // Los tres `null` de abajo son el marcador de este TODO: {{TODO_2}}. No hay
        // UnsupportedOperationException porque esto es un CONSTRUCTOR — lanzar aquí no dejaría
        // arrancar la aplicación y todas las pruebas morirían por la misma causa, tapando
        // justamente la que este lab quiere que veas.
        //
        // Pista 2: Counter.builder(nombre).tag("clave", "valor").register(metricas) devuelve el
        //          contador ya registrado. Timer.builder(nombre).register(metricas), igual.
        this.emitidosNuevos = null;
        this.emitidosReusados = null;
        this.tardanzaDeEmision = null;
    }

    @Transactional
    public ResultadoEmision emitir(Long tramiteId) {
        Tramite tramite = tramites.findById(tramiteId)
                .orElseThrow(() -> new TramiteNoEncontradoException(tramiteId));

        // RN-05 · idempotencia: si el trámite ya tiene folio, se devuelve el mismo (200).
        // El UNIQUE (tramite_id) de la V1 es la red final si dos reintentos corren a la vez.
        //
        // TODO_2 (segunda mitad) — mueve las agujas:
        //   · envuelve TODA la operación en el Timer (lo que se mide es lo que el contribuyente
        //     espera, no un trozo interno);
        //   · incrementa `emitidosNuevos` cuando se emite uno nuevo y `emitidosReusados` cuando se
        //     devuelve el existente.
        //
        // Pista 2: tardanzaDeEmision.record(Supplier<T>) cronometra y devuelve el valor del
        //          bloque — te deja envolver el `return` entero sin reordenar nada.
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
