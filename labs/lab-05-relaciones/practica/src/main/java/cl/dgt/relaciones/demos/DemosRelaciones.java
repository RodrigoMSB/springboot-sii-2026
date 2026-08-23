package cl.dgt.relaciones.demos;

import cl.dgt.relaciones.repositories.ContribuyenteRepository;
import cl.dgt.relaciones.repositories.TramiteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DemosRelaciones {

    private final ContribuyenteRepository contribuyentes;
    private final TramiteRepository tramites;

    private Long primerTramiteId;

    public DemosRelaciones(ContribuyenteRepository contribuyentes, TramiteRepository tramites) {
        this.contribuyentes = contribuyentes;
        this.tramites = tramites;
    }

    public void guardarConRelacion() {
        seccion(1, "GUARDAR CON RELACIÓN · @ManyToOne");
        // Guarda un contribuyente y dos trámites suyos, y mira el INSERT.
        // escribe aquí
    }

    @Transactional(readOnly = true)
    public void navegarDeTramiteAContribuyente() {
        seccion(2, "NAVEGAR · tramite -> contribuyente");
        // Trae un trámite y accede a su contribuyente; cuenta los SELECT.
        // escribe aquí
    }

    @Transactional(readOnly = true)
    public void listarTramitesDeUnContribuyente() {
        seccion(3, "LADO ESPEJO · @OneToMany(mappedBy)");
        // Trae un contribuyente y recorre su lista de trámites.
        // escribe aquí
    }

    public void lazyContraEager() {
        seccion(4, "LAZY vs EAGER · contar los SELECT");
        // Trae los cuatro trámites y cuenta cuántos SELECT dispara cada estrategia.
        // escribe aquí
    }

    public void elErrorDeLaSesionCerrada() {
        seccion(5, "LazyInitializationException · fuera de la transacción");
        // Accede a la relación FUERA de la transacción y deja que falle.
        // escribe aquí
    }

    public void consultaQueCruzaLaRelacion() {
        seccion(6, "CONSULTA DERIVADA QUE NAVEGA · findByContribuyenteRut()");
        // Llama a findByContribuyenteRut() y mira el JOIN que genera.
        // escribe aquí
    }

    private void seccion(int numero, String titulo) {
        System.out.println();
        System.out.println("=== " + numero + " · " + titulo + " ===");
    }
}
