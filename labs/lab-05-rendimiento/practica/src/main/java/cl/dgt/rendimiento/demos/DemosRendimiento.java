package cl.dgt.rendimiento.demos;

import cl.dgt.rendimiento.repositories.ContribuyenteRepository;
import cl.dgt.rendimiento.soporte.ContadorDeConsultas;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Las cinco demos del laboratorio, declaradas y vacías.
 *
 * <p>Todas hacen <em>lo mismo</em>: armar la pantalla «contribuyentes y cuántos trámites tiene cada
 * uno». Lo único que cambia entre ellas es <strong>cuánto cuesta</strong>, así que todas terminan
 * imprimiendo la misma línea, y esa línea es el laboratorio:
 *
 * <pre>
 *   CONSULTAS: 201   ·   TIEMPO: 79 ms
 * </pre>
 *
 * <p>El molde para medir es siempre este:
 *
 * <pre>
 *   contador.reiniciar();
 *   long empezo = System.currentTimeMillis();
 *   ... lo que se quiere medir ...
 *   System.out.println("  CONSULTAS: " + contador.consultas()
 *           + "   ·   TIEMPO: " + (System.currentTimeMillis() - empezo) + " ms");
 * </pre>
 */
@Component
public class DemosRendimiento {

    private final ContribuyenteRepository contribuyentes;
    private final ContadorDeConsultas contador;

    public DemosRendimiento(ContribuyenteRepository contribuyentes, ContadorDeConsultas contador) {
        this.contribuyentes = contribuyentes;
        this.contador = contador;
    }

    // =========================================================================
    //  1 · EL CRIMEN, MEDIDO
    // -------------------------------------------------------------------------
    //  Trae los 200 contribuyentes con findAll() y recorre la lista sumando
    //  cuántos trámites tiene cada uno. Tocar esa lista es lo que dispara una
    //  consulta más por contribuyente, porque la relación es LAZY.
    //  Mide con el molde de arriba e imprime también cuántos trámites contaste.
    //  Qué se espera ver: tres cifras en el contador.
    //  Para pensar: ¿en qué línea exacta de tu método se disparan las 200?
    // =========================================================================
    @Transactional(readOnly = true)
    public void elCrimen() {
        seccion(1, "EL CRIMEN · findAll() y tocar la relación");
        // escribe aquí
    }

    // =========================================================================
    //  2 · JOIN FETCH
    // -------------------------------------------------------------------------
    //  Lo mismo que la demo 1, palabra por palabra, cambiando findAll() por el
    //  método con @Query que agregaste al repositorio. No se toca la entidad: se
    //  cambia la consulta, y ese es el punto del laboratorio entero.
    //  Qué se espera ver: el mismo resultado con el contador en una cifra.
    //  Para pensar: ¿por qué hace falta el `distinct` en esa consulta?
    // =========================================================================
    @Transactional(readOnly = true)
    public void conJoinFetch() {
        seccion(2, "JOIN FETCH · traerlo todo de una vez");
        // escribe aquí
    }

    // =========================================================================
    //  3 · @EntityGraph
    // -------------------------------------------------------------------------
    //  Otra vez lo mismo, llamando al método anotado con @EntityGraph. Sin JPQL:
    //  se nombra la relación que hay que traer y Spring Data se encarga.
    //  Qué se espera ver: el mismo número que la demo 2.
    //  Para pensar: si dan el mismo número, ¿cuál elegirías y por qué?
    // =========================================================================
    @Transactional(readOnly = true)
    public void conEntityGraph() {
        seccion(3, "@EntityGraph · lo mismo, sin JPQL");
        // escribe aquí
    }

    // =========================================================================
    //  4 · PROYECCIÓN
    // -------------------------------------------------------------------------
    //  Llama al método que devuelve List<ResumenContribuyente> y suma sus
    //  cuantosTramites. Aquí no hay entidades: hay un record con lo justo, y la
    //  cuenta la hizo la base. Imprime también la primera fila, para verlo.
    //  Qué se espera ver: una consulta, y 200 objetos en memoria en vez de 1.200.
    //  Para pensar: ¿qué NO se puede hacer con estos objetos?
    // =========================================================================
    @Transactional(readOnly = true)
    public void conProyeccion() {
        seccion(4, "PROYECCIÓN · traer solo lo que se muestra");
        // escribe aquí
    }

    // =========================================================================
    //  5 · LA PANTALLA QUE NO NECESITA TRÁMITES
    // -------------------------------------------------------------------------
    //  Un findAll() del que solo se usa la razón social — suma sus longitudes,
    //  por ejemplo. NO toques ningún trámite: esa es toda la gracia.
    //  Con la relación LAZY esto cuesta UNA consulta. En el paso 5 pondrás EAGER
    //  en la entidad y volverás a mirar este número.
    //  Qué se espera ver: 1 consulta ahora.
    //  Para pensar: ¿cuántas pantallas así hay en un sistema de verdad?
    // =========================================================================
    @Transactional(readOnly = true)
    public void laPantallaQueNoNecesitaTramites() {
        seccion(5, "LA OTRA PANTALLA · solo razones sociales");
        // escribe aquí
    }

    private void seccion(int numero, String titulo) {
        System.out.println();
        System.out.println("=== " + numero + " · " + titulo + " ===");
    }
}
