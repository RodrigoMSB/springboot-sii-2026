package cl.dgt.tramites.domain.entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * La declaración mensual de IVA.
 *
 * <p><strong>RN-06:</strong> el total es la suma de las líneas. Fíjate en lo que
 * <em>no</em> hay: no existe una columna {@code total} en la tabla, ni un campo aquí.
 * {@link #total()} lo deriva cada vez.
 *
 * <p>Un invariante que necesita un test para sostenerse ya perdió: alguien, algún día,
 * escribirá en esa columna un número que no cuadra. Este invariante no puede violarse
 * porque no hay dónde escribir la mentira.
 */
@Entity
@Table(name = "formulario29")
public class Formulario29 {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tramite_id", nullable = false, unique = true)
    private Tramite tramite;

    @Column(nullable = false, length = 7)
    private String periodo;

    @OneToMany(mappedBy = "formulario29", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<LineaF29> lineas = new ArrayList<>();

    protected Formulario29() {
    }

    public Formulario29(Tramite tramite, String periodo) {
        this.tramite = tramite;
        this.periodo = periodo;
    }

    /** RN-06: el total es derivado, nunca persistido. */
    public long total() {
        return lineas.stream().mapToLong(LineaF29::getMonto).sum();
    }

    public void agregarLinea(LineaF29 linea) {
        lineas.add(linea);
    }

    public Long getId() { return id; }
    public Tramite getTramite() { return tramite; }
    public String getPeriodo() { return periodo; }
    public List<LineaF29> getLineas() { return lineas; }
}
