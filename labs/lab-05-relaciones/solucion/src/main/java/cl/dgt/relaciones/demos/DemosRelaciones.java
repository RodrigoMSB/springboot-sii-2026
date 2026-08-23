package cl.dgt.relaciones.demos;

import cl.dgt.relaciones.entities.Contribuyente;
import cl.dgt.relaciones.entities.Tramite;
import cl.dgt.relaciones.repositories.ContribuyenteRepository;
import cl.dgt.relaciones.repositories.TramiteRepository;
import org.hibernate.LazyInitializationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

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

        System.out.println("  al arrancar había " + contribuyentes.count() + " contribuyentes y "
                + tramites.count() + " trámites de la vez anterior");

        tramites.deleteAll();
        contribuyentes.deleteAll();

        Contribuyente andes = contribuyentes.save(
                new Contribuyente("76.543.210-K", "Comercial Andes Ltda."));
        Contribuyente rutaSur = contribuyentes.save(
                new Contribuyente("77.111.222-3", "Transportes Ruta Sur SpA"));
        Contribuyente espiga = contribuyentes.save(
                new Contribuyente("78.999.888-1", "Panadería La Espiga EIRL"));
        System.out.println("  3 contribuyentes guardados");

        Tramite primero = tramites.save(new Tramite(
                "Declaración F29", "RECIBIDO", LocalDate.of(2026, 3, 10), andes));
        this.primerTramiteId = primero.getId();

        tramites.save(new Tramite("Certificado de situación", "EMITIDO", LocalDate.of(2026, 7, 2), andes));
        tramites.save(new Tramite("Declaración F29", "RECIBIDO", LocalDate.of(2026, 4, 5), rutaSur));
        tramites.save(new Tramite("Inicio de actividades", "APROBADO", LocalDate.of(2026, 1, 20), rutaSur));
        tramites.save(new Tramite("Declaración F29", "OBSERVADO", LocalDate.of(2026, 5, 18), espiga));
        tramites.save(new Tramite("Cambio de domicilio", "APROBADO", LocalDate.of(2026, 6, 30), espiga));
        System.out.println("  6 trámites guardados, 2 por contribuyente");
        System.out.println("  el trámite " + primerTramiteId + " es de " + andes.getRazonSocial());
    }

    @Transactional(readOnly = true)
    public void navegarDeTramiteAContribuyente() {
        seccion(2, "NAVEGAR · tramite -> contribuyente");

        Tramite tramite = tramites.findById(primerTramiteId).orElseThrow();
        System.out.println("  trámite cargado: " + tramite);
        System.out.println("  --- todavía NO se ha tocado el contribuyente ---");

        String razon = tramite.getContribuyente().getRazonSocial();
        System.out.println("  ahora sí: " + razon);
    }

    @Transactional(readOnly = true)
    public void listarTramitesDeUnContribuyente() {
        seccion(3, "LADO ESPEJO · @OneToMany(mappedBy)");

        Contribuyente andes = contribuyentes.findByRut("76.543.210-K").orElseThrow();
        System.out.println("  contribuyente: " + andes.getRazonSocial());
        System.out.println("  --- todavía NO se ha tocado la lista ---");

        List<Tramite> suyos = andes.getTramites();
        System.out.println("  tiene " + suyos.size() + " trámites:");
        suyos.forEach(t -> System.out.println("    " + t));
    }

    public void lazyContraEager() {
        seccion(4, "LAZY vs EAGER · contar los SELECT");

        System.out.println("  >>>>>> EMPIEZA EL CONTEO — cuenta los 'Hibernate:' hasta la marca de fin");
        List<Tramite> todos = tramites.findAll();
        System.out.println("  <<<<<< FIN DEL CONTEO — " + todos.size() + " trámites traídos");
        System.out.println("  (no se tocó el contribuyente de ninguno)");
    }

    public void elErrorDeLaSesionCerrada() {
        seccion(5, "LazyInitializationException · fuera de la transacción");

        Tramite tramite = tramites.findById(primerTramiteId).orElseThrow();
        System.out.println("  trámite cargado (y la sesión ya se cerró): " + tramite);

        try {
            String razon = tramite.getContribuyente().getRazonSocial();
            System.out.println("  razón social: " + razon + "   <-- si ves esto, algo cambió");
        } catch (LazyInitializationException e) {
            System.out.println("  REVENTÓ, y está bien: " + e.getClass().getSimpleName());
            System.out.println("  mensaje: " + e.getMessage());
        }
    }

    public void consultaQueCruzaLaRelacion() {
        seccion(6, "CONSULTA DERIVADA QUE NAVEGA · findByContribuyenteRut()");

        List<Tramite> deAndes = tramites.findByContribuyenteRut("76.543.210-K");
        System.out.println("  trámites del RUT 76.543.210-K -> " + deAndes.size());
        deAndes.forEach(t -> System.out.println("    " + t));
    }

    private void seccion(int numero, String titulo) {
        System.out.println();
        System.out.println("=== " + numero + " · " + titulo + " ===");
    }
}
