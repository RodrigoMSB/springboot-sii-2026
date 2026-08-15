package cl.dgt.concurrencia.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Un folio emitido: un número correlativo dentro de un año. <strong>Viene dado.</strong>
 *
 * <p>La regla del negocio cabe en una línea: <em>dentro de un mismo año, no puede haber dos folios
 * con el mismo número</em>. Todo el laboratorio consiste en descubrir que esa línea es mucho más
 * difícil de cumplir de lo que parece.
 */
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

    /** JPA lo exige. */
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
