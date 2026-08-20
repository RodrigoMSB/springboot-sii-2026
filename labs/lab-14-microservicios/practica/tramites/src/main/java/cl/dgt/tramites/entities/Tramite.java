package cl.dgt.tramites.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.time.Instant;

@Entity
public class Tramite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "rut_contribuyente", nullable = false)
    private String rutContribuyente;

    @Column(nullable = false)
    private String tipo;

    @Column(nullable = false)
    private String estado;

    @Column(name = "creado_en", nullable = false)
    private Instant creadoEn;

    protected Tramite() {
    }

    public Tramite(String rutContribuyente, String tipo) {
        this.rutContribuyente = rutContribuyente;
        this.tipo = tipo;
        this.estado = "EN_PROCESO";
        this.creadoEn = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public String getRutContribuyente() {
        return rutContribuyente;
    }

    public String getTipo() {
        return tipo;
    }

    public String getEstado() {
        return estado;
    }

    public Instant getCreadoEn() {
        return creadoEn;
    }
}
