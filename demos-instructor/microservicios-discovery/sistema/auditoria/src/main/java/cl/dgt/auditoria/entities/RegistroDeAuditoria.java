package cl.dgt.auditoria.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.time.Instant;

@Entity
public class RegistroDeAuditoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String evento;

    @Column(name = "tramite_id", nullable = false)
    private Long tramiteId;

    @Column(name = "rut_contribuyente", nullable = false)
    private String rutContribuyente;

    @Column(name = "trace_id")
    private String traceId;

    @Column(name = "recibido_en", nullable = false)
    private Instant recibidoEn;

    protected RegistroDeAuditoria() {
    }

    public RegistroDeAuditoria(String evento, Long tramiteId, String rutContribuyente, String traceId) {
        this.evento = evento;
        this.tramiteId = tramiteId;
        this.rutContribuyente = rutContribuyente;
        this.traceId = traceId;
        // La hora en que auditoría lo registró, que NO es la hora en que el trámite se creó.
        // La distancia entre las dos es el paso 8.
        this.recibidoEn = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public String getEvento() {
        return evento;
    }

    public Long getTramiteId() {
        return tramiteId;
    }

    public String getRutContribuyente() {
        return rutContribuyente;
    }

    public String getTraceId() {
        return traceId;
    }

    public Instant getRecibidoEn() {
        return recibidoEn;
    }
}
