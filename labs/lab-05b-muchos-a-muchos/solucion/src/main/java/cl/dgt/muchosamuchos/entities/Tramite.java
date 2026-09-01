package cl.dgt.muchosamuchos.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "tramite")
public class Tramite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 40)
    private String tipo;

    @Column(nullable = false, length = 12)
    private String rut;

    @Column(nullable = false)
    private LocalDate fecha;

    // Este lado es el DUEÑO: el que lleva @JoinTable es el que escribe en la
    // tabla intermedia. `fetch = LAZY` es lo que @ManyToMany ya hace por defecto,
    // y se escribe igual: una relación que se lee no se declara de memoria.
    //
    // Y `Set`, no `List`. El paso 4 mide lo que cuesta la diferencia.
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "tramite_documento",
            joinColumns = @JoinColumn(name = "tramite_id"),
            inverseJoinColumns = @JoinColumn(name = "documento_id"))
    private Set<Documento> documentos = new LinkedHashSet<>();

    protected Tramite() {
    }

    public Tramite(String tipo, String rut, LocalDate fecha) {
        this.tipo = tipo;
        this.rut = rut;
        this.fecha = fecha;
    }

    public Long getId() {
        return id;
    }

    public String getTipo() {
        return tipo;
    }

    public String getRut() {
        return rut;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public Set<Documento> getDocumentos() {
        return documentos;
    }

    public void adjuntar(Documento documento) {
        documentos.add(documento);
    }

    public void quitar(Documento documento) {
        documentos.remove(documento);
    }

    @Override
    public String toString() {
        return "Tramite{id=" + id + ", tipo='" + tipo + "', rut='" + rut + "', fecha=" + fecha + "}";
    }
}
