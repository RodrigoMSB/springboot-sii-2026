package cl.dgt.jpa.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDate;

/**
 * Esta clase ES la tabla {@code observacion}.
 *
 * <p>Las anotaciones son el mapa: qué clase corresponde a qué tabla, qué propiedad a qué
 * columna, y cuál es la clave. Con eso, Hibernate escribe el SQL de todas las operaciones.
 */
@Entity
@Table(name = "observacion")
public class Observacion {

    /** La clave primaria. {@code IDENTITY} = la genera la base (la columna es BIGSERIAL). */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 500)
    private String texto;

    @Column(nullable = false, length = 100)
    private String autor;

    @Column(nullable = false)
    private LocalDate fecha;

    /** JPA exige un constructor sin argumentos. Es {@code protected} para no usarlo por error. */
    protected Observacion() {
    }

    public Observacion(String texto, String autor, LocalDate fecha) {
        this.texto = texto;
        this.autor = autor;
        this.fecha = fecha;
    }

    public Long getId() { return id; }
    public String getTexto() { return texto; }
    public String getAutor() { return autor; }
    public LocalDate getFecha() { return fecha; }

    /** Necesario para el paso 6: cambiar el texto de una observación ya guardada. */
    public void setTexto(String texto) { this.texto = texto; }

    @Override
    public String toString() {
        return "Observacion{id=%d, texto='%s', autor='%s', fecha=%s}"
                .formatted(id, texto, autor, fecha);
    }
}
