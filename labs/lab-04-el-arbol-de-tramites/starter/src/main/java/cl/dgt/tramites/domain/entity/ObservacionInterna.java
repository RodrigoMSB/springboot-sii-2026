package cl.dgt.tramites.domain.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Una nota interna de un funcionario sobre un contribuyente.
 *
 * <p>Esta clase ES la tabla {@code observacion_interna}. No hay código que abra una conexión,
 * ni que escriba un SELECT, ni que lea columna por columna: se declara la correspondencia y
 * Hibernate hace el resto.
 *
 * <p><strong>El {@code @ManyToOne} declara {@code LAZY} a propósito.</strong> Sin eso, pedir una
 * observación traería también al contribuyente, y con él sus trámites, y con ellos… El Lab 04
 * mide ese costo y AU-04 lo convierte en regla. Aquí se escribe bien desde el principio.
 *
 * <p>Sin Lombok (D-003). JPA exige el constructor sin argumentos; es {@code protected} para que
 * nadie lo use por accidente.
 */
@Entity
@Table(name = "observacion_interna")
public class ObservacionInterna {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "contribuyente_id", nullable = false)
    private Contribuyente contribuyente;

    @Column(nullable = false, length = 500)
    private String texto;

    @Column(nullable = false, length = 200)
    private String autor;

    @Column(name = "creada_en", nullable = false)
    private LocalDateTime creadaEn;

    protected ObservacionInterna() {
        // requerido por JPA
    }

    public ObservacionInterna(Contribuyente contribuyente, String texto, String autor) {
        this.contribuyente = contribuyente;
        this.texto = texto;
        this.autor = autor;
        this.creadaEn = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public Contribuyente getContribuyente() { return contribuyente; }
    public String getTexto() { return texto; }
    public String getAutor() { return autor; }
    public LocalDateTime getCreadaEn() { return creadaEn; }
}
