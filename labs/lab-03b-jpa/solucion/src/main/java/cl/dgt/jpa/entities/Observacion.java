package cl.dgt.jpa.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDate;

// Una clase y una tabla son la misma cosa: esto es todo el mapeo.
@Entity
@Table(name = "observacion")
public class Observacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 500)
    private String texto;

    @Column(nullable = false, length = 100)
    private String autor;

    // LocalDate se corresponde con DATE: solo fecha, sin hora ni zona. Hibernate
    // sabe convertirlo sin que haya que decirle nada.
    @Column(nullable = false)
    private LocalDate fecha;

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

    public void setTexto(String texto) { this.texto = texto; }

    @Override
    public String toString() {
        return "Observacion{id=%d, texto='%s', autor='%s', fecha=%s}"
                .formatted(id, texto, autor, fecha);
    }
}
