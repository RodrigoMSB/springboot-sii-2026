package cl.dgt.contribuyentes;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * El contribuyente, en su versión reducida para este laboratorio.
 *
 * <p>En el monolito de los trece labs anteriores esta entidad tenía más campos
 * —entre ellos {@code puntajeRiesgoInterno}, que por la RN-03 jamás sale por la
 * API—. Aquí se quedó con lo mínimo que hace real la interacción entre los dos
 * servicios: un RUT y una razón social. Lo que se dejó fuera, y por qué, está
 * declarado en el README del laboratorio.
 *
 * <p>El esquema lo crea Flyway ({@code db/migration/V1__contribuyentes.sql}), no
 * Hibernate: {@code ddl-auto} está en {@code validate}, y si la entidad y la
 * migración se separan, la aplicación no arranca. Es la misma regla del Lab 06.
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

    protected Contribuyente() {
        // JPA lo necesita. No lo uses tú.
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
}
