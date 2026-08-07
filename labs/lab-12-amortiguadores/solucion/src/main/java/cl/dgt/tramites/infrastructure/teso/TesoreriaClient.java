package cl.dgt.tramites.infrastructure.teso;

import cl.dgt.tramites.application.ConfirmacionPago;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

/**
 * El cliente de TESO, DECLARATIVO (HTTP Interface). No hay código imperativo: se declara la forma
 * de la llamada y Spring genera la implementación. Los timeouts viajan con el {@code RestClient}
 * que lo respalda (ver {@link TesoreriaConfig}). Migrar a esto es el TODO_3 — un refactor que no
 * cambia el comportamiento: la misma suite del enunciado sigue verde.
 */
@HttpExchange
public interface TesoreriaClient {

    @GetExchange("/pagos/{referencia}")
    ConfirmacionPago confirmar(@PathVariable String referencia);
}
