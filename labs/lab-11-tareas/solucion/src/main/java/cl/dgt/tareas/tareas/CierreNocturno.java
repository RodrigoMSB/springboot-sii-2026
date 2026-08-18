package cl.dgt.tareas.tareas;

import cl.dgt.tareas.services.Instancia;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalTime;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class CierreNocturno {

    private static final Logger log = LoggerFactory.getLogger(CierreNocturno.class);

    private final Instancia instancia;
    private final AtomicInteger vueltas = new AtomicInteger();

    public CierreNocturno(Instancia instancia) {
        this.instancia = instancia;
    }

    // fixedDelay, no fixedRate: cuenta desde que TERMINA la anterior. Ver el paso 1.
    @Scheduled(fixedDelay = 5000, initialDelay = 3000)
    public void ejecutar() throws InterruptedException {
        int n = vueltas.incrementAndGet();
        log.info("[CIERRE] {} · vuelta {} · {} · hilo {}",
                instancia.nombre(), n, LocalTime.now().withNano(0), Thread.currentThread());
        Thread.sleep(1000);
    }

    public int vueltas() {
        return vueltas.get();
    }
}
