package cl.dgt.muchosamuchos.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDate;

@Entity
@Table(name = "tramite")
public class Tramite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 40)
    private String tipo;

    @Column(nullable = false, length = 12)
    private String rut;

    @Column(nullable = false)
    private LocalDate fecha;

    // Paso 1 · declara la relación con Documento y la tabla intermedia que la guarda.
    // escribe aquí

    protected Tramite() {
    }

    public Tramite(String tipo, String rut, LocalDate fecha) {
        this.tipo = tipo;
        this.rut = rut;
        this.fecha = fecha;
    }

    public Long getId() {
        return id;
    }

    public String getTipo() {
        return tipo;
    }

    public String getRut() {
        return rut;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    // Paso 1 · el getter de la colección, y los dos métodos que la tocan.
    // escribe aquí

    @Override
    public String toString() {
        return "Tramite{id=" + id + ", tipo='" + tipo + "', rut='" + rut + "', fecha=" + fecha + "}";
    }
}
