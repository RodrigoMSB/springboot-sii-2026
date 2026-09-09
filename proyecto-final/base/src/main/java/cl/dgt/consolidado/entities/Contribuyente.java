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

    // EL ATAJO QUE NO ES ATAJO. `getTramites()` trae TODOS los trámites del contribuyente, de
    // cualquier año: esta lista no sabe nada de tu período. Y no falla — `open-in-view` viene
    // activo, así que la colección se carga sin excepción, la petición responde 200 y el total
    // sale mal. Comprobado: para 76.111.111-1 devuelve 6 trámites donde el consolidado de 2026
    // pide 4.
    //
    // El filtro por fechas va EN LA BASE, en la consulta de `TramiteRepository`. Traer el
    // contribuyente y recorrer esta lista filtrando en Java es «insuficiente» en el criterio 5
    // de la rúbrica incluso si consigues que el número salga bien.
    @OneToMany(mappedBy = "contribuyente")
    private List<Tramite> tramites = new ArrayList<>();

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
}
