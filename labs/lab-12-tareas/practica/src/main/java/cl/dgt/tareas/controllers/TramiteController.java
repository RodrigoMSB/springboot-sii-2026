package cl.dgt.tareas.controllers;

import cl.dgt.tareas.services.NotificadorService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/tramites")
public class TramiteController {

    private final NotificadorService notificador;

    public TramiteController(NotificadorService notificador) {
        this.notificador = notificador;
    }

    @PostMapping("/sincrono")
    public Map<String, Object> sincrono() {
        List.of("ana@sii.cl", "luis@sii.cl", "sofia@sii.cl").forEach(notificador::notificarSincrono);
        return Map.of("tramite", "creado", "modo", "SINCRONO");
    }

    // Paso 2 · el mismo endpoint, llamando a la versión asíncrona.
    // escribe aquí

    @GetMapping("/quien")
    public Map<String, Object> quien() {
        return Map.of(
                "hiloQueAtiende", Thread.currentThread().toString(),
                "esVirtual", Thread.currentThread().isVirtual());
    }
}
