package cl.dgt.concurrencia.entities;

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
 * Un contribuyente, tal como quedó al terminar el Lab 04. <strong>Viene dado y no se toca.</strong>
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
