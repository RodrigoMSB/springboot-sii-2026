package cl.dgt.relaciones.demos;

import cl.dgt.relaciones.repositories.ContribuyenteRepository;
import cl.dgt.relaciones.repositories.TramiteRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Las seis demos del laboratorio, declaradas y vacías.
 *
 * <p>Cada paso llena una y descomenta su llamada en {@code Lab04Application}, así que el programa
 * crece contigo: si algo se rompe, sabes qué línea lo rompió.
 *
 * <p>Lo que hay que mirar hoy no es lo que imprime el método: es el <strong>SQL que aparece entre
 * medio</strong>, y sobre todo <strong>cuántas veces</strong>.
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
    //  Guarda 3 contribuyentes y 6 trámites, 2 por contribuyente, cada uno
    //  apuntando al suyo. La relación no se guarda aparte: viaja dentro del
    //  INSERT del trámite, en la columna contribuyente_id.
    //  Empieza contando lo que quedó de la vez anterior —la base PERSISTE— y
    //  borrando. Los trámites primero: la clave foránea no deja borrar un
    //  contribuyente que tenga trámites.
    //  Qué se espera ver: el INSERT de tramite incluyendo contribuyente_id.
    //  Para pensar: ¿por qué no hay un INSERT en una tercera tabla de relación?
    // =========================================================================
    public void guardarConRelacion() {
        seccion(1, "GUARDAR CON RELACIÓN · @ManyToOne");
        // escribe aquí
        //
        // Guarda el primer trámite en una variable y deja su id en
        // `this.primerTramiteId`: las demos 2 y 5 lo necesitan.
    }

    // =========================================================================
    //  2 · NAVEGAR DEL TRÁMITE A SU CONTRIBUYENTE
    // -------------------------------------------------------------------------
    //  Carga el trámite `primerTramiteId`, imprímelo, imprime una marca que diga
    //  que todavía no has tocado el contribuyente, y solo entonces pide su razón
    //  social. El segundo select tiene que aparecer DESPUÉS de esa marca.
    //  Este método es @Transactional a propósito: sin eso la sesión ya estaría
    //  cerrada al llegar aquí, que es justo el paso 5.
    //  Qué se espera ver: dos bloques `Hibernate:`, con tu marca en medio.
    //  Para pensar: ¿cuándo interesa que el segundo select no llegue a ocurrir?
    // =========================================================================
    @Transactional(readOnly = true)
    public void navegarDeTramiteAContribuyente() {
        seccion(2, "NAVEGAR · tramite -> contribuyente");
        // escribe aquí
    }

    // =========================================================================
    //  3 · EL LADO ESPEJO
    // -------------------------------------------------------------------------
    //  Busca el contribuyente por su RUT, imprime su razón social y una marca, y
    //  solo entonces recorre su lista de trámites. La lista es el lado espejo que
    //  agregaste en la entidad: no guarda nada, sirve para navegar.
    //  Qué se espera ver: un select sobre `contribuyente`, y al tocar la lista,
    //  otro sobre `tramite` con where contribuyente_id = ?
    //  Para pensar: si borras un trámite de esa lista, ¿cambia algo en la base?
    // =========================================================================
    @Transactional(readOnly = true)
    public void listarTramitesDeUnContribuyente() {
        seccion(3, "LADO ESPEJO · @OneToMany(mappedBy)");
        // escribe aquí
    }

    // =========================================================================
    //  4 · LAZY CONTRA EAGER, CONTADO
    // -------------------------------------------------------------------------
    //  Trae los 6 trámites con findAll() y NO toques el contribuyente de ninguno.
    //  Imprime una marca antes y otra después, para poder contar a ojo cuántos
    //  bloques `Hibernate:` salieron entre las dos.
    //  Qué se espera ver: con LAZY, uno. Después cambiarás la entidad a EAGER y
    //  volverás a contar sin tocar este método.
    //  Para pensar: con 6 trámites la diferencia es pequeña. ¿Y con 6.000?
    // =========================================================================
    public void lazyContraEager() {
        seccion(4, "LAZY vs EAGER · contar los SELECT");
        // escribe aquí
    }

    // =========================================================================
    //  5 · EL ERROR QUE TODO EL MUNDO SE ENCUENTRA
    // -------------------------------------------------------------------------
    //  Lo mismo que la demo 2, pero SIN @Transactional (fíjate: aquí no está).
    //  Carga el trámite y toca su contribuyente. Va a reventar: atrapa la
    //  LazyInitializationException e imprime su nombre y su mensaje, para que el
    //  programa siga y se pueda leer con calma.
    //  Qué se espera ver: «Could not initialize proxy … - no session».
    //  Para pensar: no es un fallo de JPA. ¿Qué te está diciendo realmente?
    // =========================================================================
    public void elErrorDeLaSesionCerrada() {
        seccion(5, "LazyInitializationException · fuera de la transacción");
        // escribe aquí
    }

    // =========================================================================
    //  6 · UNA CONSULTA QUE CRUZA LA RELACIÓN
    // -------------------------------------------------------------------------
    //  Llama al método que agregaste al repositorio en el paso 6 e imprime lo que
    //  devuelve. El trámite no tiene RUT: lo tiene su contribuyente, y el nombre
    //  del método atraviesa la relación para llegar hasta él.
    //  Qué se espera ver: UN solo select, con un join dentro.
    //  Para pensar: ¿hasta dónde se puede encadenar un nombre así?
    // =========================================================================
    public void consultaQueCruzaLaRelacion() {
        seccion(6, "CONSULTA DERIVADA QUE NAVEGA · findByContribuyenteRut()");
        // escribe aquí
    }

    private void seccion(int numero, String titulo) {
        System.out.println();
        System.out.println("=== " + numero + " · " + titulo + " ===");
    }
}
