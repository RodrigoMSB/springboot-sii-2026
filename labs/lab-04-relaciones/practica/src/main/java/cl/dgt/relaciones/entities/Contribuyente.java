package cl.dgt.relaciones.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Un contribuyente. Es la misma clase de entidad que ya construiste en el Lab 3.5: anotaciones,
 * id generado, constructor sin argumentos para JPA. Aquí llega hecha, porque es repaso.
 *
 * <p>Le falta una cosa, y es del paso 3: el lado espejo de la relación, para poder ir de un
 * contribuyente a sus trámites.
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

    @Override
    public String toString() {
        return "Contribuyente{id=" + id + ", rut='" + rut + "', razonSocial='" + razonSocial + "'}";
    }
}
