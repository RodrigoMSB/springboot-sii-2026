package cl.dgt.rendimiento.demos;

import cl.dgt.rendimiento.dto.ResumenContribuyente;
import cl.dgt.rendimiento.entities.Contribuyente;
import cl.dgt.rendimiento.repositories.ContribuyenteRepository;
import cl.dgt.rendimiento.soporte.ContadorDeConsultas;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DemosRendimiento {

    private final ContribuyenteRepository contribuyentes;
    private final ContadorDeConsultas contador;

    public DemosRendimiento(ContribuyenteRepository contribuyentes, ContadorDeConsultas contador) {
        this.contribuyentes = contribuyentes;
        this.contador = contador;
    }

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
