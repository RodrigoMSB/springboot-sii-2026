package cl.dgt.tareas.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class NotificadorService {

    private static final Logger log = LoggerFactory.getLogger(NotificadorService.class);

    /** Lento a propósito: un segundo, como un correo real. */
    public void notificarSincrono(String destinatario) {
        trabajar(destinatario, "SINCRONO");
    }

    // El método vuelve al instante; el trabajo sigue en otro hilo.
    @Async
    public void notificarAsincrono(String destinatario) {
        trabajar(destinatario, "ASINCRONO");
    }

    private void trabajar(String destinatario, String modo) {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return;
        }
        log.info("[{}] aviso enviado a {} · hilo {}", modo, destinatario, Thread.currentThread());
    }
}
