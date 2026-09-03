package cl.dgt.consolidado.entities;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "tramite")
public class Tramite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String tipo;

    @Column(nullable = false)
    private String estado;

    @Column(nullable = false)
    private LocalDate fecha;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal monto;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contribuyente_id", nullable = false)
    private Contribuyente contribuyente;

    // La oficina que lo tramitó. La usa el ejemplo resuelto de `ejemplo/`.
    @Column(name = "oficina_codigo", nullable = false)
    private String oficinaCodigo;

    protected Tramite() {
    }

    /**
     * Constructor SÓLO para tests. La aplicación no lo usa: los trámites los crea Hibernate al
     * leer la tabla. Está aquí para que un test de servicio pueda armar un trámite sin levantar
     * la base ni recurrir a reflexión.
     */
    public Tramite(Long id, String tipo, String estado, java.time.LocalDate fecha,
                   java.math.BigDecimal monto, String oficinaCodigo) {
        this.id = id;
        this.tipo = tipo;
        this.estado = estado;
        this.fecha = fecha;
        this.monto = monto;
        this.oficinaCodigo = oficinaCodigo;
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

    public BigDecimal getMonto() {
        return monto;
    }

    public Contribuyente getContribuyente() {
        return contribuyente;
    }

    public String getOficinaCodigo() {
        return oficinaCodigo;
    }
}
