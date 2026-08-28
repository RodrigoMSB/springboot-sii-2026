package cl.dgt.examen.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "oficina")
public class Oficina {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 10)
    private String codigo;

    @Column(nullable = false, length = 80)
    private String nombre;

    @Column(nullable = false, length = 60)
    private String comuna;

    // =========================================================================
    //  HUECO 01 · La relacion con Solicitud, vista desde la oficina
    // -------------------------------------------------------------------------
    //  Una oficina tiene muchas solicitudes. Aqui falta esa coleccion, y falta
    //  la forma de leerla desde fuera.
    //
    //  La tabla `solicitud` ya trae la columna `oficina_id`, y `Solicitud` ya
    //  declara su lado. Lo que falta es el otro lado.
    //
    //  ESTA LISTO CUANDO · pasa el test H-01
    // =========================================================================

    protected Oficina() {
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

    public String getComuna() {
        return comuna;
    }

}
