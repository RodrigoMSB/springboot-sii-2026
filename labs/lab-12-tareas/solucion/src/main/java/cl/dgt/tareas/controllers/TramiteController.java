package cl.dgt.tareas.controllers;

import cl.dgt.tareas.services.Instancia;
import cl.dgt.tareas.services.NotificadorService;
import cl.dgt.tareas.tareas.CierreNocturno;
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
    private final CierreNocturno cierre;
    private final Instancia instancia;

    public TramiteController(NotificadorService notificador, CierreNocturno cierre, Instancia instancia) {
        this.notificador = notificador;
        this.cierre = cierre;
        this.instancia = instancia;
    }

    /** Tres avisos, uno detrás de otro: el usuario espera los tres. */
    @PostMapping("/sincrono")
    public Map<String, Object> sincrono() {
        List.of("ana@sii.cl", "luis@sii.cl", "sofia@sii.cl").forEach(notificador::notificarSincrono);
        return Map.of("tramite", "creado", "modo", "SINCRONO");
    }

    /** Los mismos tres, pero el usuario no espera a ninguno. */
    @PostMapping("/asincrono")
    public Map<String, Object> asincrono() {
        List.of("ana@sii.cl", "luis@sii.cl", "sofia@sii.cl").forEach(notificador::notificarAsincrono);
        return Map.of("tramite", "creado", "modo", "ASINCRONO");
    }

    @GetMapping("/quien")
    public Map<String, Object> quien() {
        return Map.of(
                "instancia", instancia.nombre(),
                "vueltasDelCierre", cierre.vueltas(),
                "hiloQueAtiende", Thread.currentThread().toString(),
                "esVirtual", Thread.currentThread().isVirtual());
    }
}
