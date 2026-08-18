package cl.dgt.tareas.tareas;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalTime;

@Component
public class Recordatorio {

    private static final Logger log = LoggerFactory.getLogger(Recordatorio.class);

    // Cada 10 segundos, en los segundos 0,10,20,30,40,50. La zona va explícita: sin ella se usa
    // la del servidor, que en producción suele ser UTC.
    @Scheduled(cron = "*/10 * * * * *", zone = "America/Santiago")
    public void avisar() {
        log.info("[CRON] recordatorio · {}", LocalTime.now().withNano(0));
    }
}
