package cl.dgt.relaciones.entities;

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
 * Un trámite, que siempre pertenece a un contribuyente.
 *
 * <p>Muchos trámites pueden apuntar al mismo contribuyente: de ahí el nombre de la anotación.
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

    // =========================================================================
    //  EL LADO QUE MANDA — llegó en el paso 1
    // -------------------------------------------------------------------------
    //  Quien tiene la columna en la base es quien manda en la relación, y aquí
    //  se dice con @JoinColumn: la columna `contribuyente_id` de la tabla
    //  `tramite`. Guardar el trámite guarda la relación; no hay que tocar nada
    //  del otro lado.
    //  FetchType.LAZY: el contribuyente NO viaja con el trámite. Se va a buscar
    //  cuando alguien lo toque, y eso son los pasos 2, 4 y 5.
    //  Para pensar: ¿por qué la anotación se llama ManyToOne y no OneToMany?
    // =========================================================================
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
