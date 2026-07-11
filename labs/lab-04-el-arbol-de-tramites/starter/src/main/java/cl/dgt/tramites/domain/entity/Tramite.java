package cl.dgt.tramites.domain.entity;

import cl.dgt.tramites.domain.exception.TransicionIlegalException;
import cl.dgt.tramites.domain.tipo.EstadoTramite;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Una gestión de un contribuyente ante la DGT.
 *
 * <p>Todos los {@code @ManyToOne} y {@code @OneToOne} declaran {@code fetch = LAZY}
 * <em>explícitamente</em>. El default de JPA para {@code @ManyToOne} es EAGER, y ese
 * default es el que hace que el listado del Lab 05 tarde once segundos. AU-04 lo vigila:
 * escribirlo a mano no es redundancia, es una declaración de intención.
 */
@Entity
@Table(name = "tramite")
public class Tramite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // EAGER en todo — así no sale más el LazyInitializationException. (el practicante)
    // ⚠️ Funciona. Los tests pasan. Nadie lo nota. Ese es el punto. Corre start-lab.sh,
    //    pide un trámite, y mira el log SQL: el muro de JOINs. TODO_1 lo corrige a LAZY,
    //    e instalas AU-04 para que nadie vuelva a dejar una relación en manos del default.
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "contribuyente_id", nullable = false)
    private Contribuyente contribuyente;

    @Column(nullable = false, length = 50)
    private String tipo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoTramite estado;

    @OneToOne(mappedBy = "tramite", fetch = FetchType.EAGER)
    private Formulario29 formulario29;

    @OneToOne(mappedBy = "tramite", fetch = FetchType.EAGER)
    private Folio folio;

    @OneToMany(mappedBy = "tramite", fetch = FetchType.EAGER)
    private List<Adjunto> adjuntos = new ArrayList<>();

    protected Tramite() {
    }

    public Tramite(Contribuyente contribuyente, String tipo) {
        this.contribuyente = contribuyente;
        this.tipo = tipo;
        this.estado = EstadoTramite.BORRADOR;
    }

    /**
     * Avanza el trámite al estado {@code destino}.
     *
     * @throws TransicionIlegalException si el salto no está en la máquina de estados.
     *         La excepción es de dominio: no sabe que existe HTTP. Traducirla a un
     *         {@code ProblemDetail} es trabajo de la capa web.
     */
    public void transicionarA(EstadoTramite destino) {
        if (!estado.puedeTransicionarA(destino)) {
            throw new TransicionIlegalException(estado, destino);
        }
        this.estado = destino;
    }

    public Long getId() { return id; }
    public Contribuyente getContribuyente() { return contribuyente; }
    public String getTipo() { return tipo; }
    public EstadoTramite getEstado() { return estado; }
    public Formulario29 getFormulario29() { return formulario29; }
    public Folio getFolio() { return folio; }
    public List<Adjunto> getAdjuntos() { return adjuntos; }
}
