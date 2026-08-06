package cl.dgt.tramites.domain.entity;

import jakarta.persistence.*;

/**
 * El contador de folios. Tabla técnica (no de dominio): soporta RN-02 (folios secuenciales
 * sin saltos). Hay UNA sola fila, con id = 1.
 *
 * <p>¿Por qué no una SEQUENCE de PostgreSQL? Porque una sequence es NO transaccional: si la
 * transacción revierte, el número consumido no vuelve, y el libro de folios queda con un
 * hueco. Un hueco en un libro foliado no se borra: se explica, ante un fiscalizador.
 */
@Entity
@Table(name = "contador_folio")
public class ContadorFolio {

    @Id
    private Short id;

    @Column(name = "ultimo_numero", nullable = false)
    private long ultimoNumero;

    protected ContadorFolio() {
    }

    /** Toma el siguiente número y avanza el contador. */
    public long siguiente() {
        return ++ultimoNumero;
    }

    public Short getId() { return id; }
    public long getUltimoNumero() { return ultimoNumero; }
}
