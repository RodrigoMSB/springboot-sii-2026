package cl.dgt.relaciones.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDate;

/**
 * Un trámite. Tal como llega, no sabe de quién es: le falta la relación, y eso es el paso 1.
 *
 * <p>Fíjate en que la tabla ya tiene la columna —mira
 * {@code db/migration/V1__contribuyente_y_tramite.sql}—. La base está preparada; la clase todavía
 * no. Por eso el proyecto arranca igual: Hibernate comprueba que lo que la clase declara exista en
 * la tabla, y no al revés.
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
    //  AQUÍ VA LA RELACIÓN — paso 1
    // -------------------------------------------------------------------------
    //  Un campo `Contribuyente contribuyente` con dos anotaciones: @ManyToOne
    //  (muchos trámites apuntan a un contribuyente) y @JoinColumn, que nombra la
    //  columna donde vive la relación en la tabla `tramite`.
    //  Qué se espera ver: el INSERT del trámite pasa a incluir contribuyente_id.
    //  Para pensar: ¿por qué la columna está en `tramite` y no en `contribuyente`?
    // =========================================================================
    // escribe aquí

    /** JPA lo exige. */
    protected Tramite() {
    }

    // OJO: en el paso 1, este constructor gana un cuarto parámetro —el
    // contribuyente— y la línea que lo asigna.
    public Tramite(String tipo, String estado, LocalDate fecha) {
        this.tipo = tipo;
        this.estado = estado;
        this.fecha = fecha;
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

    @Override
    public String toString() {
        return "Tramite{id=" + id + ", tipo='" + tipo + "', estado='" + estado + "', fecha=" + fecha + "}";
    }
}
