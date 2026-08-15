package cl.dgt.tramites.domain.entity;

import java.time.LocalDateTime;

/**
 * Una nota interna de un funcionario sobre un contribuyente.
 *
 * <p><strong>{{TODO_1}} · ~15 min · Esta clase todavía no es una entidad.</strong> Hoy es una
 * clase Java normal: Hibernate no la conoce, y por eso nadie puede pedirle nada a la base a
 * través de ella.
 *
 * <p><em>Qué hacer.</em> Declarar que esta clase ES la tabla {@code observacion_interna}:
 * <ul>
 *   <li>{@code @Entity} sobre la clase, y {@code @Table(name = "observacion_interna")}.</li>
 *   <li>{@code @Id} sobre {@code id}, con
 *       {@code @GeneratedValue(strategy = GenerationType.IDENTITY)} — la columna es
 *       {@code BIGSERIAL}, la genera el motor.</li>
 *   <li>{@code @Column} donde el nombre Java no coincide con el de la columna:
 *       {@code creada_en}. Los que sí coinciden ({@code texto}, {@code autor}) igual conviene
 *       anotarlos para declarar {@code nullable} y {@code length}.</li>
 *   <li>{@code @ManyToOne} hacia {@code Contribuyente}, con {@code @JoinColumn(name =
 *       "contribuyente_id")}.</li>
 * </ul>
 *
 * <p><em>Pista que te va a ahorrar el Lab 04:</em> el {@code @ManyToOne} debe declarar
 * {@code fetch = FetchType.LAZY} de forma explícita. Su valor por defecto es EAGER, y eso
 * significa que pedir una observación arrastraría también al contribuyente, y con él sus
 * trámites. En el Lab 04 vas a medir cuánto cuesta eso.
 *
 * <p><em>El esquema está en</em> {@code src/main/resources/db/migration/V3__observacion_interna.sql}.
 * Mapear es hacer coincidir esta clase con esa tabla.
 *
 * <p><em>Lo verifica:</em> {@code E1_EntidadMapeadaIT}.
 *
 * <p>Sin Lombok (D-003). JPA exige un constructor sin argumentos; déjalo {@code protected}.
 */
public class ObservacionInterna {

    private Long id;

    private Contribuyente contribuyente;

    private String texto;

    private String autor;

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
