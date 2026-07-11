package cl.dgt.tramites.domain.entity;

import jakarta.persistence.*;

/**
 * Un archivo acompañando a un trámite.
 *
 * <p>{@code mimeReal} es el tipo sniffeado del <em>contenido</em>, no el que declaró el
 * navegador. Un `.pdf` que en realidad es un ejecutable se detecta aquí, no en el nombre.
 */
@Entity
@Table(name = "adjunto")
public class Adjunto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tramite_id", nullable = false)
    private Tramite tramite;

    @Column(name = "nombre_archivo", nullable = false, length = 255)
    private String nombreArchivo;

    @Column(name = "mime_real", nullable = false, length = 100)
    private String mimeReal;

    protected Adjunto() {
    }

    public Adjunto(Tramite tramite, String nombreArchivo, String mimeReal) {
        this.tramite = tramite;
        this.nombreArchivo = nombreArchivo;
        this.mimeReal = mimeReal;
    }

    public Long getId() { return id; }
    public Tramite getTramite() { return tramite; }
    public String getNombreArchivo() { return nombreArchivo; }
    public String getMimeReal() { return mimeReal; }
}
