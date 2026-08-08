package cl.dgt.tramites.domain.entity;

import jakarta.persistence.*;

/**
 * El número que acredita un trámite ante la DGT.
 *
 * <p><strong>RN-01:</strong> irrepetible — {@code numero} es la clave primaria.
 * <strong>RN-02:</strong> secuencial sin saltos — por eso el número lo asigna el
 * contador bloqueado ({@code contador_folio}) y no una {@code SEQUENCE}, que es no
 * transaccional y deja huecos al revertir.
 *
 * <p>El {@code UNIQUE} sobre {@code tramite_id} es el suelo de RN-05: un trámite tiene a
 * lo más un folio, así que un reintento no puede crear un segundo.
 */
@Entity
@Table(name = "folio")
public class Folio {

    @Id
    @Column(nullable = false)
    private Long numero;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tramite_id", nullable = false, unique = true)
    private Tramite tramite;

    protected Folio() {
    }

    public Folio(Long numero, Tramite tramite) {
        this.numero = numero;
        this.tramite = tramite;
    }

    public Long getNumero() { return numero; }
    public Tramite getTramite() { return tramite; }
}
