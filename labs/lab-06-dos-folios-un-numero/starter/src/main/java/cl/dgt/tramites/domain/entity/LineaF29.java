package cl.dgt.tramites.domain.entity;

import jakarta.persistence.*;

/**
 * Una línea del F29: un código tributario y su monto.
 *
 * <p>El monto puede ser negativo: los créditos lo son (lo prueba {@code Formulario29TotalTest}).
 * Por eso el contrato que llega en el Módulo 8 no es {@code monto >= 0} —eso rompería los
 * créditos— sino {@code CHECK (monto <> 0)}: ninguna línea vale cero. Su ausencia hasta la V3 es
 * deliberada: la lección es que una restricción es un contrato que vive en la base, no un
 * comentario que vive en la esperanza.
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
