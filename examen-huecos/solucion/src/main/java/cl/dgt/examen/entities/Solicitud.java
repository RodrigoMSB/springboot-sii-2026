package cl.dgt.examen.entities;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "solicitud")
public class Solicitud {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 40)
    private String tipo;

    @Column(nullable = false, length = 20)
    private String estado;

    @Column(nullable = false)
    private LocalDate fecha;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal monto;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "oficina_id", nullable = false)
    private Oficina oficina;

    protected Solicitud() {
    }

    public Solicitud(String tipo, String estado, LocalDate fecha, BigDecimal monto, Oficina oficina) {
        this.tipo = tipo;
        this.estado = estado;
        this.fecha = fecha;
        this.monto = monto;
        this.oficina = oficina;
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

    public Oficina getOficina() {
        return oficina;
    }

    /** Único setter: cambiar de estado es la operación que el dominio permite. */
    public void cambiarEstadoA(String nuevoEstado) {
        this.estado = nuevoEstado;
    }
}
