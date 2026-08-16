package cl.dgt.relaciones.demos;

import cl.dgt.relaciones.entities.Contribuyente;
import cl.dgt.relaciones.entities.Tramite;
import cl.dgt.relaciones.repositories.ContribuyenteRepository;
import cl.dgt.relaciones.repositories.TramiteRepository;
import org.hibernate.LazyInitializationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Las seis demos del laboratorio, en el orden en que se construyen.
 *
 * <p>Cada método imprime lo que hace. Lo que hay que mirar hoy no es lo que imprime el método: es
 * el <strong>SQL que aparece entre medio</strong>, y sobre todo <strong>cuántas veces</strong>.
 */
@Component
public class DemosRelaciones {

    private final ContribuyenteRepository contribuyentes;
    private final TramiteRepository tramites;

    /** El id del primer trámite guardado. Lo usan las demos 2 y 5. */
    private Long primerTramiteId;

    public DemosRelaciones(ContribuyenteRepository contribuyentes, TramiteRepository tramites) {
        this.contribuyentes = contribuyentes;
        this.tramites = tramites;
    }

    // =========================================================================
    //  1 · GUARDAR CON RELACIÓN
    // -------------------------------------------------------------------------
    //  Se guarda el contribuyente y después el trámite apuntándole. La relación
    //  no se guarda aparte: viaja dentro del INSERT del trámite, en la columna
    //  contribuyente_id, porque ese es el lado que manda (@JoinColumn).
    //  El SQL: insert into tramite (contribuyente_id, estado, fecha, tipo) …
    //  Empieza contando lo que quedó de la vez anterior —la base PERSISTE— y
    //  borrando. Los trámites primero: la clave foránea no deja borrar un
    //  contribuyente que tenga trámites.
    //  Para pensar: ¿por qué no hay un INSERT en una tercera tabla de relación?
    // =========================================================================
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

    // =========================================================================
    //  2 · NAVEGAR DEL TRÁMITE A SU CONTRIBUYENTE
    // -------------------------------------------------------------------------
    //  Cargar el trámite trae UN select. Pedirle su contribuyente trae OTRO, y
    //  aparece justo cuando se toca, no antes: eso es LAZY.
    //  Este método es @Transactional a propósito. Sin eso, la sesión estaría
    //  cerrada al llegar aquí y el segundo select no podría ocurrir — que es
    //  exactamente el paso 5.
    //  Qué se espera ver: DOS bloques `Hibernate:`, separados por el texto que
    //  imprime este método.
    //  Para pensar: ¿cuándo interesa que el segundo select no llegue a ocurrir?
    // =========================================================================
    @Transactional(readOnly = true)
    public void navegarDeTramiteAContribuyente() {
        seccion(2, "NAVEGAR · tramite -> contribuyente");

        Tramite tramite = tramites.findById(primerTramiteId).orElseThrow();
        System.out.println("  trámite cargado: " + tramite);
        System.out.println("  --- todavía NO se ha tocado el contribuyente ---");

        String razon = tramite.getContribuyente().getRazonSocial();
        System.out.println("  ahora sí: " + razon);
    }

    // =========================================================================
    //  3 · EL LADO ESPEJO
    // -------------------------------------------------------------------------
    //  Desde el contribuyente se llega a sus trámites, aunque la columna no esté
    //  de este lado. `mappedBy` es lo que le dice a JPA dónde mirar: en el campo
    //  `contribuyente` de Tramite.
    //  Qué se espera ver: un select sobre `contribuyente`, y al tocar la lista,
    //  otro sobre `tramite` con where contribuyente_id = ?
    //  Para pensar: este lado no guarda nada. ¿Para qué sirve entonces?
    // =========================================================================
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

    // =========================================================================
    //  4 · LAZY CONTRA EAGER, CONTADO
    // -------------------------------------------------------------------------
    //  Se traen los 6 trámites y NO se toca el contribuyente de ninguno. Con
    //  LAZY hace falta un solo select. Cambiando la entidad a EAGER, Hibernate
    //  trae además cada contribuyente aunque nadie los haya pedido.
    //  Qué se espera ver: entre las dos marcas de abajo, UN bloque `Hibernate:`
    //  con LAZY. Con EAGER salen cuatro. Se cuentan a ojo, en la consola.
    //  Para pensar: con 6 trámites la diferencia son 3 selects. ¿Y con 6.000?
    // =========================================================================
    public void lazyContraEager() {
        seccion(4, "LAZY vs EAGER · contar los SELECT");

        System.out.println("  >>>>>> EMPIEZA EL CONTEO — cuenta los 'Hibernate:' hasta la marca de fin");
        List<Tramite> todos = tramites.findAll();
        System.out.println("  <<<<<< FIN DEL CONTEO — " + todos.size() + " trámites traídos");
        System.out.println("  (no se tocó el contribuyente de ninguno)");
    }

    // =========================================================================
    //  5 · EL ERROR QUE TODO EL MUNDO SE ENCUENTRA
    // -------------------------------------------------------------------------
    //  Este método NO es @Transactional. El repositorio abre su transacción, la
    //  cierra, y devuelve un trámite ya desconectado de la base. Tocar ahora la
    //  relación LAZY es pedirle un select a una sesión que ya no existe.
    //  Qué se espera ver: LazyInitializationException, atrapada e impresa aquí
    //  para que el programa siga.
    //  Para pensar: no es un fallo de JPA. ¿Qué te está diciendo realmente?
    // =========================================================================
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

    // =========================================================================
    //  6 · UNA CONSULTA QUE CRUZA LA RELACIÓN
    // -------------------------------------------------------------------------
    //  findByContribuyenteRut: el nombre del método atraviesa la relación. El
    //  trámite no tiene RUT — lo tiene su contribuyente—, y Spring Data lo
    //  entiende y escribe el JOIN.
    //  El SQL: select … from tramite t join contribuyente c on … where c.rut = ?
    //  Qué se espera ver: UN solo select, con el join dentro.
    //  Para pensar: ¿hasta dónde se puede encadenar un nombre así?
    // =========================================================================
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
