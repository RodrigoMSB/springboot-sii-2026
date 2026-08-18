package cl.dgt.seguridad.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "usuario")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String nombre;

    @Column(name = "clave_hash", nullable = false)
    private String claveHash;

    @Column(nullable = false)
    private String rol;

    protected Usuario() {
    }

    public Usuario(String nombre, String claveHash, String rol) {
        this.nombre = nombre;
        this.claveHash = claveHash;
        this.rol = rol;
    }

    public Long getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getClaveHash() {
        return claveHash;
    }

    public String getRol() {
        return rol;
    }
}
