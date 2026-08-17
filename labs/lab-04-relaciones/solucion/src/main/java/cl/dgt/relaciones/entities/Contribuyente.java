package cl.dgt.relaciones.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;

/**
 * Un contribuyente. Es la misma clase de entidad que ya construiste en el Lab 3b: anotaciones,
 * id generado, constructor sin argumentos para JPA. Aquí llega hecha, porque es repaso.
 *
 * <p>Lo único nuevo está abajo del todo: la lista de trámites, que llegó en el paso 3.
 */
@Entity
@Table(name = "contribuyente")
public class Contribuyente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 12)
    private String rut;

    @Column(name = "razon_social", nullable = false, length = 120)
    private String razonSocial;

    // =========================================================================
    //  EL LADO ESPEJO — llegó en el paso 3
    // -------------------------------------------------------------------------
    //  `mappedBy = "contribuyente"` significa: la relación NO se guarda aquí, se
    //  guarda en el campo `contribuyente` de Tramite, que es quien tiene la
    //  columna en la base. Este lado solo sirve para navegar.
    //  Qué se espera ver: al tocar esta lista sale un SELECT sobre `tramite`.
    //  Para pensar: si borras un trámite de esta lista, ¿cambia algo en la base?
    // =========================================================================
    @OneToMany(mappedBy = "contribuyente")
    private List<Tramite> tramites = new ArrayList<>();

    /** JPA lo exige. No se usa desde el código del laboratorio. */
    protected Contribuyente() {
    }

    public Contribuyente(String rut, String razonSocial) {
        this.rut = rut;
        this.razonSocial = razonSocial;
    }

    public Long getId() {
        return id;
    }

    public String getRut() {
        return rut;
    }

    public String getRazonSocial() {
        return razonSocial;
    }

    public List<Tramite> getTramites() {
        return tramites;
    }

    @Override
    public String toString() {
        return "Contribuyente{id=" + id + ", rut='" + rut + "', razonSocial='" + razonSocial + "'}";
    }
}
