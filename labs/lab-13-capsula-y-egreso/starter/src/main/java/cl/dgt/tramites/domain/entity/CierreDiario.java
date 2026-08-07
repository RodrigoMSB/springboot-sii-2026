package cl.dgt.tramites.domain.entity;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * El resumen del día: cuántos trámites hubo y cuánto se declaró. Lo produce el cierre nocturno.
 *
 * <p><strong>Fíjate en lo que esta clase NO impide.</strong> Nada aquí —ni en la tabla— evita que
 * existan dos filas del mismo día. Es deliberado: si lo impidiéramos, la segunda instancia moriría
 * con una violación de clave y el alumno vería un error de base de datos en vez de ver el crimen.
 * La doble ejecución tiene que quedar <em>a la vista</em>, en dos filas con la misma fecha y dos
 * instancias distintas.
 *
 * <p>Y la lección de fondo: una constraint aquí protegería la TABLA, no el TRABAJO. Para cuando
 * el {@code INSERT} choca, las notificaciones ya salieron dos veces. Una restricción no es un
 * candado.
 */
@Entity
@Table(name = "cierre_diario")
public class CierreDiario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate fecha;

    @Column(nullable = false)
    private int tramites;

    @Column(name = "total_declarado", nullable = false)
    private long totalDeclarado;

    @Column(name = "ejecutado_en", nullable = false)
    private LocalDateTime ejecutadoEn;

    /** Quién lo ejecutó. Con dos instancias, es la prueba del delito. */
    @Column(nullable = false, length = 80)
    private String instancia;

    protected CierreDiario() {
    }

    public CierreDiario(LocalDate fecha, int tramites, long totalDeclarado,
                        LocalDateTime ejecutadoEn, String instancia) {
        this.fecha = fecha;
        this.tramites = tramites;
        this.totalDeclarado = totalDeclarado;
        this.ejecutadoEn = ejecutadoEn;
        this.instancia = instancia;
    }

    public Long getId() { return id; }
    public LocalDate getFecha() { return fecha; }
    public int getTramites() { return tramites; }
    public long getTotalDeclarado() { return totalDeclarado; }
    public LocalDateTime getEjecutadoEn() { return ejecutadoEn; }
    public String getInstancia() { return instancia; }
}
