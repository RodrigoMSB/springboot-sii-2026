package cl.dgt.rendimiento.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDate;

/**
 * Un trámite, tal como quedó al terminar el Lab 04. <strong>Viene dado y no se toca.</strong>
 */
@Entity
@Table(name = "tramite")
public class Tramite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 40)
    private String tipo;

    @Column(nullable = false, length = 20)
    private String estado;

    @Column(nullable = false)
    private LocalDate fecha;

    // El lado que manda: la columna `contribuyente_id` vive en esta tabla. LAZY,
    // declarado, como dejó dicho el Lab 04.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contribuyente_id", nullable = false)
    private Contribuyente contribuyente;

    /** JPA lo exige. */
    protected Tramite() {
    }

    public Tramite(String tipo, String estado, LocalDate fecha, Contribuyente contribuyente) {
        this.tipo = tipo;
        this.estado = estado;
        this.fecha = fecha;
        this.contribuyente = contribuyente;
    }

    public Long getId() {
        return id;
    }

    public String getTipo() {
        return tipo;
    }

    public String getEstado() {
        return estado;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public Contribuyente getContribuyente() {
        return contribuyente;
    }

    @Override
    public String toString() {
        return "Tramite{id=" + id + ", tipo='" + tipo + "', estado='" + estado + "', fecha=" + fecha + "}";
    }
}
