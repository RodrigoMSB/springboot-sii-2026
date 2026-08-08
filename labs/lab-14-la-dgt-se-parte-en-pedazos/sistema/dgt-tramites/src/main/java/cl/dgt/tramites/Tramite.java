package cl.dgt.tramites;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * El trámite, en su versión reducida.
 *
 * <p>Fíjate en lo que <strong>no</strong> hay: no hay una relación
 * {@code @ManyToOne Contribuyente}. Solo un {@code rutContribuyente}, que es un
 * {@code String}. No se puede navegar, no se puede hacer un JOIN, no hay carga
 * perezosa que optimizar.
 *
 * <p>Todo el Lab 04 y el Lab 05 —el árbol de trámites, el N+1, los once
 * segundos— trataban sobre esa relación. Al partir el sistema, la relación
 * desapareció del modelo y reapareció como una llamada HTTP con circuit breaker.
 * No se resolvió el problema: se cambió por otro, más caro de operar y más
 * difícil de depurar, a cambio de poder desplegar las dos mitades por separado.
 *
 * <p>Decidir si ese cambio compensa es el contenido de la sección «Cuándo NO
 * usar microservicios» de la teoría.
 */
@Entity
@Table(name = "tramite")
public class Tramite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 40)
    private String tipo;

    @Column(name = "rut_contribuyente", nullable = false, length = 12)
    private String rutContribuyente;

    @Column(nullable = false, length = 20)
    private String estado;

    protected Tramite() {
        // JPA lo necesita. No lo uses tú.
    }

    public Long getId() {
        return id;
    }

    public String getTipo() {
        return tipo;
    }

    public String getRutContribuyente() {
        return rutContribuyente;
    }

    public String getEstado() {
        return estado;
    }
}
