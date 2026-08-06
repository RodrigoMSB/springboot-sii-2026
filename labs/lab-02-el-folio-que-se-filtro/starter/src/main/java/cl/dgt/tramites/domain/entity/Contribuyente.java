package cl.dgt.tramites.domain.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Quien tiene trámites ante la DGT.
 *
 * <p><strong>RN-03:</strong> {@code puntajeRiesgoInterno} JAMÁS sale por la API. Es un
 * juicio interno del servicio sobre una persona; filtrarlo no es un bug de formato, es
 * una filtración. Dos guardianes lo vigilan: AU-02 (estático, impide que un controlador
 * dependa de esta clase) y un test de contrato sobre el JSON (dinámico).
 *
 * <p>Sin Lombok (D-003): los constructores y accessors escritos a mano son contenido del
 * curso, no ruido. JPA exige el constructor sin argumentos; es {@code protected} para
 * que nadie lo use por accidente.
 */
@Entity
@Table(name = "contribuyente")
public class Contribuyente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 12)
    private String rut;

    @Column(name = "razon_social", nullable = false, length = 200)
    private String razonSocial;

    @Column(name = "puntaje_riesgo_interno", nullable = false)
    private int puntajeRiesgoInterno;

    // El practicante puso esto para que el endpoint dejara de dar error 500 (la
    // colección perezosa reventaba al serializar). "Ya que estamos, lo devuelvo entero."
    // Con la colección ignorada, la entidad serializa... y con ella se escapan TODOS sus
    // campos escalares, incluido puntajeRiesgoInterno. Ese es el crimen del Lab 02.
    @JsonIgnore
    @OneToMany(mappedBy = "contribuyente", fetch = FetchType.LAZY)
    private List<Tramite> tramites = new ArrayList<>();

    protected Contribuyente() {
        // requerido por JPA
    }

    public Contribuyente(String rut, String razonSocial, int puntajeRiesgoInterno) {
        this.rut = rut;
        this.razonSocial = razonSocial;
        this.puntajeRiesgoInterno = puntajeRiesgoInterno;
    }

    public Long getId() { return id; }
    public String getRut() { return rut; }
    public String getRazonSocial() { return razonSocial; }
    public int getPuntajeRiesgoInterno() { return puntajeRiesgoInterno; }
    public List<Tramite> getTramites() { return tramites; }
}
