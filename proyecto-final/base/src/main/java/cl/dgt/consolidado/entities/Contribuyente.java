package cl.dgt.consolidado.entities;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "contribuyente")
public class Contribuyente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String rut;

    @Column(name = "razon_social", nullable = false)
    private String razonSocial;

    // Dato interno de fiscalización. No es del contribuyente y no sale de aquí.
    @Column(name = "puntaje_riesgo", nullable = false)
    private int puntajeRiesgo;

    @OneToMany(mappedBy = "contribuyente")
    private List<Tramite> tramites = new ArrayList<>();

    protected Contribuyente() {
    }

    public Contribuyente(String rut, String razonSocial, int puntajeRiesgo) {
        this.rut = rut;
        this.razonSocial = razonSocial;
        this.puntajeRiesgo = puntajeRiesgo;
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

    public int getPuntajeRiesgo() {
        return puntajeRiesgo;
    }

    public List<Tramite> getTramites() {
        return tramites;
    }
}
