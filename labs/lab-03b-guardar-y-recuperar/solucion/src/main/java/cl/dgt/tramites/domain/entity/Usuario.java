package cl.dgt.tramites.domain.entity;

import cl.dgt.tramites.domain.tipo.RolUsuario;
import jakarta.persistence.*;

/**
 * Quien inicia sesión: contribuyentes, funcionarios y fiscalizadores.
 *
 * <p><strong>RN-03:</strong> {@code claveHash} JAMÁS sale por la API. Aunque un hash no
 * permita autenticarse, exponerlo regala el <em>cost</em>, el algoritmo y material para
 * un ataque offline. La regla no admite matices: no sale.
 */
@Entity
@Table(name = "usuario")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 12)
    private String rut;

    @Column(nullable = false, length = 200)
    private String nombre;

    @Column(name = "clave_hash", nullable = false, length = 72)
    private String claveHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RolUsuario rol;

    protected Usuario() {
    }

    public Usuario(String rut, String nombre, String claveHash, RolUsuario rol) {
        this.rut = rut;
        this.nombre = nombre;
        this.claveHash = claveHash;
        this.rol = rol;
    }

    public Long getId() { return id; }
    public String getRut() { return rut; }
    public String getNombre() { return nombre; }
    public String getClaveHash() { return claveHash; }
    public RolUsuario getRol() { return rol; }
}
