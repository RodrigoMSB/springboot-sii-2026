package cl.dgt.tareas.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class NotificadorService {

    private static final Logger log = LoggerFactory.getLogger(NotificadorService.class);

    public void notificarSincrono(String destinatario) {
        trabajar(destinatario, "SINCRONO");
    }

    // Paso 2 · el mismo trabajo, pero sin que el usuario espere.
    // escribe aquí

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
