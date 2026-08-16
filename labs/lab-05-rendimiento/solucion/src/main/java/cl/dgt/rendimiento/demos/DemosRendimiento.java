package cl.dgt.rendimiento.demos;

import cl.dgt.rendimiento.dto.ResumenContribuyente;
import cl.dgt.rendimiento.entities.Contribuyente;
import cl.dgt.rendimiento.repositories.ContribuyenteRepository;
import cl.dgt.rendimiento.soporte.ContadorDeConsultas;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Las cinco demos del laboratorio. Todas hacen <em>lo mismo</em>: armar la pantalla «contribuyentes
 * y cuántos trámites tiene cada uno».
 *
 * <p>Lo único que cambia entre ellas es <strong>cuánto cuesta</strong>, y por eso todas terminan
 * imprimiendo la misma línea: consultas y milisegundos. Hoy no se mira el SQL —serían doscientas
 * líneas—, se mira el número.
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
    //  findAll() trae los 200 contribuyentes en UNA consulta. Después, tocar la
    //  lista de trámites de cada uno dispara una consulta más por contribuyente,
    //  porque la relación es LAZY y nadie pidió lo contrario.
    //  1 + 200 = 201. De ahí el nombre: N+1.
    //  Qué se espera ver: tres cifras en el contador.
    //  Para pensar: ¿en qué línea exacta de este método se disparan las 200?
    // =========================================================================
    @Transactional(readOnly = true)
    public void elCrimen() {
        seccion(1, "EL CRIMEN · findAll() y tocar la relación");

        contador.reiniciar();
        long empezo = System.currentTimeMillis();

        List<Contribuyente> todos = contribuyentes.findAll();
        long totalTramites = 0;
        for (Contribuyente c : todos) {
            totalTramites += c.getTramites().size();   // <-- aquí, una consulta por vuelta
        }

        informe(todos.size(), totalTramites, empezo);
    }

    // =========================================================================
    //  2 · JOIN FETCH
    // -------------------------------------------------------------------------
    //  La misma pantalla, pidiendo de entrada los trámites en la misma consulta.
    //  No cambia el mapeo de la entidad: cambia la consulta. Ese es el punto del
    //  laboratorio entero.
    //  Qué se espera ver: el mismo resultado con el contador en una cifra.
    //  Para pensar: ¿por qué hizo falta el `distinct`?
    // =========================================================================
    @Transactional(readOnly = true)
    public void conJoinFetch() {
        seccion(2, "JOIN FETCH · traerlo todo de una vez");

        contador.reiniciar();
        long empezo = System.currentTimeMillis();

        List<Contribuyente> todos = contribuyentes.conJoinFetch();
        long totalTramites = 0;
        for (Contribuyente c : todos) {
            totalTramites += c.getTramites().size();   // ya están cargados: no consulta nada
        }

        informe(todos.size(), totalTramites, empezo);
    }

    // =========================================================================
    //  3 · @EntityGraph
    // -------------------------------------------------------------------------
    //  Lo mismo sin escribir JPQL: se nombra la relación en una anotación sobre
    //  el método del repositorio. Se prefiere cuando la consulta no tiene nada
    //  especial y solo hay que decidir qué se trae con ella.
    //  Qué se espera ver: el mismo número que la demo 2.
    //  Para pensar: si dan el mismo número, ¿cuál elegirías y por qué?
    // =========================================================================
    @Transactional(readOnly = true)
    public void conEntityGraph() {
        seccion(3, "@EntityGraph · lo mismo, sin JPQL");

        contador.reiniciar();
        long empezo = System.currentTimeMillis();

        List<Contribuyente> todos = contribuyentes.findAllBy();
        long totalTramites = 0;
        for (Contribuyente c : todos) {
            totalTramites += c.getTramites().size();
        }

        informe(todos.size(), totalTramites, empezo);
    }

    // =========================================================================
    //  4 · PROYECCIÓN
    // -------------------------------------------------------------------------
    //  Si la pantalla solo muestra rut, razón social y un número, traer entidades
    //  enteras con todos sus trámites es pagar de más. Esto trae un record con lo
    //  justo, y la cuenta la hace la base con un count().
    //  Qué se espera ver: una consulta, y 200 objetos en memoria en vez de 1.200.
    //  Para pensar: ¿qué NO se puede hacer con estos objetos? (Modificarlos.)
    // =========================================================================
    @Transactional(readOnly = true)
    public void conProyeccion() {
        seccion(4, "PROYECCIÓN · traer solo lo que se muestra");

        contador.reiniciar();
        long empezo = System.currentTimeMillis();

        List<ResumenContribuyente> resumen = contribuyentes.resumen();
        long totalTramites = resumen.stream().mapToLong(ResumenContribuyente::cuantosTramites).sum();

        informe(resumen.size(), totalTramites, empezo);
        System.out.println("  primera fila -> " + resumen.getFirst());
    }

    // =========================================================================
    //  5 · LA PANTALLA QUE NO NECESITA TRÁMITES
    // -------------------------------------------------------------------------
    //  Un listado que solo muestra razones sociales. Con la relación LAZY esto
    //  cuesta UNA consulta, porque nadie toca los trámites.
    //  Existe para el paso 5: al poner EAGER en la entidad para «arreglar» la
    //  demo 1, esta pantalla —que no pidió nada— empieza a pagar.
    //  Qué se espera ver: 1 consulta ahora; con EAGER, muchas más.
    //  Para pensar: ¿cuántas pantallas así hay en un sistema de verdad?
    // =========================================================================
    @Transactional(readOnly = true)
    public void laPantallaQueNoNecesitaTramites() {
        seccion(5, "LA OTRA PANTALLA · solo razones sociales");

        contador.reiniciar();
        long empezo = System.currentTimeMillis();

        List<Contribuyente> todos = contribuyentes.findAll();
        long letras = 0;
        for (Contribuyente c : todos) {
            letras += c.getRazonSocial().length();     // no se toca ni un trámite
        }

        System.out.println("  " + todos.size() + " contribuyentes · " + letras + " letras en total");
        System.out.println("  CONSULTAS: " + contador.consultas()
                + "   ·   TIEMPO: " + (System.currentTimeMillis() - empezo) + " ms");
    }

    private void informe(int cuantos, long totalTramites, long empezo) {
        System.out.println("  " + cuantos + " contribuyentes · " + totalTramites + " trámites");
        System.out.println("  CONSULTAS: " + contador.consultas()
                + "   ·   TIEMPO: " + (System.currentTimeMillis() - empezo) + " ms");
    }

    private void seccion(int numero, String titulo) {
        System.out.println();
        System.out.println("=== " + numero + " · " + titulo + " ===");
    }
}
