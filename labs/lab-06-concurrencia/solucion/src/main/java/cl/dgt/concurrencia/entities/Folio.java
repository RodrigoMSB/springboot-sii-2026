package cl.dgt.concurrencia.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "folio")
public class Folio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private int anio;

    @Column(nullable = false)
    private int numero;

    protected Folio() {
    }

    public Folio(int anio, int numero) {
        this.anio = anio;
        this.numero = numero;
    }

    public Long getId() {
        return id;
    }

    public int getAnio() {
        return anio;
    }

    public int getNumero() {
        return numero;
    }

    @Override
    public String toString() {
        return anio + "-" + String.format("%04d", numero);
    }
}
