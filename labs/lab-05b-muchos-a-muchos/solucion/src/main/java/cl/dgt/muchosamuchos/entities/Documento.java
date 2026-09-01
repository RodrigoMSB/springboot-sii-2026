package cl.dgt.muchosamuchos.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "documento")
public class Documento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String codigo;

    @Column(nullable = false, length = 120)
    private String nombre;

    // El lado espejo. `mappedBy` nombra el CAMPO de Tramite, no la tabla ni la
    // columna: la tabla intermedia la manda el otro lado, y este no guarda nada.
    @ManyToMany(mappedBy = "documentos")
    private Set<Tramite> tramites = new LinkedHashSet<>();

    protected Documento() {
    }

    public Documento(String codigo, String nombre) {
        this.codigo = codigo;
        this.nombre = nombre;
    }

    public Long getId() {
        return id;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public Set<Tramite> getTramites() {
        return tramites;
    }

    // Un Set decide qué es «el mismo elemento» con equals y hashCode. Sin esto,
    // `documentos.remove(otroObjetoIgual)` no quitaría nada. Se comparan por
    // `codigo`, que es la clave de negocio y es `unique` en la tabla — nunca por
    // `id`, que es null mientras la fila no se haya guardado.
    @Override
    public boolean equals(Object otro) {
        return otro instanceof Documento d && Objects.equals(codigo, d.codigo);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(codigo);
    }

    @Override
    public String toString() {
        return "Documento{id=" + id + ", codigo='" + codigo + "', nombre='" + nombre + "'}";
    }
}
