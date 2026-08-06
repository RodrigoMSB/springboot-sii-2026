package cl.dgt.tramites.application.evento;

import cl.dgt.tramites.application.NotificadorService;
import org.springframework.stereotype.Component;
import org.springframework.context.event.EventListener;

/**
 * Avisa al contribuyente cuando su folio queda emitido <strong>de verdad</strong>.
 *
 * <p><strong>{@code AFTER_COMMIT}, y esta es la lección del TODO_4.</strong> Un
 * {@code @EventListener} normal reacciona cuando el evento se publica — es decir, <em>dentro</em>
 * de la transacción, cuando el folio todavía no es un hecho. Si esa transacción revierte después
 * (falla una validación, se cae la base, salta un {@code CHECK} de la V3), el folio nunca existió…
 * y el contribuyente ya recibió el correo.
 *
 * <p>Un aviso de algo que no ocurrió es peor que no avisar. El que no recibe nada pregunta; el que
 * recibe un folio inexistente lo anota, lo declara, lo cita ante un fiscalizador y descubre el
 * problema meses después, cuando ya es de otro.
 *
 * <p>{@code AFTER_COMMIT} mueve el listener al otro lado de la frontera: se ejecuta solo si la
 * transacción llegó a puerto. Si revierte, este método <strong>no se llama nunca</strong>.
 *
 * <p><strong>Y ojo con lo que esto implica</strong>, porque es el precio: aquí ya <em>no hay
 * transacción</em>. La de antes se cerró. Si este listener escribiera en la base, necesitaría
 * abrir la suya ({@code REQUIRES_NEW}), y si fallara, lo ya confirmado no se desharía. Después del
 * commit, el mundo es de nuevo un lugar sin garantías — por eso lo que va aquí son efectos
 * externos (avisar, publicar, archivar), no correcciones al dato.
 */
@Component
public class EscuchaDeFolios {

    private final NotificadorService notificador;

    public EscuchaDeFolios(NotificadorService notificador) {
        this.notificador = notificador;
    }

    // TODO_4 — Este listener avisa DEMASIADO PRONTO.
    //
    //   @EventListener reacciona cuando el evento se publica, es decir DENTRO de la transacción,
    //   cuando el folio todavía no es un hecho. Si esa transacción revierte después —una validación,
    //   un CHECK de la base, una caída—, el folio nunca existió y el correo ya salió.
    //
    //   Avisar de algo que no ocurrió es peor que no avisar: el que no recibe nada pregunta; el que
    //   recibe un folio fantasma lo anota, lo declara y lo cita ante un fiscalizador.
    //
    //   Muévelo al otro lado de la frontera del commit.
    //
    // Pista 2: @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT). Y piensa qué
    //          pierdes al cruzar esa frontera: ahí ya no hay transacción abierta.
    @EventListener
    public void alEmitirseUnFolio(FolioEmitido evento) {
        // El notificador es @Async: esto vuelve enseguida y el aviso sale en un hilo virtual.
        // Dos desacoples encadenados, y cada uno resuelve algo distinto:
        //   · el EVENTO desacopla QUIÉN se entera  (EmisionService no conoce al notificador)
        //   · el @ASYNC desacopla CUÁNDO se hace   (nadie espera al servidor de correo)
        // Confundirlos lleva a creer que @Async solo ya bastaba. No: sin el evento, EmisionService
        // seguiría teniendo que saber que hay que notificar.
        notificador.notificar(evento.rut(),
                "Su folio N° " + evento.numero() + " fue emitido para el trámite " + evento.tramiteId());
    }
}
