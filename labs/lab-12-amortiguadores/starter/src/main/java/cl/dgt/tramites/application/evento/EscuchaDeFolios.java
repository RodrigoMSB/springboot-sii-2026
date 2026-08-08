package cl.dgt.tramites.application.evento;

import cl.dgt.tramites.application.NotificadorService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Entrega el aviso a la cola cuando el folio queda emitido <strong>de verdad</strong>.
 *
 * <p><strong>{@code AFTER_COMMIT} sigue mandando, y ahora importa más que nunca.</strong> En el
 * Lab 11, avisar antes del commit mandaba un correo por un folio que podía no existir — malo, pero
 * al menos efímero. Ahora el aviso se deja en una cola <em>durable</em>: publicarlo antes del commit
 * dejaría un mensaje persistente, sobreviviente a reinicios, hablando de un folio que la base
 * revirtió. El error deja de evaporarse y pasa a quedar escrito en disco, esperando a que alguien
 * lo procese.
 *
 * <p>La regla se refuerza: <strong>lo que sale del sistema solo puede salir cuando el dato ya es
 * cierto.</strong>
 *
 * <p><strong>Lo único que cambió respecto del Lab 11</strong> es a quién se le entrega: antes al
 * notificador (una llamada en memoria, que exigía que el destinatario estuviera vivo en ese
 * instante), ahora al publicador (que solo exige que el broker exista). El listener no sabe si
 * alguien está escuchando al otro lado, y esa ignorancia es justamente lo que lo hace robusto.
 */
@Component
public class EscuchaDeFolios {

    private final NotificadorService notificador;

    public EscuchaDeFolios(NotificadorService notificador) {
        this.notificador = notificador;
    }

    // TODO_1 — Este aviso se manda AL AIRE.
    //
    //   Llama directo al notificador, que es lo que hacía el Lab 11. Con el servicio de avisos
    //   arriba, funciona. Con el servicio caído, el mensaje se evapora: la API responde 201, nadie
    //   ve un error, y —lo peor— no queda forma de saber CUÁLES avisos se perdieron.
    //
    //   Una llamada directa exige que el otro esté vivo EN EL MISMO INSTANTE. Una cola solo exige
    //   que exista. Entrega el aviso a la cola y desentiéndete de quién lo consume.
    //
    //   Vívelo antes de arreglarlo:   ./bin/start-lab.sh --avisos-caidos
    //
    //   Y fíjate en que el AFTER_COMMIT de abajo no se toca: sigue mandando, y ahora importa MÁS.
    //   Publicar antes del commit dejaría un mensaje DURABLE, sobreviviente a reinicios, hablando
    //   de un folio que la base revirtió. El error dejaría de evaporarse y pasaría a quedar escrito
    //   en disco esperando a que alguien lo procese.
    //
    // Pista 2: AvisoPublicador ya está escrito y es un bean. AvisoDeFolio.de(evento) construye el
    //          mensaje con su clave de idempotencia. {{TODO_1}}
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void alEmitirseUnFolio(FolioEmitido evento) {
        notificador.notificar(evento.rut(),
                "Su folio N° " + evento.numero() + " fue emitido para el trámite " + evento.tramiteId());
    }
}
