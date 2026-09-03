package cl.dgt.auditoria.controllers;

import cl.dgt.auditoria.entities.RegistroDeAuditoria;
import cl.dgt.auditoria.repositories.RegistroDeAuditoriaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/auditoria")
public class AuditoriaController {

    private static final Logger log = LoggerFactory.getLogger(AuditoriaController.class);

    // A PROPÓSITO. Registrar un evento no tarda un segundo y medio: se pone para que en
    // el paso 8 se VEA que el trámite ya respondió y auditoría todavía está trabajando.
    private static final long DEMORA_DELIBERADA_MS = 1500;

    private final RegistroDeAuditoriaRepository repositorio;

    public AuditoriaController(RegistroDeAuditoriaRepository repositorio) {
        this.repositorio = repositorio;
    }

    public record EventoDto(String evento, Long tramiteId, String rutContribuyente) {
    }

    public record RegistroDto(Long id, String evento, Long tramiteId, String rutContribuyente,
                              String traceId, Instant recibidoEn) {
    }

    @PostMapping("/eventos")
    public ResponseEntity<RegistroDto> recibir(@RequestBody EventoDto entrada) throws InterruptedException {
        log.info("[AUDITORIA] llega el evento {} del trámite {} — procesando...",
                entrada.evento(), entrada.tramiteId());
        Thread.sleep(DEMORA_DELIBERADA_MS);

        RegistroDeAuditoria registro = repositorio.save(new RegistroDeAuditoria(
                entrada.evento(), entrada.tramiteId(), entrada.rutContribuyente(), MDC.get("traceId")));

        log.info("[AUDITORIA] REGISTRADO id={} del trámite {}", registro.getId(), registro.getTramiteId());
        return ResponseEntity.status(HttpStatus.CREATED).body(aDto(registro));
    }

    @GetMapping("/eventos")
    public List<RegistroDto> listar() {
        return repositorio.findAll().stream().map(AuditoriaController::aDto).toList();
    }

    private static RegistroDto aDto(RegistroDeAuditoria r) {
        return new RegistroDto(r.getId(), r.getEvento(), r.getTramiteId(), r.getRutContribuyente(),
                r.getTraceId(), r.getRecibidoEn());
    }
}
