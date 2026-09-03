package cl.dgt.consolidado.entities;

import jakarta.persistence.*;

/**
 * La oficina que tramita expedientes. Existe para el ejemplo resuelto de {@code ejemplo/},
 * donde el encargo es el resumen de una oficina — la misma forma que tu consolidado.
 */
@Entity
@Table(name = "oficina")
public class Oficina {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String codigo;

    @Column(nullable = false)
    private String nombre;

    protected Oficina() {
    }

    public Oficina(String codigo, String nombre) {
        this.codigo = codigo;
        this.nombre = nombre;
    }

    public Long getId() {
        return id;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getNombre() {
        return nombre;
    }
}
