package cl.dgt.tramites.domain.entity;

import jakarta.persistence.*;

/**
 * Una línea del F29: un código tributario y su monto.
 *
 * <p>El monto puede ser negativo (los créditos lo son). La restricción
 * {@code CHECK (monto >= 0)} <em>no</em> existe todavía, y su ausencia es deliberada:
 * llega en el lab del Módulo 8, cuando se enseña que una restricción es un contrato que
 * vive en la base y no un comentario que vive en la esperanza.
 */
@Entity
@Table(name = "linea_f29")
public class LineaF29 {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "formulario29_id", nullable = false)
    private Formulario29 formulario29;

    @Column(nullable = false, length = 10)
    private String codigo;

    @Column(nullable = false)
    private long monto;

    protected LineaF29() {
    }

    public LineaF29(Formulario29 formulario29, String codigo, long monto) {
        this.formulario29 = formulario29;
        this.codigo = codigo;
        this.monto = monto;
    }

    public Long getId() { return id; }
    public Formulario29 getFormulario29() { return formulario29; }
    public String getCodigo() { return codigo; }
    public long getMonto() { return monto; }
}
